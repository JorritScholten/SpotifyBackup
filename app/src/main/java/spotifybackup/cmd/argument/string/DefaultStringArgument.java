package spotifybackup.cmd.argument.string;

import spotifybackup.cmd.argument.DefaultArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

public class DefaultStringArgument extends DefaultArgument {
    private String value;

    /**
     * String argument with default value, has flag-like behaviour because it can be called without a value.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param shortName    Identifying character of argument, -{Character} is used as identifier.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @throws IllegalConstructorParameterException When trying assign name or defaultValue as null or assigning
     *                                              shortName a character not in the alphabet.
     */
    public DefaultStringArgument(String name, String description, Character shortName, String defaultValue)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, true);
        if (defaultValue == null) {
            throw new IllegalConstructorParameterException("Default value can not be null.");
        } else {
            this.value = defaultValue;
        }
    }

    /**
     * String argument with default value and no identifying character, has flag-like behaviour because it can be
     * called without a value.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @throws IllegalConstructorParameterException When trying assign name or defaultValue as null or assigning
     *                                              shortName a character not in the alphabet.
     */
    public DefaultStringArgument(String name, String description, String defaultValue)
            throws IllegalConstructorParameterException {
        this(name, description, null, defaultValue);
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
