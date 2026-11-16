package model.operations.light;

import model.image.IImage;
import model.image.Image;
import model.image.Pixel;
import model.operations.IOperation;

/**
 * Brightening operation on an image object.
 * Follows IOperation interface and specs.
 * Each pixel R, G, B values are increased by
 * a given amount via pixel objects.
 */
public class Brighten implements IOperation {
  private final int brightness;

  /**
   * Constructor for a Brighten operation.
   *
   * @param brightness amount to brighten the image by
   */
  public Brighten(int brightness) {
    this.brightness = brightness;
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
    // * Brightening will be on a new object
    //    Copy dimensions and pixels
    IImage brightened_image = new Image(image.getWidth(), image.getHeight());
    // loop and iterate through width and height pixels
    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        Pixel pixel = image.getPixel(i, j);
        // * Increase the pixel R, G, B values by the brightness
        int r_brightened = pixel.getR() + brightness;
        int g_brightened = pixel.getG() + brightness;
        int b_brightened = pixel.getB() + brightness;

        // * Clamp the values to be between 0 and 255
        //    Create a new pixel object with the new values
        Pixel brightened_pixel = new Pixel(r_brightened, g_brightened, b_brightened);

        // * Set the new pixel object to the new image
        brightened_image.setPixel(i, j, brightened_pixel);
        // * Return the new image object
      }
    }
    return brightened_image;
  }
}