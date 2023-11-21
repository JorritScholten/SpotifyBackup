package spotifybackup.app.exception;

/** Thrown when there is a blank or missing field in the config file. */
public class BlankConfigFieldException extends ConfigFileException {
    /** Thrown when there is a blank or missing in the config file. */
    public BlankConfigFieldException(String message) {
        super(message);
    }
}
