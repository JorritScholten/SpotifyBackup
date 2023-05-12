package spotifybackup.cmd;

public class StringArgument extends Argument {
    private String value;

    public StringArgument(String name, String description, Character shortName, boolean isMandatory)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, isMandatory, true);
    }

    public StringArgument(String name, String description, boolean isMandatory) {
        super(name, description, isMandatory, true);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        this.value = value;
    }
}