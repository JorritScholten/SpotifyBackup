package spotifybackup.storage.exception;

/** Exception thrown to indicate that an active transaction is needed. */
public class TransactionInactiveException extends RuntimeException{
    /** Exception thrown to indicate that an active transaction is needed. */
    public TransactionInactiveException(){
        super("Method will only work from within an active transaction.");
    }
}
