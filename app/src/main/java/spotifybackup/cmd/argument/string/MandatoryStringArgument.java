package spotifybackup.cmd.argument.string;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

public class MandatoryStringArgument extends Argument {
    private String value;

    /**
     * Mandatory String argument, program won't execute if missing.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param shortName   Identifying character of argument, -{Character} is used as identifier.
     * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a character
     *                                              not in the alphabet.
     */
    public MandatoryStringArgument(String name, String description, Character shortName)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
    }

    /**
     * Mandatory String argument without identifying character, program won't execute if missing.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a character
     *                                              not in the alphabet.
     */
    public MandatoryStringArgument(String name, String description)
            throws IllegalConstructorParameterException {
        this(name, description, null);
    }

    @Override
    protected String getValueName() {
        return "STRING";
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        if (value == null) {
            throw new MalformedInputException("setValue can not be called with a null value.");
        } else {
            this.value = value;
        }
    }
}
