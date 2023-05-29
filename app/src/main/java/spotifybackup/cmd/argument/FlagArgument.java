package spotifybackup.cmd.argument;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/** A flag argument has no inherent value, rather it either is or isn't present in the input. */
public class FlagArgument extends Argument {
    /** A flag argument has no inherent value, rather it either is or isn't present in the input. */
    private FlagArgument(Builder builder) {
        super(builder);
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

    public static class Builder extends Argument.Builder<Builder> {
        public Builder() {
            super(false, false);
        }

        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
        }

        @Override
        public FlagArgument build() throws IllegalConstructorParameterException {
            validate();
            return new FlagArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
