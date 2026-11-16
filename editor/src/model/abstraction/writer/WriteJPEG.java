package model.abstraction.writer;

import model.image.IImage;
import model.abstraction.InputConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * WriteJPEG implements IWriter.
 * Converts an IImage to BufferedImage and writes it to a JPEG file.
 */
public class WriteJPEG implements IWriter {

  @Override
  public void write(IImage image, String file_path) throws IllegalArgumentException, IOException {
    if (image == null) {
      throw new IllegalArgumentException("Cannot write null image.");
    }

    BufferedImage buffered_image = InputConverter.convertInput(image);
    File file = new File(file_path);
    ImageIO.write(buffered_image, "jpeg", file);
  }
}
