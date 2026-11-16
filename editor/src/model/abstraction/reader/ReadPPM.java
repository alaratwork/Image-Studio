package model.abstraction.reader;

import model.image.IImage;
import model.image.Image;
import model.image.Pixel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * The ReadPPM class is responsible for reading PPM (Portable PixMap) image files
 * and parsing their contents into an IImage object representation.
 * Implements the IReader interface to adhere to the standard image reading abstraction.
 */
public class ReadPPM implements IReader {

  private final InputStream inputStream;

  public ReadPPM(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * Reads a PPM image from the provided InputStream and constructs an IImage object.
   *
   * @return the IImage object parsed from the input stream
   * @throws IOException              if file access fails
   * @throws IllegalArgumentException if the file is malformed or contains invalid data
   */
  @Override
  public IImage read() throws IOException {
    Scanner scanner = new Scanner(new InputStreamReader(this.inputStream));
    StringBuilder string = new StringBuilder();

    // * Scan Process 1 - file to string
    // read the file line by line, remove comments and whitespace
    while (scanner.hasNextLine()) {
      String cont = scanner.nextLine();
      if (cont.length() > 0 && cont.charAt(0) != '#') {
        string.append(cont).append(System.lineSeparator());
      }
    }

    // * Scan Process 2 - string to analyze
    // parse built string to uncover pixel data
    scanner = new Scanner(string.toString());

    try {
      // read PPM magic number P3
      String token = scanner.next();
      if (!token.equals("P3")) {
        throw new IllegalArgumentException("Invalid PPM file: missing P3 header.");
      }

      // parse image dimensions
      int width = scanner.nextInt();
      int height = scanner.nextInt();

      if (width < 1 || height < 1) {
        throw new IllegalArgumentException("Invalid dimensions.");
      }

      int max = scanner.nextInt();
      Pixel[][] pixels = new Pixel[height][width];

      // read pixel components
      for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
          int r = scanner.nextInt();
          int g = scanner.nextInt();
          int b = scanner.nextInt();
          pixels[i][j] = new Pixel(r, g, b);
        }
      }

      return (IImage) new Image(width, height, pixels);
    } catch (RuntimeException e) {
      if (e instanceof NoSuchElementException) {
        throw new IllegalArgumentException("Invalid PPM file: incomplete or malformed data.", e);
      } else if (e instanceof InputMismatchException) {
        throw new IllegalArgumentException("Invalid PPM file: non-integer pixel component found.", e);
      } else {
        throw new IllegalArgumentException("An unexpected error occurred during PPM parsing: "
            + e.getMessage(), e);
      }
    } finally {
      scanner.close();
    }
  }
}
