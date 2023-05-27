package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

/**
 * Mandatory integer argument with range checking, program won't execute if missing. Argument throws exception at
 * runtime if supplied value is out of range.
 */
public class MandatoryBoundedIntArgument extends Argument {
    private final Integer min, max;
    private Integer value;

    /**
     * Mandatory integer argument with range checking, program won't execute if missing. Argument throws exception at
     * runtime if supplied value is out of range.
     */
    private MandatoryBoundedIntArgument(Builder builder) {
        super(builder);
        this.min = builder.min;
        this.max = builder.max;
    }

    @Override
    protected String getValueName() {
        return "INTEGER";
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        try {
            int tempValue = Integer.parseInt(value);
            if (tempValue >= min && tempValue <= max) {
                this.value = tempValue;
            } else {
                if (max == Integer.MAX_VALUE) {
                    throw new MalformedInputException(
                            "[" + value + "] is out of bounds (should be greater than or equal to " + min + ")."
                    );
                } else {
                    throw new MalformedInputException(
                            "[" + value + "] is out of bounds (should be greater than or equal to "
                                    + min + " and less than or equal to " + max + ")."
                    );
                }
            }
        } catch (NumberFormatException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as an integer.");
        }
    }


    public static class Builder extends Argument.Builder<Builder> {
        private Integer min, max;

        public Builder() {
            super(true, true);
        }

        /** @param minimum Minimum value integer should be. */
        public Builder minimum(int minimum) {
            this.min = minimum;
            return this;
        }

        /** @param maximum Maximum value integer should be. */
        public Builder maximum(int maximum) {
            this.max = maximum;
            return this;
        }

        @Override
        protected void validateThis() throws IllegalConstructorParameterException {
            if (min == null) {
                throw new IllegalConstructorParameterException("minimum can not be null value.");
            }
            if (max == null) {
//                throw new IllegalConstructorParameterException("maximum can not be null value.");
                max = Integer.MAX_VALUE;
            }
            if (max <= min) {
                throw new IllegalConstructorParameterException("maximum(" + max + ") should be greater than minimum("
                        + min + ").");
            }
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name, description, minimum or maximum as null
         *                                              or assigning shortName a character not in the alphabet.
         */
        @Override
        public Argument build() throws IllegalConstructorParameterException {
            validateSuper();
            validateThis();
            return new MandatoryBoundedIntArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
