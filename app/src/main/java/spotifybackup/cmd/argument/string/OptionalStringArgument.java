package spotifybackup.cmd.argument.string;

import spotifybackup.cmd.exception.IllegalConstructorParameterException;

/** Optional String argument, program won't execute if flag is present but value is missing. */
public class OptionalStringArgument extends StringArgument {
    /** Optional String argument, program won't execute if flag is present but value is missing. */
    private OptionalStringArgument(Builder builder) {
        super(builder);
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
