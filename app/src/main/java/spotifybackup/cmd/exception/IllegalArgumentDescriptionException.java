package spotifybackup.cmd.exception;

/** Thrown to indicate an Argument Builder has been passed an illegal value for the description field/ */
public class IllegalArgumentDescriptionException extends IllegalArgumentException {
    public IllegalArgumentDescriptionException(String errorMessage) {
        super(errorMessage);
    }
}
