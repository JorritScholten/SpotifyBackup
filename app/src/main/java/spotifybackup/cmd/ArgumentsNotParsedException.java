package spotifybackup.cmd;

public class ArgumentsNotParsedException extends Exception{
    public ArgumentsNotParsedException(String errorMessage){
        super(errorMessage);
    }
}
