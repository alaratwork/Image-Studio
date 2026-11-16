package controller.commands;

import controller.ImageController;
import model.operations.IOperation;
import model.operations.light.Brighten;

/**
 * Represents the command to brighten an image by a specified amount.
 * This command applies the Brighten operation to the given source image
 * and stores the result in the destination image.
 */
public class BrightenCommand implements Command {

  private final int brightness;
  private final String source_image_name;
  private final String destination_image_name;

  /**
   * Constructs a BrightenCommand with the given parameters.
   *
   * @param brightness             the amount to increase brightness
   * @param source_image_name      the name of the source image
   * @param destination_image_name the name of the destination image
   * @throws IllegalArgumentException if any argument is invalid
   */
  public BrightenCommand(int brightness, String source_image_name, String destination_image_name)
      throws IllegalArgumentException {
    if (source_image_name == null || source_image_name.trim().isEmpty()) {
      throw new IllegalArgumentException("Source image name cannot be null or empty.");
    }
    if (destination_image_name == null || destination_image_name.trim().isEmpty()) {
      throw new IllegalArgumentException("Destination image name cannot be null or empty.");
    }
    if (brightness < 0) {
      throw new IllegalArgumentException("Brightness cannot be negative.");
    }
    this.brightness = brightness;
    this.source_image_name = source_image_name;
    this.destination_image_name = destination_image_name;
  }

  /**
   * Executes the brighten operation using the provided controller.
   *
   * @param controller the controller responsible for applying operations
   * @throws IllegalArgumentException if an error occurs during execution
   */
  @Override
  public void run(ImageController controller) throws IllegalArgumentException {
    IOperation brighten_operation = new Brighten(brightness);
    controller.applyOperation(brighten_operation, source_image_name, destination_image_name);
  }
}