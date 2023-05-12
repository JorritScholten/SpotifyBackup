package spotifybackup.cmd;

public class MandatoryStringArgument extends Argument {
    private String value;

    public MandatoryStringArgument(String name, String description, Character shortName)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
    }

    public MandatoryStringArgument(String name, String description)
            throws IllegalConstructorParameterException {
        this(name, description, null);
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
