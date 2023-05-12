package spotifybackup.cmd.exception;

/**
 * Thrown when trying to get the value of an undefined argument.
 */
public class ArgumentNotPresentException extends RuntimeException {
    public ArgumentNotPresentException(String errorMessage) {
        super(errorMessage);
    }
}
