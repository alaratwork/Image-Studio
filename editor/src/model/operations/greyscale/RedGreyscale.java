package model.operations.greyscale;

import model.image.Pixel;

/**
 * RedGrayscale is a concrete implementation of the GreyscaleAbstract class.
 * This class calculates the grayscale value of a pixel based on its red (R) component.
 */
public class RedGreyscale extends GreyscaleAbstract {

  @Override
  // * Extracts the red component of the pixel as the grayscale value.
  protected int calculateGreyscale(Pixel pixel) {
    return pixel.getR();
  }
}
