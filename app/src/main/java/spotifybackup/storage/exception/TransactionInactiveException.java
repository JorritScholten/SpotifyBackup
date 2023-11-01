package spotifybackup.storage.exception;

public class TransactionInactiveException extends RuntimeException{
    public TransactionInactiveException(){
        super("Method will only work from within an active transaction.");
    }
}
