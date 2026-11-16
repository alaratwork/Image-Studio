package model.abstraction.writer;

import model.image.IImage;
import model.image.Pixel;

import java.io.IOException;

/**
 * WritePPM class represents a PPM image writer.
 * This class implements the IWriter interface.
 * Takes an Appendable object which is used to write image data.
 */

import java.io.FileWriter;

/**
 * WritePPM class represents a PPM image writer.
 * This class implements the IWriter interface.
 * It writes the IImage data to a file in PPM (P3) format.
 */
public class WritePPM implements IWriter {

  /**
   * Writes image data to the image source.
   *
   * @param image     the IImage object to be written
   * @param file_path the destination file path
   * @throws IllegalArgumentException if the image is null
   * @throws IOException              if the write operation fails
   */
  @Override
  public void write(IImage image, String file_path) throws IllegalArgumentException, IOException {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null.");
    }

    try (FileWriter writer = new FileWriter(file_path)) {
      writer.write("P3\n");
      writer.write(String.format("%d %d\n", image.getWidth(), image.getHeight()));
      writer.write("255\n");

      for (int i = 0; i < image.getHeight(); i++) {
        for (int j = 0; j < image.getWidth(); j++) {
          Pixel pixel = image.getPixel(i, j);
          writer.write(String.format("%d\n%d\n%d\n", pixel.getR(), pixel.getG(), pixel.getB()));
        }
      }
    } catch (IOException e) {
      throw new IOException("Error writing to file.");
    }
  }
}
