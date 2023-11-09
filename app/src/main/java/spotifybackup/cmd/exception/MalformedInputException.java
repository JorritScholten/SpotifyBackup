package spotifybackup.cmd.exception;

/**
 * Thrown when the value passed to an argument can't be correctly converted to the specified type.
 */
public class MalformedInputException extends Exception {
    public MalformedInputException(String errorMessage) {
        super(errorMessage);
    }
}
