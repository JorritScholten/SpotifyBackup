package spotifybackup.cmd;

public class DefaultIntArgument extends Argument {
    private Integer value;

    public DefaultIntArgument(String name, String description, Character shortName, Integer defaultValue)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, true);
        if (defaultValue == null) {
            throw new IllegalConstructorParameterException("Default value can not be null.");
        } else {
            this.value = defaultValue;
        }
    }

    public DefaultIntArgument(String name, String description, Integer defaultValue)
            throws IllegalConstructorParameterException {
        super(name, description, false, true);
        if (defaultValue == null) {
            throw new IllegalConstructorParameterException("Default value can not be null.");
        } else {
            this.value = defaultValue;
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
            this.value = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as an integer.");
        }
    }
}
