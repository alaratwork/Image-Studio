import controller.ImageDatabase;
import view.ViewController;
import view.View;
import javax.swing.SwingUtilities;

/**
 * The main class to launch the IME GUI application.
 * It creates the Model, the View and the Controller.
 *
 */
public class IMEProgram {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      // create the Model (the database that holds the images)
      ImageDatabase database = new ImageDatabase();
      // create the View (the main window)
      View view = new View();
      // create the Controller and link it
      // to the Model and the View
      new ViewController(database, view);
    });
  }
}
