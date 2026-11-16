package view;

import controller.ImageController;
import controller.ImageDatabase;
import model.abstraction.InputConverter;
import model.histogram.HistogramRGBValues;
import model.image.IImage;
import controller.commands.*;
import java.io.File;
import java.util.Arrays;

/**
 * The ViewController class acts as the mediator.
 * Between the view and the model.
 * It listens to user interactions from the view,
 * applies corresponding operations and updates the view
 * Implements the ViewListener interface to handle
 * view-triggered events.
 */
public class ViewController implements ViewListener {

  private final ImageController controller;
  private final View view;
  private final ImageDatabase database;

  /**
   * Constructs a ViewController object.
   * That integrates the ImageDatabase and the View
   * Delegating operations to an ImageController and handling
   * events triggered by the view.
   *
   * @param database the ImageDatabase
   * @param view the view component
   */
  public ViewController(ImageDatabase database, View view) {
    this.database = database;
    this.view = view;
    // loop in the original image controller to run
    // commands and back end logic
    this.controller = new ImageController(null,
        null, database);
    // delegate control to this specific
    // controller as a listener for all the events
    this.view.setViewListener(this);
  }

  /**
   * Handles the loading of an image.
   * Prompts the user for a file path.
   */
  @Override
  public void handleLoadImage() {
    // loops in import loads
    // window which opens the file chooser
    String path = view.importLoadWindow();
    if (path != null) {
      String default_name =
          new File(path).getName().replaceFirst("[.][^.]+$",
              "");
      String image_name =
          view.popUpInputWindow("Enter Image Name", default_name);
      if (image_name != null && !image_name.trim().isEmpty()) {
        executeCommand(new LoadCommand(path, image_name), image_name);
      }
    }
  }

  /**
   * Handles the saving of an image.
   * Prompts the user for a file path.
   */
  @Override
  public void handleSaveImage() {
    String image_name = view.getSelectedImageName();
    if (image_name == null) {
      view.displayError("No image selected.");
      return;
    }
    String path = view.exportSaveWindow();
    if (path != null) {
      try {
        new SaveCommand(path,
            image_name).run(controller);
      } catch (Exception e) {
        view.displayError(e.getMessage());
      }
    }
  }

  /**
   * Handles the brightening operation.
   * @param amount the numerical value
   */
  @Override
  public void handleBrighten(String amount) {
    handleValueOperation("brighten",
        amount, true);
  }

  /**
   * Handles the darkening operation.
   * @param amount the numerical value
   */
  @Override
  public void handleDarken(String amount) {
    handleValueOperation("darken",
        amount, false);
  }

  /**
   * Handles the greyscale operation for red.
   */
  @Override
  public void handleRedGreyscale() {
    handleGreyscale("red",
        RedGreyscaleCommand::new);
  }

  /**
   * Handles the greyscale operation for green.
   */
  @Override
  public void handleGreenGreyscale() {
    handleGreyscale("green",
        GreenGreyscaleCommand::new);
  }

  /**
   * Handles the greyscale operation for blue.
   */
  @Override
  public void handleBlueGreyscale() {
    handleGreyscale("blue",
        BlueGreyscaleCommand::new);
  }

  /**
   * Handles the greyscale operation for value.
   */
  @Override
  public void handleValueGreyscale() {
    handleGreyscale("value",
        ValueGreyscaleCommand::new);
  }

  /**
   * Handles the greyscale operation for luma.
   */
  @Override
  public void handleLumaGreyscale() {
    handleGreyscale("luma",
        LumaGreyscaleCommand::new);
  }

  /**
   * Handles the greyscale operation for intensity.
   */
  @Override
  public void handleIntensityGreyscale() {
    handleGreyscale("intensity",
        IntensityGreyscaleCommand::new);
  }

  /**
   * Handles the selection of an image.
   * By retrieving it from the database and updating the view,
   * including the image display and its associated histogram.
   *
   * @param image_name the name of the image
   */
  @Override
  public void handleImageSelection(String image_name) {
    // retrieve image from the image database
    IImage image = (image_name == null) ? null :
        database.get_image(image_name);
    // update the view with the retrieved image
    view.updateImage(image != null ?
        InputConverter.convertInput(image) : null);
    // histogram integration:
    view.updateHistogram(image != null ?
        HistogramRGBValues.calculateValues(image) : null);
  }

  /**
   * Handles the greyscale operation for an image.
   * Based on the provided operation name and command
   * Prompts the user for a destination name
   * @param operation_name the name of the greyscale op
   * @param factory a factory to create the
   *                appropriate greyscale command
   */
  private void handleGreyscale(String operation_name,
                               GreyscaleCommandFactory factory) {
    String name = view.getSelectedImageName();
    if (name == null) {
      view.displayError("No image selected.");
      return;
      // generic grayscale command since multiple
      // operations share the same logic
      // factory logic is introduced in another
      // helper method below to manage arguments
    }
    String dest_name = view.popUpInputWindow("Enter "
            + operation_name + " image:",
        name + "-" + operation_name);
    if (dest_name != null && !dest_name.trim().isEmpty()) {
      executeCommand(factory.create(name, dest_name), dest_name);
    }
  }

  /**
   * Handles numerical adjustment operations.
   * Such as brightening or darkening an image.
   * @param operation_name the name of the operation
   * @param amount the numerical value
   * @param isBrighten a flag indicating whether
   *                   the operation is to brighten (true)
   *                   or darken (false)
   */
  private void handleValueOperation(String operation_name,
                                    String amount, boolean isBrighten) {
    String name = view.getSelectedImageName();
    if (name == null) {
      view.displayError("No image selected.");
      return;
    }
    try {
      int value = Integer.parseInt(amount);
      String destName = view.popUpInputWindow("Enter name for " +
          operation_name + " image:", name + "-" + operation_name);
      if (destName != null && !destName.trim().isEmpty()) {
        Command cmd = isBrighten ? new BrightenCommand(value,
            name, destName) :
            new DarkenCommand(value, name, destName);
        executeCommand(cmd, destName);
      }
    } catch (NumberFormatException e) {
      view.displayError("Please enter a valid number.");
    }
  }

  /**
   * Executes a command and updates the image list in the view.
   * @param command the command to be executed
   * @param new_image_name the name to assign
   */
  private void executeCommand(Command command,
                              String new_image_name) {
    try {
      command.run(controller);
      updateImageList();
      if (new_image_name != null) {
        view.setSelectedImage(new_image_name);
      }
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Updates the list of image names in the view.
   * This method retrieves all image names from the database,
   * Sorts them alphabetically, and updates the view
   */
  private void updateImageList() {
    String[] names =
        database.get_all_images().keySet().toArray(new String[0]);
    Arrays.sort(names);
    view.updateImageDatabase(names);
  }

  /**
   * Functional interface for creating greyscale commands.
   */
  @FunctionalInterface
  interface GreyscaleCommandFactory {
    Command create(String source, String dest);
  }
}
