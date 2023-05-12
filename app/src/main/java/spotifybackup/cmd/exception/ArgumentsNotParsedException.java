package spotifybackup.cmd.exception;

/**
 * Thrown when trying to get value before command-line input has been parsed.
 */
public class ArgumentsNotParsedException extends Exception{
    public ArgumentsNotParsedException(String errorMessage){
        super(errorMessage);
    }
}
