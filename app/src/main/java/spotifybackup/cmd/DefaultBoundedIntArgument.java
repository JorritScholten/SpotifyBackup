package spotifybackup.cmd;

public class DefaultBoundedIntArgument extends Argument {
    private final Integer min, max;
    private Integer value;

    public DefaultBoundedIntArgument(String name, String description, Character shortName, Integer defaultValue, Integer minimum)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, true);
        this.min = minimum;
        this.max = Integer.MAX_VALUE;
        checkFieldsNotNull();
        try {
            setBoundedValue(defaultValue);
        } catch (MalformedInputException e) {
            throw new IllegalConstructorParameterException(e.getMessage());
        }
    }

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

    public DefaultBoundedIntArgument(String name, String description, Integer defaultValue, Integer minimum)
            throws IllegalConstructorParameterException {
        super(name, description, false, true);
        this.min = minimum;
        this.max = Integer.MAX_VALUE;
        checkFieldsNotNull();
        try {
            setBoundedValue(defaultValue);
        } catch (MalformedInputException e) {
            throw new IllegalConstructorParameterException(e.getMessage());
        }
    }

    public DefaultBoundedIntArgument(String name, String description, Integer defaultValue, Integer minimum, Integer maximum)
            throws IllegalConstructorParameterException {
        super(name, description, false, true);
        this.min = minimum;
        this.max = maximum;
        checkFieldsNotNull();
        try {
            setBoundedValue(defaultValue);
        } catch (MalformedInputException e) {
            throw new IllegalConstructorParameterException(e.getMessage());
        }
    }

    private void checkFieldsNotNull()
            throws IllegalConstructorParameterException {
        if (min == null) {
            throw new IllegalConstructorParameterException("minimum can not be null value.");
        }
        if (max == null) {
            throw new IllegalConstructorParameterException("minimum can not be null value.");
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