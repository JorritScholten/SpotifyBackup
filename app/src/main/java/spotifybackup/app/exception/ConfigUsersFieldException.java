package spotifybackup.app.exception;

/** Thrown when an error is found in the "users" field in the config file. */
public class ConfigUsersFieldException extends ConfigFileException {
    public ConfigUsersFieldException(String message) {super(message);}
}
