package spotifybackup.cmd.argument;

import spotifybackup.cmd.Argument;

abstract public class DefaultArgument extends Argument {
    public DefaultArgument(String name, String description, Character shortName, boolean isMandatory, boolean hasValue) {
        super(name, description, shortName, isMandatory, hasValue);
    }

    public boolean isPresent() {
        return isPresent;
    }
}
