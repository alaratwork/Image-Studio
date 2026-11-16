package model.operations.greyscale;

import model.image.Pixel;

/**
 * GreenGreyscale is a concrete implementation of the GreyscaleAbstract class.
 * This class calculates the grayscale value of a pixel based on its green (G) component.
 */
public class GreenGreyscale extends GreyscaleAbstract {

  @Override
  protected int calculateGreyscale(Pixel pixel) {
    return pixel.getG();
  }
}
