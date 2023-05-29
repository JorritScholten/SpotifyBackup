package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.MalformedInputException;

abstract class IntArgument extends Argument {
    int value;

    IntArgument(Builder<?> builder) {
        super(builder);
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
            this.value = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as an integer.");
        }
    }

    abstract protected static class Builder<T extends Builder<T>> extends Argument.Builder<T> {
        protected Builder(boolean isMandatory) {
            super(isMandatory, true);
        }
    }
}
