package controller.commands;

import controller.ImageController;
import model.operations.IOperation;

/**
 * Abstract command that applies a greyscale operation to an image and saves the result.
 * Subclasses define the specific greyscale operation logic.
 */
public abstract class AbstractGreyscaleCommand implements Command {
    protected final String source_image_name;
    protected final String dest_image_name;

    /**
     * Constructs a new greyscale command with a given source and destination image name.
     *
     * @param source_image_name the name of the source image in the model
     * @param dest_image_name   the name to assign to the resulting image
     * @throws IllegalArgumentException if either name is null or empty
     */
    public AbstractGreyscaleCommand(String source_image_name, String dest_image_name) {
        // Validate Source_image_name
        if (source_image_name == null || source_image_name.trim().isEmpty()) {
            throw new IllegalArgumentException("Source image name cannot be null or empty.");
        }
        // Validate Dest_image_name
        if (dest_image_name == null || dest_image_name.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination image name cannot be null or empty.");
        }
        this.source_image_name = source_image_name;
        this.dest_image_name = dest_image_name;
    }

    /**
     * Runs the greyscale command using the provided controller.
     * Applies the greyscale operation to the source image and stores the result.
     *
     * @param controller the image controller to access the model and perform the operation
     * @throws IllegalArgumentException if the operation fails or image names are invalid
     */
    @Override
    public void run(ImageController controller) throws IllegalArgumentException {
        // Create the specific greyscale operation from subclass
        IOperation operation = createOperation();
        // Apply the operation and store the result with the destination name
        controller.applyOperation(operation, source_image_name, dest_image_name);
    }

    /**
     * Creates the specific greyscale operation to be applied.
     *
     * @return the greyscale operation
     */
    protected abstract IOperation createOperation();
}
