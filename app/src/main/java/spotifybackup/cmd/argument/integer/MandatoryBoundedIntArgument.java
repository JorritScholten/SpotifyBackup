package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/**
 * Mandatory integer argument with range checking, program won't execute if missing. Argument throws exception at
 * runtime if supplied value is out of range.
 */
public class MandatoryBoundedIntArgument extends BoundedIntArgument {
    /**
     * Mandatory integer argument with range checking, program won't execute if missing. Argument throws exception at
     * runtime if supplied value is out of range.
     */
    private MandatoryBoundedIntArgument(Builder builder) {
        super(builder);
    }

    public static class Builder extends BoundedIntArgument.Builder<Builder> {
        public Builder() {
            super(true);
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name, description, minimum or maximum as null
         *                                              or assigning shortName a character not in the alphabet.
         */
        @Override
        public MandatoryBoundedIntArgument build() throws IllegalConstructorParameterException {
            validate();
            return new MandatoryBoundedIntArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
