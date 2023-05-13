package spotifybackup.cmd.argument.string;

import spotifybackup.cmd.argument.DefaultArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

public class DefaultStringArgument extends DefaultArgument {
    private String value;

    public DefaultStringArgument(String name, String description, Character shortName, String defaultValue)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, true);
        if (defaultValue == null) {
            throw new IllegalConstructorParameterException("Default value can not be null.");
        } else {
            this.value = defaultValue;
        }
    }

    public DefaultStringArgument(String name, String description, String defaultValue)
            throws IllegalConstructorParameterException {
        this(name, description, null, defaultValue);
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
