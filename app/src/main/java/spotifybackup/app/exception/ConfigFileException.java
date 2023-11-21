package spotifybackup.app.exception;

/** Thrown when there is an issue with the config file. */
public class ConfigFileException extends RuntimeException {
    /** Thrown when there is an issue with the config file. */
    public ConfigFileException(String message) {
        super(message);
    }
}
