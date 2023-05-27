package spotifybackup.cmd.argument;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/** A flag argument has no inherent value, rather it either is or isn't present in the input. */
public class FlagArgument extends Argument {
    /** A flag argument has no inherent value, rather it either is or isn't present in the input. */
    public FlagArgument(Builder builder) {
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

        @Override
        protected void validateThis() throws IllegalConstructorParameterException {
        }

        @Override
        public Argument build() throws IllegalConstructorParameterException {
            validateSuper();
            validateThis();
            return new FlagArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
