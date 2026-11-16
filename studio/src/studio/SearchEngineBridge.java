package studio;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Connects the Java editor to the C++ search engine across a process boundary.
 *
 * <p>The engine is used exactly as shipped, through its two command-line programs:
 * <ul>
 *   <li>{@code compute_features <imageDir> <featureType> <outCsv>} - indexes a
 *       library once per feature type;</li>
 *   <li>{@code match <targetImage> <featureType> <featureCsv> <N>} - ranks the
 *       library against a query image and prints the top N.</li>
 * </ul>
 * This class owns everything around those two calls: finding the binaries,
 * caching an index per library and feature and rebuilding it when the library
 * changes, timeouts, turning failures into readable errors, and parsing the
 * ranked output. It has no Swing dependencies, so it can be tested headless.
 */
public final class SearchEngineBridge {

  /**
   * One ranked result.
   *
   * @param rank     1 for the closest match
   * @param fileName the library file's name, as the engine reports it
   * @param distance the engine's distance to the query - lower is more similar
   */
  public record Match(int rank, String fileName, double distance) {

    /** @return true when the engine found no difference at all from the query */
    public boolean identical() {
      return distance == 0.0;
    }
  }

  /**
   * Feature types the engine computes directly from an image file, in the
   * order the editor offers them. ("dnn" is left out: it reads precomputed
   * embeddings keyed by library filename, so it cannot score an edited image.)
   */
  public static final List<String> FEATURES =
      List.of("custom", "hist", "multihist", "texture", "baseline");

