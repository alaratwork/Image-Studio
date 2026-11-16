package studio;

import controller.ImageController;
import controller.ImageDatabase;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import model.abstraction.writer.WritePNG;

/**
 * Checks for the Java-to-C++ bridge. Runs without a test framework or a
 * display: {@code make test}, or
 * {@code java -cp build/studio-classes:build/test-classes studio.BridgeTest}.
 * Exits non-zero on the first failure.
 *
 * <p>The end-to-end checks need the engine built ({@code make engine}) and run
 * from the project root, against {@code samples/library}.
 */
public final class BridgeTest {

  private static final Path ENGINE = Path.of("search-engine");
  private static final Path LIBRARY = Path.of("samples/library");
  private static int passed = 0;

  private BridgeTest() {
  }

  public static void main(String[] args) throws Exception {
    Path cache = Files.createTempDirectory("image-studio-test-cache");
    SearchEngineBridge bridge = new SearchEngineBridge(ENGINE, cache, 60);

    parsesEngineOutput();
    parsesNamesWithSpacesAndSymbols();
    ignoresLinesThatAreNotResults();
    rejectsUnknownFeature(bridge);
    reportsMissingLibrary(bridge);
    reportsMissingEngine(cache);
    reusesIndexUntilLibraryChanges(bridge);
    findsOtherPhotosOfTheSameItem(bridge);
    searchesAnImageExportedByTheEditor(bridge);

    System.out.println("BridgeTest: all " + passed + " checks passed");
  }

  private static void parsesEngineOutput() {
    List<SearchEngineBridge.Match> m = SearchEngineBridge.parseMatches(String.join("\n",
        "target: samples/library/salt_IMG_5481.jpg   feature: custom",
        "top 4 matches:",
        "   1.  salt_IMG_5482.jpg  (dist 0.0235)",
        "   2.  salt_IMG_5483.jpg  (dist 0.0906)",
        "  10.  salt_2.jpg        (dist 0.1238)"));
    check(m.size() == 3, "three results parsed, got " + m.size());
    check(m.get(0).rank() == 1 && m.get(0).fileName().equals("salt_IMG_5482.jpg"), "first result");
    check(m.get(0).distance() == 0.0235, "first distance");
    check(m.get(2).rank() == 10 && m.get(2).fileName().equals("salt_2.jpg"), "padded name trimmed");
  }

  private static void parsesNamesWithSpacesAndSymbols() {
    List<SearchEngineBridge.Match> m = SearchEngineBridge.parseMatches(
        "   1.  apricot+lemon (copy) 2.jpg  (dist 0.0000)\r\n   2.  x.png  (dist 1.5e-03)");
    check(m.size() == 2, "two results parsed");
    check(m.get(0).fileName().equals("apricot+lemon (copy) 2.jpg"), "name with spaces and symbols");
    check(m.get(0).identical(), "zero distance means identical");
    check(m.get(1).distance() == 0.0015, "scientific notation distance");
  }

  private static void ignoresLinesThatAreNotResults() {
    check(SearchEngineBridge.parseMatches("").isEmpty(), "empty output");
    check(SearchEngineBridge.parseMatches(null).isEmpty(), "null output");
    check(SearchEngineBridge.parseMatches("usage: match <target> ...\n  skip (unreadable): a.jpg")
        .isEmpty(), "usage and skip lines ignored");
  }

  private static void rejectsUnknownFeature(SearchEngineBridge bridge) throws Exception {
    try {
      bridge.findSimilar(LIBRARY.resolve("salt_IMG_5481.jpg"), LIBRARY, "dnn", 4);
      fail("dnn should be rejected for image queries");
    } catch (IllegalArgumentException expected) {
      check(expected.getMessage().contains("Unknown feature type"), "unknown feature message");
    }
  }

  private static void reportsMissingLibrary(SearchEngineBridge bridge) throws Exception {
    try {
      bridge.index(Path.of("no/such/folder"), "hist");
      fail("a missing library should raise SearchException");
    } catch (SearchException expected) {
      check(expected.getMessage().contains("could not be found"), "missing library message");
    }
  }

  private static void reportsMissingEngine(Path cache) throws Exception {
    SearchEngineBridge broken = new SearchEngineBridge(Path.of("no/engine/here"), cache, 5);
    try {
      broken.index(LIBRARY, "hist");
      fail("a missing engine should raise SearchException");
    } catch (SearchException expected) {
      check(expected.getMessage().contains("make engine"), "missing engine message names the fix");
    }
  }

  private static void reusesIndexUntilLibraryChanges(SearchEngineBridge bridge) throws Exception {
    Path library = Files.createTempDirectory("image-studio-test-library");
    for (String name : List.of("salt_IMG_5481.jpg", "salt_IMG_5482.jpg", "ketchup_bottle_1.jpg")) {
      Files.copy(LIBRARY.resolve(name), library.resolve(name));
    }
    Path csv = bridge.index(library, "hist");
    check(Files.readAllLines(csv).size() == 3, "one index row per image");
    FileTime built = Files.getLastModifiedTime(csv);
    Thread.sleep(1100);   // filesystem timestamps can be second-granular
    check(bridge.index(library, "hist").equals(csv)
        && Files.getLastModifiedTime(csv).equals(built), "unchanged library reuses the index");

    Files.copy(LIBRARY.resolve("lemon_1.jpg"), library.resolve("lemon_1.jpg"));
    bridge.index(library, "hist");
    check(Files.readAllLines(csv).size() == 4, "adding a photo rebuilds the index");
  }

  private static void findsOtherPhotosOfTheSameItem(SearchEngineBridge bridge) throws Exception {
    List<SearchEngineBridge.Match> m =
        bridge.findSimilar(LIBRARY.resolve("salt_IMG_5481.jpg"), LIBRARY, "custom", 4);
    check(m.size() == 4, "four results");
    for (SearchEngineBridge.Match match : m) {
      check(match.fileName().startsWith("salt"), "expected salt photos, got " + match.fileName());
    }
    for (int i = 1; i < m.size(); i++) {
      check(m.get(i).distance() >= m.get(i - 1).distance(), "results ordered by distance");
    }
  }

  // the GUI path: the editor loads a photo, the bridge exports it with the
  // editor's own PNG writer, and the engine searches with the exported file
  private static void searchesAnImageExportedByTheEditor(SearchEngineBridge bridge)
      throws Exception {
    ImageDatabase database = new ImageDatabase();
    new ImageController(new StringReader(""), new StringBuilder(), database)
        .loadImage(LIBRARY.resolve("ketchup_bottle_IMG_5476.jpg").toString(), "ketchup");
    Path query = Files.createTempFile("image-studio-test-query-", ".png");
    new WritePNG().write(database.get_image("ketchup"), query.toString());

    // not distance 0: Java's JPEG decoder and OpenCV's differ by a few levels per
    // pixel, so the exported copy is close to the original but not bit-identical
    List<SearchEngineBridge.Match> m = bridge.findSimilar(query, LIBRARY, "hist", 3);
    check(m.get(0).fileName().equals("ketchup_bottle_IMG_5476.jpg") && m.get(0).distance() < 0.1,
        "an unedited photo finds itself first - got " + m.get(0));
    check(m.get(1).fileName().startsWith("ketchup_bottle"), "then another ketchup bottle");
    Files.delete(query);
  }

  private static void check(boolean condition, String what) {
    if (!condition) {
      fail(what);
    }
    passed++;
  }

  private static void fail(String what) {
    System.err.println("BridgeTest FAILED: " + what);
    System.exit(1);
  }
}
