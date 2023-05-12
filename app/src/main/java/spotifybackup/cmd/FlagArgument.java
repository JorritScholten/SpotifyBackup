package spotifybackup.cmd;

/**
 * A flag argument has no inherent value, rather it either is or isn't present in the input. A flag cannot be
 * mandatory.
 */
public class FlagArgument extends Argument {
    public FlagArgument(String name, String description, Character shortName)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, false);
    }

    public FlagArgument(String name, String description) {
        this(name, description, null);
    }

    @Override
    protected String getValueName() {
        return "NONE";
    }

    /**
     * Returns true if flag is present in input.
     * @return true if flag is present in input.
     */
    @Override
    public Boolean getValue() {
        return (Boolean) isPresent;
    }

    /**
     * Does nothing, value is dependent on whether flag is present.
     * @param value is ignored.
     */
    @Override
    protected void setValue(String value) {
    }
}
