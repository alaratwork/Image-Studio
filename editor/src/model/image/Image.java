package model.image;

/**
 * Represents an image, implements IImage.
 * Represents the image as a 2D array of Pixel objects.
 */

public class Image implements IImage {

  private final int width;
  private final int height;
  private final Pixel[][] pixels;

  /**
   * Constructor I for an Image.
   * Loads the image FROM A FILE.
   * Creates an Image object with a given width and height.
   * Made up of Pixel objects.
   * @param width  width of the image
   * @param height height of the image
   * @param pixels 2D array of Pixel objects
   * @throws IllegalArgumentException if height or width is less than 1
   *                                 if pixels are null
   */
  public Image(int width, int height, Pixel[][] pixels) throws IllegalArgumentException {
    if (width < 1 || height < 1) {
      throw new IllegalArgumentException("Width and height must "
          + "be greater than 0.");
    }
    if (pixels == null) {
      throw new IllegalArgumentException("Pixels cannot be null.");
    }
    // Set instances
    this.width = width;
    this.height = height;

    // * To perform any pixel editing operation, a
    //   copy of the original image will be used,
    //   thus we copy over all the pixel data into
    //   a new array to manipulate the object.
    this.pixels = new Pixel[height][width];
    // * A new pixel is created for each pixel in height
    //   A new pixel is created for each pixel in width
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (pixels[i][j] == null) {
          throw new IllegalArgumentException("Null pixel.");
        }
        this.pixels[i][j] = pixels[i][j];
      }
    }
  }

  /**
   * Constructor II for an Image.
   * CREATES AN EMPTY IMAGE with the given dimensions.
   * @param width  width of the image
   * @param height height of the image
   * @throws IllegalArgumentException if height or width is less than 1
   */
  public Image(int width, int height) throws IllegalArgumentException {
    if (width < 1 || height < 1) {
      throw new IllegalArgumentException("Width and height must " +
          "be greater than 0.");
    }
    // Set instances
    this.width = width;
    this.height = height;
    this.pixels = new Pixel[height][width];
    // * To perform any pixel editing operation, a
    //   copy of the original image will be used,
    //   thus we copy over all the pixel data into
    //   a new array to manipulate the object.
    // * A new pixel is created for each pixel in height
    for (int i = 0; i < height; i++) {
      // * A new pixel is created for each pixel in width
      for (int j = 0; j < width; j++) {
        // * Since a blank image is being created,
        //   all pixel RGN values in the new array are
        //   set to 0, which corresponds to a black image
        this.pixels[i][j] = new Pixel(0, 0, 0);
      }
    }
  }

  /**
   * Getter for the width of the image.
   * In Pixel objects,
   * the width is represented by the x coordinate.
   * (Row)
   *
   * @return width int
   */
  @Override
  public int getWidth() {
    return this.width;
  }

  /**
   * Getter for the height of the image.
   * In Pixel objects,
   * the height is represented by the y coordinate.
   * (Column)
   *
   * @return height int
   */
  @Override
  public int getHeight() {
    return this.height;
  }

  /**
   * Returns a Pixel object at the given row and column.
   *
   * @param row    row of the pixel
   * @param column column of the pixel
   * @return Pixel object
   * @throws IllegalArgumentException if row or column is out of bounds
   */
  @Override
  public Pixel getPixel(int row, int column) throws IllegalArgumentException {
    if (row < 0 || row >= height || column < 0 || column >= width) {
      throw new IllegalArgumentException("Out of bounds.");
    }
    return this.pixels[row][column];
  }

  /**
   * Sets a Pixel object at the given row and column.
   * Can change the components of a Pixel object.
   * This method is created for Image Operations.
   * It will be used to change and edit values, and
   * called in a nested loop for the apply() operation
   * within another interface.
   * @param row    row of the pixel
   * @param column column of the pixel
   * @param pixel  Pixel object to be set
   * @throws IllegalArgumentException if row or column is out of bounds
   *                                 if pixel is null
   */
  @Override
  public void setPixel(int row, int column, Pixel pixel) throws IllegalArgumentException {
    if (row < 0 || row >= height || column < 0 || column >= width) {
      throw new IllegalArgumentException("Out of bounds.");
    }
    if (pixel == null) {
      throw new IllegalArgumentException("Null pixel.");
    }
    // * Set the pixel at the given row and column
    this.pixels[row][column] = pixel;
  }

  /**
   * Returns a copy of the edited image.
   * Copies the original Image and its
   * respective pixel values before performing
   * any image operations.
   */
  @Override
  public IImage editedImage() {
    // * To create a copy of 'this',
    //   create a new copy array of pixels
    Pixel[][] edited = new Pixel[this.height][this.width];
    // loop through each pixel for height and width
    for (int i = 0; i < this.height; i++) {
      for (int j = 0; j < this.width; j++) {
        // * Copy the pixel data from the original array
        //   to the new array
        edited[i][j] = new Pixel(this.pixels[i][j].getR(),
            this.pixels[i][j].getG(), this.pixels[i][j].getB());
      }
    }
    // * Return the new copy of the image
    return new Image(this.width, this.height, edited);
  }
}