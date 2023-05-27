package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/** Mandatory integer argument, program won't execute if missing. */
public class MandatoryIntArgument extends IntArgument {
    /** Mandatory integer argument, program won't execute if missing. */
    private MandatoryIntArgument(Builder builder) {
        super(builder);
    }

    public static class Builder extends IntArgument.Builder<Builder> {
        public Builder() {
            super(true);
        }

        @Override
        protected void validateThis() throws IllegalConstructorParameterException {
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name as null or assigning shortName a
         *                                              character not in the alphabet.
         */
        @Override
        public MandatoryIntArgument build() throws IllegalConstructorParameterException {
            validateSuper();
            return new MandatoryIntArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
