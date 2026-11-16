package model.operations.greyscale;

import model.image.Pixel;

/**
 * LumaGreyscale is a concrete implementation of the GreyscaleAbstract class.
 */
public class LumaGreyscale extends GreyscaleAbstract {
  /**
   * Calculates the grayscale value of a pixel based on its luma (Y) component.
   * @param pixel The pixel to calculate the grayscale value of.
   * @return The grayscale value of the pixel.
   */
  @Override
  protected int calculateGreyscale(Pixel pixel) {
    return pixel.luma();
  }
}
