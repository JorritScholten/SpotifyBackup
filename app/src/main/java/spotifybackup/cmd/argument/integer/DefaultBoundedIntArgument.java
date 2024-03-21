package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.DefaultArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/**
 * Integer argument with range checking and default value, has flag-like behaviour because it can be called without a
 * value. Argument throws exception at runtime if supplied value is out of range.
 */
public class DefaultBoundedIntArgument extends BoundedIntArgument implements DefaultArgument<Integer> {

    /**
     * Integer argument with range checking and default value, has flag-like behaviour because it can be called without
     * a value. Argument throws exception at runtime if supplied value is out of range.
     */
    private DefaultBoundedIntArgument(Builder builder) {
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

    public static class Builder extends BoundedIntArgument.Builder<Builder> {
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
            if (defaultValue < getMin() || defaultValue > getMax()) {
                throw new IllegalConstructorParameterException(
                        BoundedIntArgument.generateOutOfBoundsText(getMin(), getMax(), defaultValue));
            }
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name, minimum, maximum or defaultValue as
         *                                              null, defaultValue is out of range or assigning shortName a
         *                                              character not in the alphabet.
         */
        @Override
        public DefaultBoundedIntArgument build() throws IllegalConstructorParameterException {
            validate();
            return new DefaultBoundedIntArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
