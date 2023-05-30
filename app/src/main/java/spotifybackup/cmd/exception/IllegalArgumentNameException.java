package spotifybackup.cmd.exception;

/** Thrown to indicate an Argument Builder has been passed an illegal name value. */
public class IllegalArgumentNameException extends IllegalArgumentException {
    public IllegalArgumentNameException(String errorMessage) {
        super(errorMessage);
    }
}
