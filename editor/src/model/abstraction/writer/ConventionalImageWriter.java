package model.abstraction.writer;

import model.image.IImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This Writer class is used to write images conventionally.
 * It converts an IImage object into a BufferedImage object,
 * then saves the BufferedImage object to a file.
 */
public class ConventionalImageWriter implements IWriter {

  /**
   * Saves the given IImage to a file at the specified path in a supported format.
   *
   * @param image      the image to save
   * @param file_path  the path (including filename and extension) to save the image
   * @throws IOException if the file could not be saved or the format is unsupported
   */
  @Override
  public void write(IImage image, String file_path) throws IOException {
    // Create a new BufferedImage to hold the image's pixel data in RGB format
    BufferedImage buffered_image = new BufferedImage(
        image.getWidth(),
        image.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    // Loop through each pixel and convert it into RGB integer format
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int r = image.getPixel(y, x).getR();
        int g = image.getPixel(y, x).getG();
        int b = image.getPixel(y, x).getB();
        int rgb = (r << 16) | (g << 8) | b;
        buffered_image.setRGB(x, y, rgb);
      }
    }

    // Create a file output target
    File output_file = new File(file_path);

    // Determine the image format from the file extension
    String format_name = file_path.substring(file_path.lastIndexOf('.') + 1);

    // Write the BufferedImage to file in the specified format
    boolean success = ImageIO.write(buffered_image, format_name, output_file);

    // If writing fails, throw an exception
    if (!success) {
      throw new IOException("Unsupported file format: " + format_name);
    }
  }
}