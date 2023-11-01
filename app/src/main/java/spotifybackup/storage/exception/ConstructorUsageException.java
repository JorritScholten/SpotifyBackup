package spotifybackup.storage.exception;

public class ConstructorUsageException extends RuntimeException {
    public ConstructorUsageException(){
        super("Class constructor should not be called.");
    }
}
