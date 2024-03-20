package spotifybackup.cmd.argument.string;

import spotifybackup.cmd.OptionallyPresent;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/** String argument with default value, has flag-like behaviour because it can be called without a value. */
public class DefaultStringArgument extends StringArgument implements OptionallyPresent {
    /** String argument with default value, has flag-like behaviour because it can be called without a value. */
    private DefaultStringArgument(Builder builder) {
        super(builder);
        super.value = builder.defaultValue;
    }

    @Override
    public boolean isPresent() {
        return isPresent;
    }

    @Override
    protected String getDescription() {
        return super.getDescription() + " Default value: [" + getValue() + "]";
    }

    public static class Builder extends StringArgument.Builder<Builder> {
        private String defaultValue;

        public Builder() {
            super(false, false);
        }

        /** @param defaultValue The value produced if the argument is absent from or undefined in the command line. */
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
            if (defaultValue == null) {
                throw new IllegalConstructorParameterException("defaultValue can not be null value.");
            }
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name or defaultValue as null or assigning
         *                                              shortName a character not in the alphabet.
         */
        @Override
        public DefaultStringArgument build() throws IllegalConstructorParameterException {
            validate();
            return new DefaultStringArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
