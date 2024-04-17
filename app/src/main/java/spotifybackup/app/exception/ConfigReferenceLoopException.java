package spotifybackup.app.exception;

/** Checked exception to guarantee that self-reference issues are checked for. */
public class ConfigReferenceLoopException extends Exception {
    public ConfigReferenceLoopException(String message) {super(message);}
}
