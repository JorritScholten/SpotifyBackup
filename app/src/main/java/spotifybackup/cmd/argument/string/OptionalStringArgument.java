package spotifybackup.cmd.argument.string;

import spotifybackup.cmd.OptionallyPresent;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

import java.util.NoSuchElementException;

/** Optional string argument, program won't execute if flag is present but value is missing. */
public class OptionalStringArgument extends StringArgument implements OptionallyPresent {
    /** Optional string argument, program won't execute if flag is present but value is missing. */
    private OptionalStringArgument(Builder builder) {
        super(builder);
    }

    /**
     * @throws NoSuchElementException when trying to get value from argument not present in command line input.
     * @apiNote Always call {@link #isPresent()} first to avoid the NoSuchElementException.
     */
    @Override
    public String getValue() throws NoSuchElementException {
        if (!isPresent) throw new NoSuchElementException(name + " was not present in input, cannot get value.");
        return super.getValue();
    }

    @Override
    public boolean isPresent() {
        return super.isPresent;
    }

    public static class Builder extends StringArgument.Builder<Builder> {
        public Builder() {
            super(false, true);
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a
         *                                              character not in the alphabet.
         */
        @Override
        public OptionalStringArgument build() {
            validate();
            return new OptionalStringArgument(this);
        }

        @Override
        protected Builder getThis() {return this;}
    }
}
