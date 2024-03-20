package spotifybackup.cmd.argument.integer;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

abstract class BoundedIntArgument extends Argument<Integer> {
    final Integer min;
    final Integer max;
    Integer value;

    BoundedIntArgument(Builder<?> builder) {
        super(builder);
        this.min = builder.min;
        this.max = builder.max;
    }

    static String generateOutOfBoundsText(int min, int max, int value) {
        if (max == Integer.MAX_VALUE) {
            return "[" + value + "] is out of bounds (should be greater than or equal to " + min + ").";
        } else {
            return "[" + value + "] is out of bounds (should be greater than or equal to " + min
                    + " and less than or equal to " + max + ").";
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

    protected abstract static class Builder<T extends Builder<T>> extends Argument.Builder<T, Integer> {
        private Integer min;
        private Integer max;

        protected Builder(boolean argMandatory, boolean valMandatory) {
            super(argMandatory, true, valMandatory);
        }

        /** @param minimum Minimum value integer should be (also applied to defaultValue). */
        public T minimum(int minimum) {
            this.min = minimum;
            return getThis();
        }

        /** @param maximum Maximum value integer should be (also applied to defaultValue). */
        public T maximum(int maximum) {
            this.max = maximum;
            return getThis();
        }

        Integer getMax() {
            return max;
        }

        Integer getMin() {
            return min;
        }

        @Override
        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
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
    }
}
