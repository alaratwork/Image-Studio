package model.abstraction.reader;

import model.image.IImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;

/**
 * Interface for reading image data and constructing an image object.
 */
public interface IReader {
  /**
   * Read image data from the given input stream and construct an image object.
   * @return the IImage object read from the source
   * @throws IllegalArgumentException if the image source is invalid
   * @throws InputMismatchException if the image data is malformed
   * @throws IOException if reading the file fails
   */
  IImage read()
      throws IllegalArgumentException, InputMismatchException, IOException;
}