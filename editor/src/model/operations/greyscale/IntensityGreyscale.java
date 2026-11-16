package model.operations.greyscale;

import model.image.Pixel;

/**
 * IntensityGreyscale is a concrete implementation of the GreyscaleAbstract class.
 */
public class IntensityGreyscale extends GreyscaleAbstract {
  /**
   * Calculates the grayscale intensity of a pixel.
   * @param pixel The pixel to calculate the grayscale intensity of.
   * @return The grayscale intensity of the pixel.
   */
  @Override
  protected int calculateGreyscale(Pixel pixel) {
    return pixel.intensity();
  }
}
