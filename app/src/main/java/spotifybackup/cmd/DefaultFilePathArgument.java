package spotifybackup.cmd;

import java.io.File;

public class DefaultFilePathArgument extends Argument {
    private final boolean isFolder;
    private File value;

    public DefaultFilePathArgument(String name, String description, Character shortName, File defaultValue, boolean isFolder)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, true);
        this.isFolder = isFolder;
        if (defaultValue == null) {
            throw new IllegalConstructorParameterException("Default value can not be null.");
        } else {
            this.value = defaultValue;
            try {
                checkValue();
            } catch (MalformedInputException e) {
                throw new IllegalConstructorParameterException(e.getMessage());
            }
        }
    }

    public DefaultFilePathArgument(String name, String description, File defaultValue, boolean isFolder)
            throws IllegalConstructorParameterException {
        this(name, description, null, defaultValue, isFolder);
    }

    @Override
    protected String getValueName() {
        return "FILE";
    }

    private void checkValue() throws MalformedInputException {
        if (this.value.isDirectory() && !this.isFolder) {
            throw new MalformedInputException("Supplied filepath points to a directory rather than a file: " + this.value);
        } else if (this.value.isFile() && this.isFolder) {
            throw new MalformedInputException("Supplied filepath points to a file rather than a directory: " + this.value);
        }
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
        checkValue();
    }
}
