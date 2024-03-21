package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.OptionallyPresent;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

import java.util.NoSuchElementException;

/**
 * Optional integer argument with range checking, program won't execute if flag is present but value is missing.
 * Argument throws exception at runtime if supplied value is out of range.
 */
public class OptionalBoundedIntArgument extends BoundedIntArgument implements OptionallyPresent {
    /**
     * Optional integer argument with range checking, program won't execute if flag is present but value is missing.
     * Argument throws exception at runtime if supplied value is out of range.
     */
    private OptionalBoundedIntArgument(Builder builder) {
        super(builder);
    }

    /**
     * @throws NoSuchElementException when trying to get value from argument not present in command line input.
     * @apiNote Always call {@link #isPresent()} first to avoid the NoSuchElementException.
     */
    @Override
    public Integer getValue() throws NoSuchElementException {
        if (!isPresent) throw new NoSuchElementException(name + " was not present in input, cannot get value.");
        return super.getValue();
    }

    @Override
    public boolean isPresent() {
        return super.isPresent;
    }

    public static class Builder extends BoundedIntArgument.Builder<Builder> {
        public Builder() {
            super(false, true);
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a
         *                                              character not in the alphabet.
         */
        @Override
        public OptionalBoundedIntArgument build() {
            validate();
            return new OptionalBoundedIntArgument(this);
        }

        @Override
        protected Builder getThis() {return this;}
    }
}
