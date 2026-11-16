package model.operations.greyscale;

import model.image.IImage;
import model.image.Image;
import model.image.Pixel;
import model.operations.IOperation;

/**
 * Abstract class for Grayscale operations.
 * Follows IOperation interface and specs.
 * Each pixel R, G, B values are set to the same value.
 * Hooks into the apply() method in IOperation.
 * Implements an abstract method for calculating the grayscale value.
 */
public abstract class GreyscaleAbstract implements IOperation {
  protected abstract int calculateGreyscale(Pixel pixel);

  /**
   * Applies the operation to the image.
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
    // * create a new pixel array, fill it with grayscale pixels, and construct the image
    Pixel[][] newPixels = new Pixel[image.getHeight()][image.getWidth()];
    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        Pixel pixel = image.getPixel(i, j);
        int grayscale = calculateGreyscale(pixel);
        newPixels[i][j] = new Pixel(grayscale, grayscale, grayscale);
      }
    }
    IImage grayscale_image = new Image(image.getWidth(), image.getHeight(), newPixels);
    return grayscale_image;
  }
}