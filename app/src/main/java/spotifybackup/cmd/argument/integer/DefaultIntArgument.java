package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.argument.DefaultArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

public class DefaultIntArgument extends DefaultArgument {
    private Integer value;

    /**
     * Integer argument with default value, has flag-like behaviour because it can be called without a value.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param shortName    Identifying character of argument, -{Character} is used as identifier.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @throws IllegalConstructorParameterException When trying assign name or defaultValue as null or assigning
     *                                              shortName a character not in the alphabet.
     */
    public DefaultIntArgument(String name, String description, Character shortName, Integer defaultValue)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, true);
        if (defaultValue == null) {
            throw new IllegalConstructorParameterException("Default value can not be null.");
        } else {
            this.value = defaultValue;
        }
    }

    /**
     * Integer argument with default value and no identifying character, has flag-like behaviour because it can be
     * called without a value.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @throws IllegalConstructorParameterException When trying assign name or defaultValue as null or assigning
     *                                              shortName a character not in the alphabet.
     */
    public DefaultIntArgument(String name, String description, Integer defaultValue)
            throws IllegalConstructorParameterException {
        this(name, description, null, defaultValue);
    }

    @Override
    protected String getValueName() {
        return "INTEGER";
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        try {
            this.value = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as an integer.");
        }
    }
}
