package spotifybackup.storage.exception;

/** Exception thrown to indicate this constructor shouldn't be used. */
public class ConstructorUsageException extends RuntimeException {
    /** Exception thrown to indicate this constructor shouldn't be used. */
    public ConstructorUsageException(){
        super("Class constructor should not be called.");
    }
}
