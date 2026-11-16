package model.abstraction.reader;

import model.image.IImage;
import model.abstraction.InputConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class reads BMP image files and converts them to IImage format.
 * It implements the IReader interface and uses ImageIO for reading.
 */
public class ReadBMP implements IReader {
  private final InputStream inputStream;

  /**
   * Constructs a ReadBMP with the given input stream.
   *
   * @param inputStream the input stream of the BMP image file
   */
  public ReadBMP(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * Reads a BMP image file from the input stream and returns it as an IImage.
   *
   * @return the converted IImage
   * @throws IOException if the image fails to load
   */
  @Override
  public IImage read() throws IOException {
    BufferedImage bufferedImage = ImageIO.read(inputStream);
    return InputConverter.convertBufferedImage(bufferedImage);
  }
}
