package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

public class DefaultBoundedIntArgument extends Argument {
    private final Integer min, max;
    private Integer value;

    /**
     * Integer argument with range checking and default value, has flag-like behaviour because it can be called without
     * a value. Argument throws exception at runtime if supplied value is out of range.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param shortName    Identifying character of argument, -{Character} is used as identifier.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @param minimum      Minimum value integer should be (also applied to defaultValue).
     * @param maximum      Maximum value integer should be (also applied to defaultValue).
     * @throws IllegalConstructorParameterException When trying assign name, minimum, maximum or defaultValue as null,
     *                                              defaultValue is out of range or assigning shortName a character not
     *                                              in the alphabet.
     */
    public DefaultBoundedIntArgument(String name, String description, Character shortName, Integer defaultValue, Integer minimum, Integer maximum)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, true);
        this.min = minimum;
        this.max = maximum;
        checkFieldsNotNull();
        try {
            setBoundedValue(defaultValue);
        } catch (MalformedInputException e) {
            throw new IllegalConstructorParameterException(e.getMessage());
        }
    }

    /**
     * Integer argument with range checking and default value, has flag-like behaviour because it can be called without
     * a value. Argument throws exception at runtime if supplied value is out of range.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param shortName    Identifying character of argument, -{Character} is used as identifier.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @param minimum      Minimum value integer should be (also applied to defaultValue).
     * @throws IllegalConstructorParameterException When trying assign name, minimum or defaultValue as null,
     *                                              defaultValue is out of range or assigning shortName a character not
     *                                              in the alphabet.
     */
    public DefaultBoundedIntArgument(String name, String description, Character shortName, Integer defaultValue, Integer minimum)
            throws IllegalConstructorParameterException {
        this(name, description, shortName, defaultValue, minimum, Integer.MAX_VALUE);
    }

    /**
     * Integer argument with range checking, a default value and without identifying an character, has flag-like
     * behaviour because it can be called without a value. Argument throws exception at runtime if supplied value is out
     * of range.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @param minimum      Minimum value integer should be (also applied to defaultValue).
     * @throws IllegalConstructorParameterException When trying assign name, minimum or defaultValue as null,
     *                                              defaultValue is out of range or assigning shortName a character not
     *                                              in the alphabet.
     */
    public DefaultBoundedIntArgument(String name, String description, Integer defaultValue, Integer minimum)
            throws IllegalConstructorParameterException {
        this(name, description, null, defaultValue, minimum, Integer.MAX_VALUE);
    }

    /**
     * Integer argument with range checking, a default value and without identifying an character, has flag-like
     * behaviour because it can be called without a value. Argument throws exception at runtime if supplied value is out
     * of range.
     * @param name         Identifying name of argument, --{name} is used as identifier.
     * @param description  Description of argument printed in help.
     * @param defaultValue The value produced if the argument is absent from or undefined in the command line.
     * @param minimum      Minimum value integer should be (also applied to defaultValue).
     * @param maximum      Maximum value integer should be (also applied to defaultValue).
     * @throws IllegalConstructorParameterException When trying assign name, minimum, maximum or defaultValue as null,
     *                                              defaultValue is out of range or assigning shortName a character not
     *                                              in the alphabet.
     */
    public DefaultBoundedIntArgument(String name, String description, Integer defaultValue, Integer minimum, Integer maximum)
            throws IllegalConstructorParameterException {
        this(name, description, null, defaultValue, minimum, maximum);
    }

    @Override
    protected String getValueName() {
        return "INTEGER";
    }

    private void checkFieldsNotNull()
            throws IllegalConstructorParameterException {
        if (min == null) {
            throw new IllegalConstructorParameterException("minimum can not be null value.");
        }
        if (max == null) {
            throw new IllegalConstructorParameterException("maximum can not be null value.");
        }
    }

    private void setBoundedValue(Integer tempValue) throws MalformedInputException {
        if (tempValue == null) {
            throw new MalformedInputException("Can not assign null value.");
        } else if (tempValue >= min && tempValue <= max) {
            this.value = tempValue;
        } else {
            if (max == Integer.MAX_VALUE) {
                throw new MalformedInputException(
                        "[" + value + "] is out of bounds (should be greater than or equal to " + min + ")."
                );
            } else {
                throw new MalformedInputException(
                        "[" + value + "] is out of bounds (should be greater than or equal to "
                                + min + " and less than or equal to " + max + ")."
                );
            }
        }
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        try {
            setBoundedValue(Integer.valueOf(value));
        } catch (NumberFormatException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as an integer.");
        }
    }
}
