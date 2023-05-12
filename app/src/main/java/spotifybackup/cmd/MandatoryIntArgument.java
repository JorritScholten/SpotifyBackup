package spotifybackup.cmd;

public class MandatoryIntArgument extends Argument {
    private Integer value;

    public MandatoryIntArgument(String name, String description, Character shortName)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
    }

    public MandatoryIntArgument(String name, String description) {
        super(name, description, true, true);
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
