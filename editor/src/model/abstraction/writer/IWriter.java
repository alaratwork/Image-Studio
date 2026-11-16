package model.abstraction.writer;

import model.image.IImage;

import java.io.IOException;

/**
 * Interface represents an image writer.
 * Allows for writing image data to different sources.
 * Writes image data to the image source that
 * will translate into an image file.
 */
public interface IWriter {

  /**
   * Writes image data to the image source.
   *
   * @param image the IImage object to be written
   * @param file_path the destination file path (including format extension)
   * @throws IllegalArgumentException if the image is null
   * @throws IOException if the write operation fails
   */
  void write(IImage image, String file_path) throws IllegalArgumentException, IOException;
}
