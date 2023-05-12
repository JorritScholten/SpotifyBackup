package spotifybackup.cmd;

public class BoundedIntArgument extends Argument {
    private final int min, max;
    private Integer value;

    public BoundedIntArgument(String name, String description, Character shortName, boolean isMandatory, int minimum)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, isMandatory, true);
        this.min = minimum;
        this.max = Integer.MAX_VALUE;
    }

    public BoundedIntArgument(String name, String description, Character shortName, boolean isMandatory, int minimum, int maximum)
            throws IllegalConstructorParameterException {
        super(name, description, shortName, isMandatory, true);
        this.min = minimum;
        this.max = maximum;
    }

    public BoundedIntArgument(String name, String description, boolean isMandatory, int minimum) {
        super(name, description, isMandatory, true);
        this.min = minimum;
        this.max = Integer.MAX_VALUE;
    }

    public BoundedIntArgument(String name, String description, boolean isMandatory, int minimum, int maximum) {
        super(name, description, isMandatory, true);
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
