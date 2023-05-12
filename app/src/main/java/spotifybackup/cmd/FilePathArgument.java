package spotifybackup.cmd;

import java.io.File;

public class FilePathArgument extends Argument {
    private final boolean isFolder;
    private File value;

    public FilePathArgument(String name, String description, Character shortName, boolean isMandatory, boolean isFolder)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, isMandatory, true);
        this.isFolder = isFolder;
    }

    public FilePathArgument(String name, String description, boolean isMandatory, boolean isFolder)
            throws IllegalConstructorParameterException {
        super(name, description, isMandatory, true);
        this.isFolder = isFolder;
    }

    @Override
    public File getValue() {
        if (isPresent) {
            return value;
        } else {
            throw new ArgumentNotPresentException("Argument " + name + " not supplied a value.");
        }
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        this.value = new File(value);
        if (this.value.isDirectory() && !this.isFolder) {
            throw new MalformedInputException("Supplied filepath points to a directory rather than a file: " + this.value);
        } else if (this.value.isFile() && this.isFolder) {
            throw new MalformedInputException("Supplied filepath points to a file rather than a directory: " + this.value);
        }
    }
}
