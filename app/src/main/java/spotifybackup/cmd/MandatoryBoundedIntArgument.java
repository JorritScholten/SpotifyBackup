package spotifybackup.cmd;

public class MandatoryBoundedIntArgument extends Argument {
    private final Integer min, max;
    private Integer value;

    /**
     * Mandatory integer argument with range checking, program won't execute if missing. Argument throws exception at
     * runtime if supplied value is out of range.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param shortName   Identifying character of argument, -{Character} is used as identifier.
     * @param minimum     Minimum value integer should be.
     * @param maximum     Maximum value integer should be.
     * @throws IllegalConstructorParameterException When trying assign name, minimum or maximum as null or assigning
     *                                              shortName a character not in the alphabet.
     */
    public MandatoryBoundedIntArgument(String name, String description, Character shortName, Integer minimum, Integer maximum)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
        this.min = minimum;
        this.max = maximum;
        checkFieldsNotNull();
    }

    /**
     * Mandatory integer argument with range checking, program won't execute if missing. Argument throws exception at
     * runtime if supplied value is out of range.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param shortName   Identifying character of argument, -{Character} is used as identifier.
     * @param minimum     Minimum value integer should be.
     * @throws IllegalConstructorParameterException When trying assign name or minimum as null or assigning shortName a
     *                                              character not in the alphabet.
     */
    public MandatoryBoundedIntArgument(String name, String description, Character shortName, Integer minimum)
            throws IllegalConstructorParameterException {
        this(name, description, shortName, minimum, Integer.MAX_VALUE);
    }

    /**
     * Mandatory integer argument with range checking and without an identifying character, program won't execute if
     * missing. Argument throws exception at runtime if supplied value is out of range.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param minimum     Minimum value integer should be.
     * @throws IllegalConstructorParameterException When trying assign name or minimum as null or assigning shortName a
     *                                              character not in the alphabet.
     */
    public MandatoryBoundedIntArgument(String name, String description, Integer minimum) {
        this(name, description, null, minimum, Integer.MAX_VALUE);
    }

    /**
     * Mandatory integer argument with range checking and without an identifying character, program won't execute if
     * missing. Argument throws exception at runtime if supplied value is out of range.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param minimum     Minimum value integer should be.
     * @param maximum     Maximum value integer should be.
     * @throws IllegalConstructorParameterException When trying assign name, minimum or maximum as null or assigning
     *                                              shortName a character not in the alphabet.
     */
    public MandatoryBoundedIntArgument(String name, String description, Integer minimum, Integer maximum) {
        this(name, description, null, minimum, maximum);
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
            var tempValue = Integer.valueOf(value);
            if (tempValue >= min && tempValue <= max) {
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
        } catch (NumberFormatException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as an integer.");
        }
    }
}
