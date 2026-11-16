package studio;

/**
 * A search could not be completed: the engine is missing, a library has no
 * images, an engine program failed or timed out, or it returned no matches.
 * The message is written to be shown to the user as-is.
 */
public class SearchException extends Exception {

  /**
   * @param message a user-readable explanation of what went wrong
   */
  public SearchException(String message) {
    super(message);
  }

  /**
   * @param message a user-readable explanation of what went wrong
   * @param cause   the underlying failure
   */
  public SearchException(String message, Throwable cause) {
    super(message, cause);
  }
}
