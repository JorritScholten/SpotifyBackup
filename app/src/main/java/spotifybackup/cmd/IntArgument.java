package spotifybackup.cmd;

public class IntArgument extends Argument {
    private Integer value;

    public IntArgument(String name, String description, Character shortName, boolean isMandatory)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, isMandatory, true);
    }

    public IntArgument(String name, String description, boolean isMandatory) {
        super(name, description, isMandatory, true);
    }

    @Override
    public Integer getValue() {
        if (isPresent) {
            return value;
        } else {
            throw new ArgumentNotPresentException("Argument " + name + " not supplied a value.");
        }
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