  /** File types compute_features indexes (cbir_engine.cpp, isImage). */
  static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "tif", "ppm");

  // match prints:  "  %2zu.  %-16s  (dist %.4f)"
  private static final Pattern RESULT_LINE = Pattern.compile(
      "^\\s*(\\d+)\\.\\s+(.+?)\\s+\\(dist\\s+(-?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?)\\)\\s*$");

  private final Path computeFeatures;
  private final Path match;
  private final Path cacheDir;
  private final long timeoutSeconds;

  /**
   * @param engineDir      folder holding the built compute_features and match programs
   * @param cacheDir       folder for cached feature indexes (created when needed)
   * @param timeoutSeconds how long one engine program may run before it is stopped
   */
  public SearchEngineBridge(Path engineDir, Path cacheDir, long timeoutSeconds) {
    if (engineDir == null || cacheDir == null) {
      throw new IllegalArgumentException("engineDir and cacheDir are required");
    }
    if (timeoutSeconds <= 0) {
      throw new IllegalArgumentException("timeoutSeconds must be positive");
    }
    this.computeFeatures = engineDir.resolve("compute_features");
    this.match = engineDir.resolve("match");
    this.cacheDir = cacheDir;
    this.timeoutSeconds = timeoutSeconds;
  }

  /**
   * Ranks every image in {@code library} by similarity to {@code query}.
   *
   * @param query   the image to search with - any file the engine can read
   * @param library a folder of images to search
   * @param feature one of {@link #FEATURES}
   * @param count   how many matches to return
   * @return the matches, closest first
   * @throws SearchException if the search could not be completed
   */
  public List<Match> findSimilar(Path query, Path library, String feature, int count)
      throws SearchException {
    requireFeature(feature);
    if (count < 1) {
      throw new IllegalArgumentException("count must be at least 1");
    }
    if (query == null || !Files.isRegularFile(query)) {
      throw new SearchException("The query image could not be found: " + query);
    }
    Path index = index(library, feature);
    String output = run(List.of(match.toString(), query.toString(), feature,
        index.toString(), Integer.toString(count)));
    List<Match> results = parseMatches(output);
    if (results.isEmpty()) {
      throw new SearchException("The search engine returned no matches.");
    }
    return results;
  }

  /**
   * Returns the feature index for a library, building it only when it is
   * missing or the library has changed since it was built.
   *
   * @param library a folder of images
   * @param feature one of {@link #FEATURES}
   * @return the path of the feature CSV
   * @throws SearchException if the library is empty or indexing fails
   */
  public Path index(Path library, String feature) throws SearchException {
    requireFeature(feature);
    List<Path> images = listImages(library);
    if (images.isEmpty()) {
      throw new SearchException("No images (jpg, png, tif, ppm) were found in " + library);
    }
    Path csv = cacheDir.resolve(cacheKey(library) + "-" + feature + ".csv");
    if (isFresh(csv, images)) {
      return csv;
    }
    try {
      Files.createDirectories(cacheDir);
      Files.deleteIfExists(csv);
    } catch (IOException e) {
      throw new SearchException("Could not prepare the search cache at " + cacheDir, e);
    }
    run(List.of(computeFeatures.toString(), library.toString(), feature, csv.toString()));
    if (!Files.isRegularFile(csv)) {
      throw new SearchException("Indexing finished but produced no feature file.");
    }
    return csv;
  }

  /**
   * Parses the ranked lines printed by the engine's match program. Header
   * lines and anything else that is not a result are ignored.
   *
   * @param output everything match printed
   * @return the parsed matches, in the order printed
   */
  static List<Match> parseMatches(String output) {
    if (output == null || output.isEmpty()) {
      return Collections.emptyList();
    }
    List<Match> matches = new ArrayList<>();
    for (String line : output.split("\\R")) {
      Matcher m = RESULT_LINE.matcher(line);
      if (m.matches()) {
        matches.add(new Match(Integer.parseInt(m.group(1)), m.group(2),
            Double.parseDouble(m.group(3))));
      }
    }
    return matches;
  }

  /**
   * Lists the images in a library folder that the engine will index.
   *
   * @param library a folder of images
   * @return the image files, sorted by name
   * @throws SearchException if the folder cannot be read
   */
  static List<Path> listImages(Path library) throws SearchException {
    if (library == null || !Files.isDirectory(library)) {
      throw new SearchException("The photo library folder could not be found: " + library);
    }
    try (Stream<Path> files = Files.list(library)) {
      return files.filter(Files::isRegularFile)
          .filter(p -> !p.getFileName().toString().startsWith("."))
          .filter(p -> IMAGE_EXTENSIONS.contains(extension(p)))
          .sorted()
          .collect(Collectors.toList());
    } catch (IOException | UncheckedIOException e) {
      throw new SearchException("The photo library could not be read: " + library, e);
    }
  }

  /**
   * @param path any file path
   * @return its extension, lower-cased, or "" if it has none
   */
  static String extension(Path path) {
    String name = path.getFileName().toString();
    int dot = name.lastIndexOf('.');
    return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
  }

  // an index is fresh if it has one row per image and is newer than every image
  private static boolean isFresh(Path csv, List<Path> images) {
    try {
      if (!Files.isRegularFile(csv)) {
        return false;
      }
      long rows;
      try (Stream<String> lines = Files.lines(csv, StandardCharsets.UTF_8)) {
        rows = lines.filter(line -> !line.isBlank()).count();
      }
      if (rows != images.size()) {
        return false;
      }
      FileTime built = Files.getLastModifiedTime(csv);
      for (Path image : images) {
        if (Files.getLastModifiedTime(image).compareTo(built) > 0) {
          return false;
        }
      }
      return true;
    } catch (IOException | UncheckedIOException e) {
      return false;   // unreadable index: rebuild it
    }
  }

  // one cache file per library folder, keyed by its absolute path
  private static String cacheKey(Path library) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(
          library.toAbsolutePath().normalize().toString().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest, 0, 6);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is always available", e);
    }
  }

  private static void requireFeature(String feature) {
    if (!FEATURES.contains(feature)) {
      throw new IllegalArgumentException("Unknown feature type: " + feature
          + " (expected one of " + FEATURES + ")");
    }
  }

  // runs one engine program, returning its combined output
  private String run(List<String> command) throws SearchException {
    Path program = Path.of(command.get(0));
    String name = program.getFileName().toString();
    if (!Files.isExecutable(program)) {
      throw new SearchException("The search engine is not built (" + program
          + " is missing). Run `make engine` from the project root.");
    }
    Process process;
    try {
      process = new ProcessBuilder(command).redirectErrorStream(true).start();
    } catch (IOException e) {
      throw new SearchException("Could not start " + name + ": " + e.getMessage(), e);
    }
    // drain output on another thread so a chatty program can never block on a full pipe
    CompletableFuture<String> output = CompletableFuture.supplyAsync(() -> {
      try (InputStream in = process.getInputStream()) {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
    try {
      if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        throw new SearchException(name + " did not finish within " + timeoutSeconds + " s.");
      }
      String text = output.get(5, TimeUnit.SECONDS);
      if (process.exitValue() != 0) {
        throw new SearchException(name + " failed (exit code " + process.exitValue() + ")"
            + lastLine(text));
      }
      return text;
    } catch (InterruptedException e) {
      process.destroyForcibly();
      Thread.currentThread().interrupt();
      throw new SearchException("The search was interrupted.", e);
    } catch (ExecutionException | java.util.concurrent.TimeoutException e) {
      throw new SearchException("Could not read the output of " + name + ".", e);
    }
  }

  private static String lastLine(String text) {
    String trimmed = text == null ? "" : text.strip();
    if (trimmed.isEmpty()) {
      return ".";
    }
    String[] lines = trimmed.split("\\R");
    return ": " + lines[lines.length - 1];
  }
}
