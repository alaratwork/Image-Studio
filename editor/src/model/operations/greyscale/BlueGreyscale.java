package model.operations.greyscale;

import model.image.Pixel;

/**
 * BlueGreyscale is a concrete implementation of the GreyscaleAbstract class.
 * This class calculates the grayscale value of a pixel based on its blue (B) component.
 */
public class BlueGreyscale extends GreyscaleAbstract {
  @Override
  protected int calculateGreyscale(Pixel pixel) {
    return pixel.getB();
  }
}
