package spotifybackup.cmd.argument.string;

import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/** Mandatory String argument, program won't execute if missing. */
public class MandatoryStringArgument extends StringArgument {
    /** Mandatory String argument, program won't execute if missing. */
    private MandatoryStringArgument(Builder builder)
            throws IllegalConstructorParameterException {
        super(builder);
    }

    public static class Builder extends StringArgument.Builder<Builder> {
        public Builder() {
            super(true);
        }

        @Override
        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a
         *                                              character not in the alphabet.
         */
        @Override
        public MandatoryStringArgument build() throws IllegalConstructorParameterException {
            validate();
            return new MandatoryStringArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
