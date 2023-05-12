package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

public class MandatoryIntArgument extends Argument {
    private Integer value;

    /**
     * Mandatory integer argument, program won't execute if missing.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @param shortName   Identifying character of argument, -{Character} is used as identifier.
     * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a character
     *                                              not in the alphabet.
     */
    public MandatoryIntArgument(String name, String description, Character shortName)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
    }

    /**
     * Mandatory integer argument without identifying character, program won't execute if missing.
     * @param name        Identifying name of argument, --{name} is used as identifier.
     * @param description Description of argument printed in help.
     * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a character
     *                                              not in the alphabet.
     */
    public MandatoryIntArgument(String name, String description) {
        this(name, description, null);
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
