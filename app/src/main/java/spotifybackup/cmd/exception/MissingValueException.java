package spotifybackup.cmd.exception;

public class MissingValueException extends MalformedInputException {
    MissingValueException(String errorMessage) {
        super(errorMessage);
    }
}
