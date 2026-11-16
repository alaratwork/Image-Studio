package model.image;

/**
 * Pixel class represents a single pixel.
 * With R, G, B components.
 * R: Red, G: Green, B: Blue.
 * Each component has a value between 0 and 255.
 * Class also includes 3 operations for calculating
 * brightness values where the R, G, B components are manipulated.
 */

public class Pixel {

  private final int r;
  private final int g;
  private final int b;

  /**
   * Constructor for a Pixel.
   * Creates the object with given RGB values.
   * Clamps the value to be between 0 and 255.
   * References Clamp Helper Method.
   * @param r Red value int
   * @param g Green value in
   * @param b Blue value int
   */
  public Pixel(int r, int g, int b) {
    this.r = clamp(r);
    this.g = clamp(g);
    this.b = clamp(b);
  }

  /**
   * Helper method for Pixel constructor.
   * Clamps the value to be between 0 and 255.
   * @param value rgb value to be clamped
   * @return clamped rgb value int
   */
  private int clamp(int value) {
    return Math.max(0, Math.min(255, value));
  }

  /**
   * Getter for an r/Red component.
   * @return r component int
   */
  public int getR() {
    return r;
  }

  /**
   * Getter for a g/Green component.
   * @return g component int
   */
  public int getG() {
    return g;
  }

  /**
   * Getter for a b/Blue component.
   * @return b component int
   */
  public int getB() {
    return b;
  }

  /**
   * Brightness Operation I.
   * Calculates the Value for a pixel.
   * Defined as the MAXIMUM of the RGB components.
   * @return value int
   */
  public int value() {
    return Math.max(r, Math.max(g, b));
  }

  /**
   * Brightness Operation II.
   * Calculates the Intensity of a pixel.
   * Defined as the AVERAGE of the RGB components.
   * @return intensity int
   */
  public int intensity() {
    return (r + g + b) / 3;
  }

  /**
   * Brightness Operation III.
   * Calculates the Luma of a pixel.
   * Defined as the WEIGHTED SUM of the RGB components.
   * Rounds to the nearest integer.
   * @return luma int
   */
  public int luma() {
    return Math.toIntExact(Math.round((r * 0.2126)
        + (g * 0.7152) + (b * 0.0722)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Pixel)) {
      return false;
    }
    Pixel pixel = (Pixel) o;
    return  r == pixel.r &&
            g == pixel.g &&
            b == pixel.b;
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(r, g, b);
  }

  @Override
  public String toString() {
    return "(" + r + "," + g + "," + b + ")";
  }
}