package controller.commands;

import controller.ImageController;

/**
 * Command to load an image from a given file path and store it in the model
 * under a user-defined name.
 */
public class LoadCommand implements Command {
  private final String file_path;
  private final String image_name;

  /**
   * Constructs a LoadCommand with the specified file path and image name.
   *
   * @param file_path  the path to the image file
   * @param image_name the name to assign to the loaded image in the model
   * @throws IllegalArgumentException if either argument is null or empty
   */
  public LoadCommand(String file_path, String image_name) {
    if (file_path == null || file_path.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty.");
    }
    if (image_name == null || image_name.trim().isEmpty()) {
      throw new IllegalArgumentException("Image name cannot be null or empty.");
    }
    this.file_path = file_path;
    this.image_name = image_name;
  }

  /**
   * Executes the command by reading an image from the file system and storing it in the model.
   *
   * @param controller the image controller containing the model
   * @throws IllegalArgumentException if the image could not be read or stored
   */
  @Override
  public void run(ImageController controller) throws IllegalArgumentException {
    try {
      controller.loadImage(file_path, image_name);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Failed to load image: " + e.getMessage(), e);
    }
  }
}