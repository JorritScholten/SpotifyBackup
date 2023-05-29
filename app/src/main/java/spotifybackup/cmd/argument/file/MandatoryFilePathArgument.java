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
    public MandatoryFilePathArgument(Builder builder) {
        super(builder);
    }

    public static class Builder extends FilePathArgument.Builder<Builder> {
        public Builder() {
            super(true);
        }

        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
            if (getIsFolder() == null) {
                throw new IllegalConstructorParameterException("Must specify whether value is file or directory.");
            }
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
