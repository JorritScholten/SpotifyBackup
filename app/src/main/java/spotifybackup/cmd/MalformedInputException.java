package spotifybackup.cmd;

/**
 * Thrown when the value passed to an argument can't be correctly converted to the specified type.
 */
public class MalformedInputException extends RuntimeException {
    public MalformedInputException(String errorMessage) {
        super(errorMessage);
    }
}
