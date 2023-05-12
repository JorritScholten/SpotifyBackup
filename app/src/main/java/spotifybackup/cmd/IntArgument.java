package spotifybackup.cmd;

public class IntArgument extends Argument {
    private Integer value;

    public IntArgument(String name, String description, Character shortName, boolean isMandatory) {
        hasValue = true;
        this.isMandatory = isMandatory;
        this.name = name;
        this.description = description;
        this.shortName = shortName;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    protected void setValue(String value) throws MalformedInputException {
        try {
            this.value = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as an integer.");
        }
    }
}
