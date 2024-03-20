package spotifybackup.cmd.exception;

public class MissingValueException extends MalformedInputException {
    public MissingValueException(String errorMessage) {
        super(errorMessage);
    }
}
