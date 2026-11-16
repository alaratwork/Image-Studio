package model.operations;

import model.image.IImage;

/**
 * Interface for an image operation.
 * (Strategy Pattern)
 * Represents an operation that can be performed on an image.
 * Defines specific methods for performing the operation.
 */
public interface IOperation {

  /**
   * Performs the operation on the image.
   * @param image IImage object to be operated on
   * @return an IImage object
   * @throws IllegalArgumentException if the image is null
   */
  IImage apply(IImage image) throws IllegalArgumentException;
}
