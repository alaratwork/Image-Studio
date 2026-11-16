package controller.commands;

import model.operations.IOperation;
import model.operations.greyscale.LumaGreyscale;

/**
 * Command to create a greyscale image using the luma component
 * of the source image. The result is stored under a new image name.
 */
/**
 * Command to apply the luma-based greyscale operation using the LumaGreyscale class.
 */
public class LumaGreyscaleCommand extends AbstractGreyscaleCommand {

  /**
   * Constructs a LumaGreyscaleCommand with the given source and destination image names.
   *
   * @param source_image_name the name of the source image in the model
   * @param dest_image_name the name to assign to the resulting greyscale image
   * @throws IllegalArgumentException if either name is null or empty
   */
  public LumaGreyscaleCommand(String source_image_name, String dest_image_name) {
    super(source_image_name, dest_image_name);
  }

  /**
   * Returns the LumaGreyscale operation to apply to the image.
   *
   * @return the operation to apply
   */
  @Override
  protected IOperation createOperation() {

    return new LumaGreyscale();
  }
}
