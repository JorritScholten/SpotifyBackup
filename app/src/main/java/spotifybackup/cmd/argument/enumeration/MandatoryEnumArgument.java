package spotifybackup.cmd.argument.enumeration;

import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/**
 * Mandatory enum argument, program won't execute if missing. Argument throws exception at runtime if supplied value is
 * out of range.
 * @param <E> target enum of argument.
 */
public class MandatoryEnumArgument<E extends Enum<E>> extends EnumArgument<E> {
    /**
     * Mandatory enum argument, program won't execute if missing. Argument throws exception at runtime if supplied value is
     * out of range.
     */
    private MandatoryEnumArgument(Builder<E> builder) {
        super(builder);
    }

    public static class Builder<E extends Enum<E>> extends EnumArgument.Builder<Builder<E>, E> {
        public Builder() {
            super(true);
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name, description, minimum or maximum as null
         *                                              or assigning shortName a character not in the alphabet.
         */
        @Override
        public MandatoryEnumArgument<E> build() throws IllegalConstructorParameterException {
            validate();
            return new MandatoryEnumArgument<>(this);
        }

        @Override
        protected Builder<E> getThis() {
            return this;
        }
    }
}
