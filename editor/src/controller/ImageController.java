package controller;

import controller.commands.*;
import model.abstraction.reader.IReader;
import model.abstraction.reader.ReaderFactory;
import model.abstraction.writer.WriterFactory;
import model.abstraction.writer.IWriter;
import model.operations.IOperation;
import model.image.IImage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

import static java.lang.System.out;

/**
 * Controller class for managing image operations through commands.
 */
public class ImageController {
  private final Readable input;
  private final Appendable output;
  private final ImageDatabase database;
  private final Map<String, Function<Scanner, Command>> commands;

  /**
   * Constructor for an ImageController.
   *
   * @param input    Readable for commands (string reader, systemin, etc)
   * @param output   Appendable for message destinations
   * @param database ImageDatabase will manage images
   * @throws IllegalArgumentException if any are null
   */
  public ImageController(Readable input, Appendable output, ImageDatabase database) throws IllegalArgumentException {
    if (database == null) {
      throw new IllegalArgumentException("Input, output, and database cannot be null.");
    }
    this.input = input;
    this.output = output;
    this.database = database;
    this.commands = new HashMap<>();
    initializeCommands();
  }

  /**
   * Initializes the map of supported commands.
   */
  private void initializeCommands() {
    commands.put("load", scanner -> new LoadCommand(scanner.next(), scanner.next()));
    commands.put("save", scanner -> new SaveCommand(scanner.next(), scanner.next()));
    commands.put("brighten", scanner -> new BrightenCommand(scanner.nextInt(), scanner.next(), scanner.next()));
    commands.put("darken", scanner -> new DarkenCommand(scanner.nextInt(), scanner.next(), scanner.next()));
    commands.put("red-greyscale", scanner -> new RedGreyscaleCommand(scanner.next(), scanner.next()));
    commands.put("green-greyscale", scanner -> new GreenGreyscaleCommand(scanner.next(), scanner.next()));
    commands.put("blue-greyscale", scanner -> new BlueGreyscaleCommand(scanner.next(), scanner.next()));
    commands.put("luma-greyscale", scanner -> new LumaGreyscaleCommand(scanner.next(), scanner.next()));
    commands.put("intensity-greyscale", scanner -> new IntensityGreyscaleCommand(scanner.next(), scanner.next()));
    commands.put("value-greyscale", scanner -> new ValueGreyscaleCommand(scanner.next(), scanner.next()));
  }

  /**
   * Private helper method that gives user feedback.
   *
   * @param message message displayed
   * @throws IOException if I/O exception occurs
   */
  private void interface_message(String message) throws IOException {
    this.output.append(message).append(System.lineSeparator());
  }

  /**
   * Main orchestrator function to run the controller and process commands.
   */
  public void run() {
    Scanner scanner = new Scanner(this.input);
    try {
      interface_message("IME. Enter image commands. Type 'q' to exit.");
      while (scanner.hasNextLine()) {
        interface_message("Enter a command:");
        String command_line = scanner.nextLine().trim();
        if (command_line.isEmpty() || command_line.startsWith("#")) {
          continue;
        }
        Scanner command_line_scanner = new Scanner(command_line);
        if (!command_line_scanner.hasNext()) {
          command_line_scanner.close();
          continue;
        }
        String command_id = command_line_scanner.next().toLowerCase();
        if (command_id.equals("q")) {
          interface_message("Quitting the IME program.");
          command_line_scanner.close();
          break;
        }
        Function<Scanner, Command> command_getter = commands.get(command_id);
        if (command_getter == null) {
          interface_message("You entered an invalid command name.");
          command_line_scanner.close();
          continue;
        }
        try {
          Command command = command_getter.apply(command_line_scanner);
          command.run(this);
          interface_message(command_id + " command successfully implemented");
        } catch (Exception e) {
          interface_message("Error executing command: " + e.getMessage());
        }
        command_line_scanner.close();
      }
    } catch (IOException e) {
      // This is now the primary catch for IOExceptions from interface_message
      System.err.println("An I/O error occurred: " + e.getMessage());
    } finally {
      scanner.close();
    }
  }

  /**
   * Loads an image from the given path and stores it in the database with the given name.
   *
   * @param path the file path of the image to load
   * @param name the name to associate with the loaded image
   */
  public void loadImage(String path, String name) {
    try {
      IReader reader = ReaderFactory.getReader(path);
      IImage image = reader.read();
      database.add_image(name, image);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error loading image: " + e.getMessage());
    }
  }

  /**
   * Saves an image with the given name to the specified path.
   *
   * @param path the file path to save the image to
   * @param name the name of the image in the database to save
   */
  public void saveImage(String path, String name) {
    IImage image = database.get_image(name);
    if (image == null) {
      throw new IllegalArgumentException("Image '" + name + "' not found in database.");
    }
    try {
      IWriter writer = WriterFactory.getWriter(path);
      writer.write(image, path);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error saving image: " + e.getMessage());
    }
  }

  /**
   * Applies an image operation to a source image and stores the result with a new name.
   *
   * @param operation         the image operation to apply
   * @param source_image_name the name of the source image in the database
   * @param dest_image_name   the name to associate with the resulting image
   * @throws IllegalArgumentException if the source image is not found
   */
  public void applyOperation(IOperation operation, String source_image_name, String dest_image_name) throws IllegalArgumentException {
    IImage source_image = database.get_image(source_image_name);
    if (source_image == null) {
      throw new IllegalArgumentException("Source image '" + source_image_name + "' not found in database.");
    }
    IImage result_image = operation.apply(source_image);
    database.add_image(dest_image_name, result_image);
  }
}
