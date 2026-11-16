package model.image;

/**
 * Interface represents an image.
 * Has methods for getting width and height.
 * Stores the image with Pixel objects
 * when implemented by a concrete class.
 * Stores the row and column of the pixel.
 * Stores methods that are used by Image Operations.
 */
public interface IImage {

  /**
   * Getter for the width of the image.
   * In Pixel objects,
   * the width is represented by the x coordinate.
   * (Row)
   * @return width int
   */
  int getWidth();

  /**
   * Getter for the height of the image.
   * In Pixel objects,
   * the height is represented by the y coordinate.
   * (Column)
   * @return height int
   */
  int getHeight();

  /**
   * Returns a Pixel object at the given row and column.
   * @param row row of the pixel
   * @param column column of the pixel
   * @return Pixel object
   * @throws IllegalArgumentException if row or column is out of bounds
   */
  Pixel getPixel(int row, int column)
      throws IllegalArgumentException;

  /**
   * Sets a Pixel object at the given row and column.
   * Can change the components of a Pixel object.
   * This method is created for Image Operations.
   * It will be used to change and edit values, and
   * called in a nested loop for the apply() operation
   * within another interface.
   * @param row row of the pixel
   * @param column column of the pixel
   * @param pixel Pixel object to be set
   * @throws IllegalArgumentException if row or column is out of bounds
  */
  public void setPixel(int row, int column, Pixel pixel)
      throws IllegalArgumentException;

  /**
   * Returns a copy of the edited image.
   * Copies the original Image and its
   * respective pixel values before performing
   * any image operations.
   */
  IImage editedImage();
}