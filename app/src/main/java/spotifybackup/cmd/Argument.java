package spotifybackup.cmd;

abstract public class Argument {
    /** If true and missing from input then program will throw an Exception, flags cannot be mandatory. */
    protected final boolean isMandatory;
    protected final boolean hasValue;
    protected final Character shortName;
    protected final String name;
    protected final String description;
    /** true if present in input. */
    protected boolean isPresent;

    protected Argument(String name, String description, Character shortName, boolean isMandatory, boolean hasValue) {
        this.name = name;
        this.description = description;
        this.shortName = shortName;
        this.isMandatory = isMandatory;
        this.hasValue = hasValue;
    }

    protected Argument(String name, String description, boolean isMandatory, boolean hasValue) {
        this.name = name;
        this.description = description;
        this.shortName = null;
        this.isMandatory = isMandatory;
        this.hasValue = hasValue;
    }

    /**
     * Gets value inherent to argument.
     * @return value of argument.
     */
    abstract public Object getValue();

    /**
     * Set value of argument when parsing command line arguments.
     * @param value String from String[] args.
     */
    abstract protected void setValue(final String value) throws MalformedInputException;
}
