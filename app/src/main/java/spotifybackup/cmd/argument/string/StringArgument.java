package spotifybackup.cmd.argument.string;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.MalformedInputException;

abstract class StringArgument extends Argument<String> {
    String value;

    StringArgument(Builder<?> builder) {
        super(builder);
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

    abstract static class Builder<T extends Builder<T>> extends Argument.Builder<T, String> {
        protected Builder(boolean argMandatory, boolean valMandatory) {
            super(argMandatory, true, valMandatory);
        }
    }
}
