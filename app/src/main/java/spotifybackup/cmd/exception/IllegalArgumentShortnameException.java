package spotifybackup.cmd.exception;

/** Thrown to indicate an Argument Builder has been passed an illegal value for the shortName field. */
public class IllegalArgumentShortnameException extends IllegalArgumentException {
    public IllegalArgumentShortnameException(String errorMessage) {
        super(errorMessage);
    }
}
