package spotifybackup.cmd.argument.file;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.io.File;

public class MandatoryFilePathArgument extends Argument {
    private final boolean isFolder;
    private File value;

    /**
     * Mandatory file path argument, program won't execute if missing. Argument throws exception at runtime if supplied
     * value does not adhere to isFolder rule.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param shortName   Identifying character of argument, -{Character} is used as identifier.
     * @param isFolder    Argument throws runtime error if supplied value does not match (also applied to defaultValue).
     * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a character
     *                                              not in the alphabet.
     */
    public MandatoryFilePathArgument(String name, String description, Character shortName, boolean isFolder)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
        this.isFolder = isFolder;
    }

    /**
     * Mandatory file path argument and without an identifying character, program won't execute if missing. Argument
     * throws exception at runtime if supplied value does not adhere to isFolder rule.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param isFolder    Argument throws runtime error if supplied value does not match (also applied to defaultValue).
     * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a character
     *                                              not in the alphabet.
     */
    public MandatoryFilePathArgument(String name, String description, boolean isFolder)
            throws IllegalConstructorParameterException {
        this(name, description, null, isFolder);
    }

    @Override
    protected String getValueName() {
        return "FILE";
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
