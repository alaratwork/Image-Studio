package model.operations.light;

import model.image.IImage;
import model.image.Image;
import model.image.Pixel;
import model.operations.IOperation;

/**
 * Darkening operation on an image object.
 * Follows IOperation interface and specs.
 * Each pixel R, G, B values are decreased by
 * a given amount via pixel objects.
 */
public class Darken implements IOperation {
  private final int darken;

  public Darken(int darken) {
    this.darken = darken;
  }

  /**
   * Performs the operation on the image.
   *
   * @param image IImage object to be operated on
   * @throws IllegalArgumentException if the image is null
   *                                  returns an IImage object after the operation is performed
   */
  @Override
  public IImage apply(IImage image) throws IllegalArgumentException {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null.");
    }
    // * Darkening will be on a new object
    //    Copy dimensions and pixels
    IImage darkened_image = new Image(image.getWidth(), image.getHeight());
    // loop and iterate through width and height pixels
    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        Pixel pixel = image.getPixel(i, j);
        // * Decrease the pixel R, G, B values by the brightness
        int r_dark = pixel.getR() - darken;
        int g_dark = pixel.getG() - darken;
        int b_dark = pixel.getB() - darken;

        // * Clamp the values to be between 0 and 255
        //    Create a new pixel object with the new values
        Pixel brightened_pixel = new Pixel(r_dark, g_dark, b_dark);

        // * Set the new pixel object to the new image
        darkened_image.setPixel(i, j, brightened_pixel);
        // * Return the new image object
      }
    }
    return darkened_image;
  }
}