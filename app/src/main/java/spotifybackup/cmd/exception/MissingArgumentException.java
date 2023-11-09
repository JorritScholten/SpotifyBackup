package spotifybackup.cmd.exception;

/**
 * Thrown when a mandatory argument is missing from the input.
 */
public class MissingArgumentException extends Exception {
    public MissingArgumentException(String errorMessage) {
        super(errorMessage);
    }
}
