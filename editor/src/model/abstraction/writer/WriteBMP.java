package model.abstraction.writer;

import model.image.IImage;
import model.abstraction.InputConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * WriteBMP implements IWriter.
 * Converts an IImage to BufferedImage and writes it to a BMP file.
 */
public class WriteBMP implements IWriter {

  /**
   * Writes image data to the image source.
   *
   * @param image     the IImage object to be written
   * @param file_path the destination file path (including format extension)
   * @throws IllegalArgumentException if the image is null
   * @throws IOException              if the write operation fails
   */
  @Override
  public void write(IImage image, String file_path) throws IllegalArgumentException, IOException {
    if (image == null) {
      throw new IllegalArgumentException("Cannot write null image.");
    }

    BufferedImage buffered_image = InputConverter.convertInput(image);
    File file = new File(file_path);
    ImageIO.write(buffered_image, "bmp", file);
  }
}
