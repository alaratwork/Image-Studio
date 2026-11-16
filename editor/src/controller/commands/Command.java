
package controller.commands;

import controller.ImageController;

/**
 * This interface represents a command in the IME application.
 * It uses the Command Pattern and supports execution via the ImageController.
 */
public interface Command {
  void run(ImageController controller) throws IllegalArgumentException;
}
