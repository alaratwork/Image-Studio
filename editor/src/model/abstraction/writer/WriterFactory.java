package model.abstraction.writer;

/**
 * This factory class is responsible for returning the appropriate image writer
 * based on the file format of the path provided.
 */
public class WriterFactory {

  /**
   * Returns a writer instance based on the file extension of the given path.
   *
   * @param path the full file path including its extension
   * @return the corresponding IWriter implementation
   * @throws IllegalArgumentException if the file format is unsupported
   */
  public static IWriter getWriter(String path) throws IllegalArgumentException {
    // Validate input
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty.");
    }

    // Convert path to lowercase for case-insensitive comparison
    String extension = path.toLowerCase();

    // Check extension and return the appropriate writer
    if (extension.endsWith(".ppm")) {
      return new WritePPM();
    } else if (extension.endsWith(".png") || extension.endsWith(".jpg")
            || extension.endsWith(".jpeg") || extension.endsWith(".bmp")) {
      return new ConventionalImageWriter();
    } else {
      // Throw if unsupported format
      throw new IllegalArgumentException("Unsupported file format for writing: " + path);
    }
  }
}
