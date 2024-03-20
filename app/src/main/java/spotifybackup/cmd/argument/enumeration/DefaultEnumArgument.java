package spotifybackup.cmd.argument.enumeration;

import spotifybackup.cmd.OptionallyPresent;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/**
 * Enum argument with default value, has flag-like behaviour because it can be called without a value. Argument throws
 * exception at runtime if supplied value is out of range.
 * @param <E> target enum of argument.
 */
public class DefaultEnumArgument<E extends Enum<E>> extends EnumArgument<E> implements OptionallyPresent {
    /**
     * Enum argument with default value, has flag-like behaviour because it can be called without a value. Argument
     * throws exception at runtime if supplied value is out of range.
     */
    private DefaultEnumArgument(Builder<E> builder) {
        super(builder);
        super.value = builder.defaultValue;
    }

    @Override
    public boolean isPresent() {
        return super.isPresent;
    }

    @Override
    protected String getDescription() {
        return super.getDescription() + " Default value: [" + getValue() + "]";
    }

    public static class Builder<E extends Enum<E>> extends EnumArgument.Builder<Builder<E>, E> {
        private E defaultValue;

        public Builder() {
            super(false, false);
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name, description, minimum or maximum as null
         *                                              or assigning shortName a character not in the alphabet.
         */
        @Override
        public DefaultEnumArgument<E> build() throws IllegalConstructorParameterException {
            validate();
            return new DefaultEnumArgument<>(this);
        }

        @Override
        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
            if (defaultValue == null)
                throw new IllegalConstructorParameterException("defaultValue can not be null value.");
        }

        /** @param defaultValue The value produced if the argument is absent from or undefined in the command line. */
        public Builder<E> defaultValue(E defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        protected Builder<E> getThis() {
            return this;
        }
    }
}
