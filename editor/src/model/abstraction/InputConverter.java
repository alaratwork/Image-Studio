package model.abstraction;

import model.image.IImage;
import model.image.Image;
import model.image.Pixel;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Converter class for converting user input.
 * Takes a file from input and returns an IImage object.
 * IImage object is then connected to a reader class.
 * Returns a BufferedImage object, part of the ImageIO library.
 * Step 1 of 2 for converting user input.
 * IImage extends ImageIO
 */
public class InputConverter {
  public static BufferedImage convertInput(IImage image) throws IllegalArgumentException {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null.");
    }
    // the buffered image will act as a bridge between the input and image object
    // will homogenize the image data
    // type int rgb is the default type for buffered images
    BufferedImage buffered_image = new BufferedImage(image.getWidth(),
        image.getHeight(), BufferedImage.TYPE_INT_RGB);
    // we are copying over the data exactly, so we loop through all the pixels
    // to translate them into the buffered image
    for (int i = 0; i < image.getHeight(); i++) {
      for (int j = 0; j < image.getWidth(); j++) {
        Pixel pixel = image.getPixel(i, j); // get pixels from image via pixel object
        // set the rgb values for the buffered image
        // Color Class / getRGB() method combines the rgb values into an integer
        // https://docs.oracle.com/javase/8/docs/api/java/awt/Color.html
        // a pixel object has r, g, b values
        int buffer_rgb = new Color(pixel.getR(), pixel.getG(),
            pixel.getB()).getRGB();
        // set extracted rgb values into the buffered image
        buffered_image.setRGB(j, i, buffer_rgb);
      }
    }
    return buffered_image;
  }
  /*
    * Converts a buffered image into an IImage object.
   */
  public static IImage convertBufferedImage(BufferedImage buffered_image)
      throws IllegalArgumentException {
    if (buffered_image == null) {
      throw new IllegalArgumentException("Buffered image cannot be null.");
    }
    // set up IImage object with BufferedImage dimensions
    // use a pixel array to store rgb values
    int width = buffered_image.getWidth();
    int height = buffered_image.getHeight();
    Pixel[][] pixels = new Pixel[height][width];

    // iterate over each pixel in buffered image
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        int rgb = buffered_image.getRGB(j, i); // extract rgb values from buffered image
        // extract individual rgb values
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        // create a pixel object with the rgb values
        pixels[i][j] = new Pixel(r, g, b);
      }
    }
    // return an Image object with the buffered image dimensions and pixel array
    return new Image(width, height, pixels);
  }
}