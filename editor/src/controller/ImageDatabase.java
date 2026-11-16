package controller;

import model.image.IImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all images that are currently being worked on or stored.
 * Maps string names to their respective IImage objects.
 */
public class ImageDatabase {

  // A map to store all images by name
  private final Map<String, IImage> image_map;

  /**
   * Creates a new empty image database.
   */
  public ImageDatabase() {
    this.image_map = new HashMap<>();
  }

  /**
   * Add an image to the database with a given name.
   *
   * @param name   name to assign to this image
   * @param image  the actual image object to store
   */
  public void add_image(String name, IImage image) {
    image_map.put(name, image);
  }

  /**
   * Retrieve an image from the database by its name.
   *
   * @param name name of the image
   * @return the image, or null if not found
   */
  public IImage get_image(String name) {
    return image_map.get(name);
  }

  /**
   * Check if an image exists with the given name.
   *
   * @param name name to check
   * @return true if the image is in the database, false otherwise
   */
  public boolean has_image(String name) {
    return image_map.containsKey(name);
  }

  /**
   * Return all current images (useful for testing or inspection).
   *
   * @return map of all stored images
   */
  public Map<String, IImage> get_all_images() {
    return new HashMap<>(image_map);
  }
}
