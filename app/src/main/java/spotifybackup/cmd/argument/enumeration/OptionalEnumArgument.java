package spotifybackup.cmd.argument.enumeration;

import spotifybackup.cmd.OptionalArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

import java.util.NoSuchElementException;

/**
 * Optional enum argument, program won't execute if flag is present but value is missing.
 * @param <E> target enum of argument.
 */
public class OptionalEnumArgument<E extends Enum<E>> extends EnumArgument<E> implements OptionalArgument<E> {
    /** Optional enum argument, program won't execute if flag is present but value is missing. */
    private OptionalEnumArgument(Builder<E> builder) {
        super(builder);
    }

    /**
     * @throws NoSuchElementException when trying to get value from argument not present in command line input.
     * @apiNote Always call {@link #isPresent()} first to avoid the NoSuchElementException.
     */
    @Override
    public E getValue() throws NoSuchElementException {
        if (!isPresent) throw new NoSuchElementException(name + " was not present in input, cannot get value.");
        return super.getValue();
    }

    @Override
    public boolean isPresent() {
        return super.isPresent;
    }

    public static class Builder<E extends Enum<E>> extends EnumArgument.Builder<Builder<E>, E> {
        public Builder() {
            super(false, true);
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name, description, minimum or maximum as null
         *                                              or assigning shortName a character not in the alphabet.
         */
        @Override
        public OptionalEnumArgument<E> build() throws IllegalConstructorParameterException {
            validate();
            return new OptionalEnumArgument<>(this);
        }

        @Override
        protected Builder<E> getThis() {
            return this;
        }
    }
}
