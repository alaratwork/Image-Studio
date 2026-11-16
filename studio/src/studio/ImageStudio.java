package studio;

import controller.ImageDatabase;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import view.View;
import view.ViewController;

/**
 * Entry point for Image Studio: the original editor, wired exactly as
 * {@code IMEProgram} wires it, plus a Search menu backed by the C++ engine.
 *
 * <p>Locations can be overridden with system properties, all relative to the
 * working directory by default (the project root, when run via {@code make run-studio}):
 * <ul>
 *   <li>{@code studio.engine} - folder with the built engine (default {@code search-engine})</li>
 *   <li>{@code studio.library} - photo library to search (default {@code samples/library})</li>
 *   <li>{@code studio.cache} - where feature indexes are kept (default {@code build/search-cache})</li>
 * </ul>
 */
public final class ImageStudio {

  private static final long ENGINE_TIMEOUT_SECONDS = 120;

  private ImageStudio() {
  }

  /**
   * @param args unused
   */
  public static void main(String[] args) {
    Path engine = Path.of(System.getProperty("studio.engine", "search-engine"));
    Path library = Path.of(System.getProperty("studio.library", "samples/library"));
    Path cache = Path.of(System.getProperty("studio.cache", "build/search-cache"));
    SearchEngineBridge bridge = new SearchEngineBridge(engine, cache, ENGINE_TIMEOUT_SECONDS);

    SwingUtilities.invokeLater(() -> {
      ImageDatabase database = new ImageDatabase();
      View view = new View();
      new ViewController(database, view);
      view.setTitle("Image Studio");
      SearchMenu.install(view, database, bridge, library);
    });
  }
}
