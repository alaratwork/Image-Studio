package controller.commands;

import model.operations.IOperation;
import model.operations.greyscale.RedGreyscale;

/**
 * Command to convert an image to greyscale using its red color component.
 * Applies the RedGreyscale operation and stores the result with a new image name.
 */
public class RedGreyscaleCommand extends AbstractGreyscaleCommand {

  /**
   * Constructs a RedGreyscaleCommand with the given source and destination image names.
   *
   * @param source_image_name the name of the source image in the model
   * @param dest_image_name   the name to assign to the resulting greyscale image
   * @throws IllegalArgumentException if either name is null or empty
   */
  public RedGreyscaleCommand(String source_image_name, String dest_image_name) {
    super(source_image_name, dest_image_name);
  }

  /**
   * Returns the RedGreyscale operation to apply to the image.
   * @return the operation to apply
   */
  @Override
  protected IOperation createOperation() {
    return new RedGreyscale();
  }
}
