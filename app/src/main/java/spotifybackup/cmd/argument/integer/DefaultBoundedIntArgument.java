package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.argument.DefaultArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

/**
 * Integer argument with range checking and default value, has flag-like behaviour because it can be called without a
 * value. Argument throws exception at runtime if supplied value is out of range.
 */
public class DefaultBoundedIntArgument extends DefaultArgument {
    private final Integer min, max;
    private Integer value;

    /**
     * Integer argument with range checking and default value, has flag-like behaviour because it can be called without
     * a value. Argument throws exception at runtime if supplied value is out of range.
     */
    private DefaultBoundedIntArgument(Builder builder) {
        super(builder);
        this.min = builder.min;
        this.max = builder.max;
        this.value = builder.defaultValue;
    }

    private static String generateOutOfBoundsText(int min, int max, int value) {
        if (max == Integer.MAX_VALUE) {
            return "[" + value + "] is out of bounds (should be greater than or equal to " + min + ").";
        } else {
            return "[" + value + "] is out of bounds (should be greater than or equal to "
                    + min + " and less than or equal to " + max + ").";
        }
    }

    @Override
    protected String getValueName() {
        return "INTEGER";
    }

    private void setBoundedValue(Integer tempValue) throws MalformedInputException {
        if (tempValue >= min && tempValue <= max) {
            this.value = tempValue;
        } else {
            throw new MalformedInputException(generateOutOfBoundsText(min, max, tempValue));
        }
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        try {
            setBoundedValue(Integer.valueOf(value));
        } catch (NumberFormatException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as an integer.");
        }
    }

    public static class Builder extends Argument.Builder<Builder> {
        private Integer min, max, defaultValue;

        public Builder() {
            super(false, true);
        }

        /** @param minimum Minimum value integer should be (also applied to defaultValue). */
        public Builder minimum(int minimum) {
            this.min = minimum;
            return this;
        }

        /** @param maximum Maximum value integer should be (also applied to defaultValue). */
        public Builder maximum(int maximum) {
            this.max = maximum;
            return this;
        }

        /** @param defaultValue The value produced if the argument is absent from or undefined in the command line. */
        public Builder defaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
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
            if (defaultValue == null) {
                throw new IllegalConstructorParameterException("defaultValue can not be null value.");
            }
            if (max <= min) {
                throw new IllegalConstructorParameterException("maximum(" + max + ") should be greater than minimum("
                        + min + ").");
            }
            if (defaultValue < min || defaultValue > max) {
                throw new IllegalConstructorParameterException(
                        DefaultBoundedIntArgument.generateOutOfBoundsText(min, max, defaultValue));
            }
        }

        /**
         * @throws IllegalConstructorParameterException When trying assign name, minimum, maximum or defaultValue as
         *                                              null, defaultValue is out of range or assigning shortName a
         *                                              character not in the alphabet.
         */
        @Override
        public Argument build() throws IllegalConstructorParameterException {
            validateSuper();
            validateThis();
            return new DefaultBoundedIntArgument(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
