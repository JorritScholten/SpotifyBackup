package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.DefaultArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/**
 * Integer argument with default value, has flag-like behaviour because it can be called without a value.
 */
public class DefaultIntArgument extends IntArgument implements DefaultArgument<Integer> {
    /**
     * Integer argument with default value, has flag-like behaviour because it can be called without a value.
     */
    private DefaultIntArgument(Builder builder) {
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

    public static class Builder extends IntArgument.Builder<Builder> {
        private Integer defaultValue;

        public Builder() {
            super(false, false);
        }

        /** @param defaultValue The value produced if the argument is absent from or undefined in the command line. */
        public Builder defaultValue(int defaultValue) {
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
        public DefaultIntArgument build() throws IllegalConstructorParameterException {
            validate();
            return new DefaultIntArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
