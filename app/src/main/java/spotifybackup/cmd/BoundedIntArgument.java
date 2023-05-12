package spotifybackup.cmd;

public class BoundedIntArgument extends Argument {
    private final int min, max;
    private Integer value;

    public BoundedIntArgument(String name, String description, Character shortName, Integer minimum)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
        this.min = minimum;
        this.max = Integer.MAX_VALUE;
    }

    public BoundedIntArgument(String name, String description, Character shortName, Integer minimum, Integer maximum)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, true, true);
        this.min = minimum;
        this.max = maximum;
    }

    public BoundedIntArgument(String name, String description, Integer minimum) {
        super(name, description, true, true);
        this.min = minimum;
        this.max = Integer.MAX_VALUE;
    }

    public BoundedIntArgument(String name, String description, Integer minimum, Integer maximum) {
        super(name, description, true, true);
        this.min = minimum;
        this.max = maximum;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        try {
            var tempValue = Integer.valueOf(value);
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
}
