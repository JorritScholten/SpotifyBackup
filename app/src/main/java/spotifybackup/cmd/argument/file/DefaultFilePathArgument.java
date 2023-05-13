package spotifybackup.cmd.argument.file;

import spotifybackup.cmd.argument.DefaultArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.io.File;

public class DefaultFilePathArgument extends DefaultArgument {
    private final boolean isFolder;
    private File value;

    /**
     * File path argument with default value, has flag-like behaviour because it can be called without a value. Argument
     * throws exception at runtime if supplied value does not adhere to isFolder rule.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param shortName    Identifying character of argument, -{Character} is used as identifier.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @param isFolder     Argument throws runtime error if supplied value does not match (also applied to defaultValue).
     * @throws IllegalConstructorParameterException When trying assign name or defaultValue as null or assigning
     *                                              shortName a character not in the alphabet.
     */
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

    /**
     * File path argument with default value and without identifying an character, has flag-like behaviour because it
     * can be called without a value. Argument
     * throws exception at runtime if supplied value does not adhere to isFolder rule.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @param isFolder     Argument throws runtime error if supplied value does not match (also applied to defaultValue).
     * @throws IllegalConstructorParameterException When trying assign name or defaultValue as null or assigning
     *                                              shortName a character not in the alphabet.
     */
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
