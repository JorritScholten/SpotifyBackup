package spotifybackup.cmd;

/**
 * A flag argument has no inherent value, rather it either is or isn't present in the input. A flag cannot be
 * mandatory.
 */
public class FlagArgument extends Argument {
    public FlagArgument(String name, String description) {
        isMandatory = false;
        hasValue = false;
        this.name = name;
        this.description = description;
    }

    public FlagArgument(String name, String description, Character shortName) {
        isMandatory = false;
        hasValue = false;
        this.name = name;
        this.description = description;
        this.shortName = shortName;
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
    protected void setValue(String value){}
}
