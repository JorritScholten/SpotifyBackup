package spotifybackup.cmd;

/**
 * Thrown when trying to construct an Argument with an illegal value.
 */
public class IllegalConstructorParameterException extends RuntimeException {
    public IllegalConstructorParameterException(String errorMessage) {
        super(errorMessage);
    }
}
