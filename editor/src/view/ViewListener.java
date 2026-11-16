package view;


/**
 * The ViewListener interface defines methods.
 * For handling various user actions in a GUI
 * Each method corresponds to a specific operation
 * that can be triggered from the view.
 */
public interface ViewListener {

  /**
   * Handles the loading of an image.
   */
  void handleLoadImage();

  /**
   * Handles the save operation.
   */
  void handleSaveImage();

  /**
   * Handles the brighten operation.
   * @param amount the numerical value
   */
  void handleBrighten(String amount);

  /**
   * Handles the darken operation.
   * @param amount the numerical value
   */
  void handleDarken(String amount);

  /**
   * Handles the greyscale operation for red.
   */
  void handleRedGreyscale();

  /**
   * Handles the greyscale operation for green.
   */
  void handleGreenGreyscale();

  /**
   * Handles the greyscale operation for blue.
   */
  void handleBlueGreyscale();

  /**
   * Handles the greyscale operation for luma.
   */
  void handleLumaGreyscale();

  /**
   * Handles the greyscale operation for intensity.
   */
  void handleIntensityGreyscale();

  /**
   * Handles the greyscale operation for value.
   */
  void handleValueGreyscale();

  /**
   * Handles the selection operation for an image.
   */
  void handleImageSelection(String image_name);

}