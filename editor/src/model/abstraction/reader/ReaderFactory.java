package model.abstraction.reader;

import java.io.InputStream;

/**
 * (Factory Pattern)
 * This class is used to create image readers.
 * Provides a centralized point of creation for
 * the correct IReader implementation for a given file.
 * Takes a file path as an argument and an input stream.
 * Returns the object representation of the image reader.
 */
public class ReaderFactory {

  /**
   * This will delegate each input to its respective reader.
   *
   * @param filePath    - the path of the file to be read
   * @param inputStream - the input stream of the file to be read
   * @return - the IReader object for the given file
   */
  public static IReader createReader(String filePath, InputStream inputStream)
      throws IllegalArgumentException {
    if (filePath == null || inputStream == null) {
      throw new IllegalArgumentException("File path or input stream cannot be null.");
    }

    // Call helper method below to extract the extension,
    // then use the extension to determine the correct reader
    String fileType = extractExtension(filePath);
    return switch (fileType) {
      case "ppm" -> new ReadPPM(inputStream);
      case "png" -> new ReadPNG(inputStream);
      case "jpeg", "jpg" -> new ReadJPEG(inputStream);
      case "bmp" -> new ReadBMP(inputStream);
      default -> throw new IllegalArgumentException("File type not supported.");
    };
  }

  /**
   * This private helper method will extract the file extension.
   * Calls in createReader() to determine the correct reader.
   * Returns a String representing the file extension,
   * which may be used in other methods.
   *
   * @param filePath the path of the file to be read
   * @return the file extension as a String
   */
  private static String extractExtension(String filePath) {
    int period = filePath.lastIndexOf('.');
    if (period == -1) {
      return "";
    } else {
      return filePath.substring(period + 1).toLowerCase();
    }
  }

  /**
   * Creates an IReader from a file path by opening a FileInputStream.
   *
   * @param filePath - the path of the file to be read
   * @return - the IReader object for the given file
   * @throws IllegalArgumentException if the file cannot be read
   */
  public static IReader getReader(String filePath) {
    try {
      InputStream inputStream = new java.io.FileInputStream(filePath);
      return createReader(filePath, inputStream);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error reading file: " + e.getMessage());
    }
  }
}