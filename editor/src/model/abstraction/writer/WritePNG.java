package model.abstraction.writer;

import model.image.IImage;
import model.abstraction.InputConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Class that writes an IImage to a PNG file.
 * Implements the IWriter interface.
 */
public class WritePNG implements IWriter {

  /**
   * Converts the IImage to BufferedImage and writes it to the file.
   *
   * @param image     the image to be saved
   * @param file_path the destination file path
   * @throws IllegalArgumentException if the image is null
   * @throws IOException              if writing to the file fails
   */
  @Override
  public void write(IImage image, String file_path) throws IllegalArgumentException, IOException {
    if (image == null) {
      throw new IllegalArgumentException("Cannot write null image.");
    }

    BufferedImage buffered_image = InputConverter.convertInput(image);
    File file = new File(file_path);
    ImageIO.write(buffered_image, "png", file);
  }
}
