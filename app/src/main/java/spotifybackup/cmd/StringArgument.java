package spotifybackup.cmd;

public class StringArgument extends Argument {
    private String value;

    public StringArgument(String name, String description, Character shortName)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
    }

    public StringArgument(String name, String description)
            throws IllegalConstructorParameterException {
        super(name, description, true, true);
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
