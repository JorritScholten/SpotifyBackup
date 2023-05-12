package spotifybackup.cmd.argument;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

public class FlagArgument extends Argument {
    /**
     * A flag argument has no inherent value, rather it either is or isn't present in the input.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param shortName   Identifying character of argument, -{Character} is used as identifier.
     * @throws IllegalConstructorParameterException when shortname isn't in the alphabet or name is null.
     */
    public FlagArgument(String name, String description, Character shortName)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, false, false);
    }

    /**
     * A flag argument has no inherent value and no identifying character, rather it either is or isn't present in the
     * input.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @throws IllegalConstructorParameterException when shortname isn't in the alphabet or name is null.
     */
    public FlagArgument(String name, String description) {
        this(name, description, null);
    }

    @Override
    protected String getValueName() {
        return null;
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
