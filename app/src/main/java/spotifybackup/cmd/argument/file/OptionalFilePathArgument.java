package spotifybackup.cmd.argument.file;

import spotifybackup.cmd.OptionallyPresent;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

import java.io.File;
import java.util.NoSuchElementException;

/**
 * Optional file path argument, program won't execute if flag is present but value is missing. Argument throws exception
 * at runtime if supplied value does not adhere to isFolder rule.
 */
public class OptionalFilePathArgument extends FilePathArgument implements OptionallyPresent {
    /**
     * Optional file path argument, program won't execute if flag is present but value is missing. Argument throws exception
     * at runtime if supplied value does not adhere to isFolder rule.
     */
    private OptionalFilePathArgument(Builder builder) {
        super(builder);
    }

    /**
     * @throws NoSuchElementException when trying to get value from argument not present in command line input.
     * @apiNote Always call {@link #isPresent()} first to avoid the NoSuchElementException.
     */
    @Override
    public File getValue() throws NoSuchElementException {
        if (!isPresent) throw new NoSuchElementException(name + " was not present in input, cannot get value.");
        return super.getValue();
    }

    @Override
    public boolean isPresent() {
        return super.isPresent;
    }

    public static class Builder extends FilePathArgument.Builder<Builder> {
        public Builder() {
            super(false, true);
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name, description, minimum or maximum as null
         *                                              or assigning shortName a character not in the alphabet.
         */
        @Override
        public OptionalFilePathArgument build() throws IllegalConstructorParameterException {
            validate();
            return new OptionalFilePathArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
