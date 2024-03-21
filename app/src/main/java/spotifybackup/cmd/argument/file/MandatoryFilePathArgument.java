package spotifybackup.cmd.argument.file;

import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/**
 * Mandatory file path argument, program won't execute if missing. Argument throws exception at runtime if supplied
 * value does not adhere to isFolder rule.
 */
public class MandatoryFilePathArgument extends FilePathArgument {
    /**
     * Mandatory file path argument, program won't execute if missing. Argument throws exception at runtime if supplied
     * value does not adhere to isFolder rule.
     */
    private MandatoryFilePathArgument(Builder builder) {
        super(builder);
    }

    public static class Builder extends FilePathArgument.Builder<Builder> {
        public Builder() {
            super(true, true);
        }

        @Override
        public MandatoryFilePathArgument build() throws IllegalConstructorParameterException {
            validate();
            return new MandatoryFilePathArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
