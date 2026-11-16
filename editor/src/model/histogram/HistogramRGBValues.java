package model.histogram;
import model.image.IImage;
import model.image.Pixel;

/**
 * Calculates the histogram of RGB values and intensity.
 * Reads each pixel in the image to record the
 * frequency of occurrence for each.
 */
public class HistogramRGBValues {
  /**
   * Calculates the histogram of RGB values for the given image.
   * @param image image object to be processed
   * @return histogram object
   */
  public static Histogram calculateValues(IImage image) {
    // initialize array fields that will store individual rgb values
    int[] red = new int[256];
    int[] green = new int[256];
    int[] blue = new int[256];
    int[] intensity = new int[256];
    // loop through pixels and populate the rgb values
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        Pixel pixel = image.getPixel(y, x);
        // frequency count for the corresponding value in each channel
        red[pixel.getR()]++;
        green[pixel.getG()]++;
        blue[pixel.getB()]++;
        intensity[pixel.intensity()]++;
      }
    }
    // return a new histogram object with the calculated frequencies
    return new Histogram(red, green, blue, intensity);
  }
}
