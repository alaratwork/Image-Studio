package model.histogram;

/**
 * Represents a histogram of pixel intensity and RGB channels.
 * Stores frequency distribution of pixel values.
 * It also calculates and provides the maximum
 * frequency across all channels and intensity values.
 */
public class Histogram {
  private final int[] red_pix;
  private final int[] green_pix;
  private final int[] blue_pix;
  private final int[] intensity_value;
  private final int maximum_value;

  /**
   * Constructs a Histogram object.
   * Represents the frequency distribution.
   * Stores the maximum frequency among all provided values.
   *
   * @param red_pix an array of integers representing
   *                the frequency of red pixel values (0-255)
   * @param green_pix an array of integers representing
   *                  the frequency of green pixel values (0-255)
   * @param blue_pix an array of integers representing
   *                 the frequency of blue pixel values (0-255)
   * @param intensity_value an array of integers representing
   *                        the frequency of intensity values (0-255)
   */
  public Histogram(int[] red_pix, int[] green_pix,
                   int[] blue_pix, int[] intensity_value) {
    this.red_pix = red_pix;
    this.green_pix = green_pix;
    this.blue_pix = blue_pix;
    this.intensity_value = intensity_value;
    this.maximum_value = imageMaxFrequency();
  }

  /**
   * Calculates the maximum frequency.
   * Across all color channels (red, green, blue).
   * And intensity values in the histogram.
   * @return the maximum frequency value
   */
  private int imageMaxFrequency() {
    int max = 0;
    // loop through each pixel and find the max value
    for (int i = 0; i < 256; i++) {
      if (red_pix[i] > max) {
        max = red_pix[i];
      }
      if (green_pix[i] > max) {
        max = green_pix[i];
      }
      if (blue_pix[i] > max) {
        max = blue_pix[i];
      }
      if (intensity_value[i] > max) {
        max = intensity_value[i];
      }
    }
    return max;
  }

  /**
   * Returns the red pixel array.
   * @return red pixel array
   */
  public int[] getRedPix() {
    return red_pix;
  }

  /**
   * Returns the green pixel array.
   * @return green pixel array
   */
  public int[] getGreenPix() {
    return green_pix;
  }

  /**
   * Returns the blue pixel array.
   * @return blue pixel array
   */
  public int[] getBluePix() {
    return blue_pix;
  }

  /**
   * Returns the intensity value array.
   * @return intensity value array
   */
  public int[] getIntensityValue() {
    return intensity_value;
  }

  /**
   * Returns the maximum value across all channels.
   * @return maximum value int
   */
  public int getMaximumValue() {
    return maximum_value;
  }
}