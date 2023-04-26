package spotifybackup.cmd;

public class ArgumentNotPresentException extends Exception {
    public ArgumentNotPresentException(String errorMessage) {
        super(errorMessage);
    }
}
