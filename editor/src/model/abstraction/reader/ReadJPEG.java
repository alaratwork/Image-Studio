package model.abstraction.reader;

import model.image.IImage;
import model.abstraction.InputConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;

/**
 * This class is used to read JPEG image files.
 * Implements the IReader interface and uses ImageIO.
 */
public class ReadJPEG implements IReader {
  private final InputStream input_stream;

  /**
   * Constructor for ReadJPEG.
   * @param input_stream the input stream of the JPEG image file
   */
  public ReadJPEG(InputStream input_stream) {
    this.input_stream = input_stream;
  }

  /**
   * This method reads a JPEG file and returns an IImage.
   * @return the converted IImage
   * @throws IllegalArgumentException if the argument is invalid
   * @throws InputMismatchException if the input does not match expected format
   * @throws IOException if the image fails to load
   */
  @Override
  public IImage read() throws IllegalArgumentException, InputMismatchException, IOException {
    BufferedImage buffered_image = ImageIO.read(this.input_stream);
    return InputConverter.convertBufferedImage(buffered_image);
  }
}
