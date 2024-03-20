package spotifybackup.cmd.argument.file;

import spotifybackup.cmd.OptionallyPresent;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

import java.io.File;

/**
 * File path argument with default value, has flag-like behaviour because it can be called without a value. Argument
 * throws exception at runtime if supplied value does not adhere to isFolder rule.
 */
public class DefaultFilePathArgument extends FilePathArgument implements OptionallyPresent {
    /**
     * File path argument with default value, has flag-like behaviour because it can be called without a value. Argument
     * throws exception at runtime if supplied value does not adhere to isFolder rule.
     */
    private DefaultFilePathArgument(Builder builder) {
        super(builder);
        super.value = builder.defaultValue;
    }

    @Override
    public boolean isPresent() {
        return isPresent;
    }

    @Override
    protected String getDescription() {
        return super.getDescription() + " Default value: [" + getValue().getAbsolutePath() + "]";
    }

    public static class Builder extends FilePathArgument.Builder<Builder> {
        private File defaultValue;

        public Builder() {
            super(false, false);
        }

        /** @param defaultValue The value produced if the argument is absent from or undefined in the command line. */
        public Builder defaultValue(File defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
            if (defaultValue == null) {
                throw new IllegalConstructorParameterException("defaultValue can not be null value.");
            }
            if (FilePathArgument.errorWithValue(defaultValue, getIsFolder())) {
                throw new IllegalConstructorParameterException(
                        FilePathArgument.generateErrorString(defaultValue, getIsFolder()));
            }
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name or defaultValue as null, or defaultValue
         *                                              does not adhere to whether it is a file or not or assigning
         *                                              shortName a character not in the alphabet.
         */
        @Override
        public DefaultFilePathArgument build() throws IllegalConstructorParameterException {
            validate();
            return new DefaultFilePathArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
