package spotifybackup.cmd;

import java.io.File;

public class FilePathArgument extends Argument {
    private final boolean isFolder;
    private File value;

    public FilePathArgument(String name, String description, Character shortName, boolean isFolder)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
        this.isFolder = isFolder;
    }

    public FilePathArgument(String name, String description, boolean isFolder)
            throws IllegalConstructorParameterException {
        super(name, description, true, true);
        this.isFolder = isFolder;
    }

    @Override
    public File getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        if (value == null) {
            throw new MalformedInputException("setValue can not be called with a null value.");
        } else {
            this.value = new File(value);
        }
        if (this.value.isDirectory() && !this.isFolder) {
            throw new MalformedInputException("Supplied filepath points to a directory rather than a file: " + this.value);
        } else if (this.value.isFile() && this.isFolder) {
            throw new MalformedInputException("Supplied filepath points to a file rather than a directory: " + this.value);
        }
    }
}
