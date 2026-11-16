package controller.commands;

import controller.ImageController;

/**
 * Command to save an image from the model to a given file path using the appropriate writer.
 */
public class SaveCommand implements Command {
  private final String filePath;
  private final String imageName;

  /**
   * Constructs a SaveCommand with the specified file path and image name.
   *
   * @param filePath  the destination path to save the image
   * @param imageName the name of the image in the model to save
   * @throws IllegalArgumentException if either argument is null or empty
   */
  public SaveCommand(String filePath, String imageName) {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty.");
    }
    if (imageName == null || imageName.trim().isEmpty()) {
      throw new IllegalArgumentException("Image name cannot be null or empty.");
    }
    this.filePath = filePath;
    this.imageName = imageName;
  }

  /**
   * Executes the command by retrieving the image from the model and saving it to the file system.
   *
   * @param controller the image controller providing access to the model
   * @throws IllegalArgumentException if the image does not exist or cannot be saved
   */
  @Override
  public void run(ImageController controller) throws IllegalArgumentException {
    controller.saveImage(this.filePath, this.imageName);
  }
}