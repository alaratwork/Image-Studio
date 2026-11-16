package model.abstraction.reader;

import model.image.IImage;
import model.abstraction.InputConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class will be used to read PNG image files.
 * Implements the IReader interface and uses ImageIO.
 */
public class ReadPNG implements IReader {
  private final InputStream input_stream;

  /**
   * Constructor for ReadPNG.
   * @param input_stream the input stream of the PNG image file
   */
  public ReadPNG(InputStream input_stream) {
    this.input_stream = input_stream;
  }

  /**
   * This method reads a PNG file and returns an IImage.
   * @return the converted IImage
   * @throws IOException if the image fails to load
   */
  @Override
  public IImage read() throws IOException {
    BufferedImage buffered_image = ImageIO.read(input_stream);
    return InputConverter.convertBufferedImage(buffered_image);
  }
}
