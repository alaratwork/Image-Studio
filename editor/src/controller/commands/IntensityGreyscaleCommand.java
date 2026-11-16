package controller.commands;

import model.operations.IOperation;
import model.operations.greyscale.IntensityGreyscale;

/**
 * Applies a greyscale transformation using the intensity component
 * of the source image. The result is stored under a new image name.
 */
public class IntensityGreyscaleCommand extends AbstractGreyscaleCommand {

  /**
   * Constructs an IntensityGreyscaleCommand with the given source and destination image names.
   *
   * @param source_image_name the name of the source image in the model
   * @param dest_image_name   the name to assign to the resulting greyscale image
   * @throws IllegalArgumentException if either name is null or empty
   */
  public IntensityGreyscaleCommand(String source_image_name, String dest_image_name) {
    super(source_image_name, dest_image_name);
  }

  /**
   * Creates and returns the IntensityGreyscale operation to apply to the image.
   *
   * @return the operation to apply
   */
  @Override
  protected IOperation createOperation() {
    return new IntensityGreyscale();
  }
}
