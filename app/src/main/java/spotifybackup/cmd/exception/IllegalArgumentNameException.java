package spotifybackup.cmd.exception;

/** Thrown to indicate an Argument Builder has been passed an illegal value for the name field. */
public class IllegalArgumentNameException extends IllegalArgumentException {
    public IllegalArgumentNameException(String errorMessage) {
        super(errorMessage);
    }
}
