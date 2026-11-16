package model.operations.greyscale;

import model.image.Pixel;

/**
 * A concrete implementation of a greyscale operation that extends {@link GreyscaleAbstract}.
 * This operation calculates the greyscale value of a pixel based on its "value" component,
 * which is the maximum of its red, green, and blue components.
 */
public class ValueGreyscale extends GreyscaleAbstract {

  /**
   * Calculates the greyscale component for a pixel based on its value (maximum RGB component).
   *
   * @param pixel The original pixel.
   * @return The value component of the pixel.
   */
  @Override
  protected int calculateGreyscale(Pixel pixel) {
    return pixel.value();
  }
}
