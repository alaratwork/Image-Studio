package controller.commands;

import controller.ImageController;
import model.operations.IOperation;
import model.operations.light.Darken;

/**
 * A command that applies a darken operation to an image.
 * It decreases the brightness of the source image by a given decrement
 * and saves the result as a new image in the controller.
 */
public class DarkenCommand implements Command {
  private final int decrement;
  private final String source_image_name;
  private final String dest_image_name;

  public DarkenCommand(int decrement, String source_image_name, String dest_image_name) {
    if (source_image_name == null || source_image_name.trim().isEmpty()) {
      throw new IllegalArgumentException("Source image name cannot be null or empty.");
    }
    if (dest_image_name == null || dest_image_name.trim().isEmpty()) {
      throw new IllegalArgumentException("Destination image name cannot be null or empty.");
    }
    this.decrement = decrement;
    this.source_image_name = source_image_name;
    this.dest_image_name = dest_image_name;
  }


  @Override
  public void run(ImageController controller) throws IllegalArgumentException {
    IOperation darkenOperation = new Darken(decrement);
    controller.applyOperation(darkenOperation, source_image_name, dest_image_name);
  }

  }