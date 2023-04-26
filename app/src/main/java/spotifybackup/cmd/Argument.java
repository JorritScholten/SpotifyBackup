package spotifybackup.cmd;

abstract public class Argument {
    /** true if present in input. */
    protected boolean isPresent;
    /** If true and missing from input then program will throw an Exception, flags cannot be mandatory. */
    protected boolean isMandatory;
    protected boolean hasValue;
    protected Character shortName;
    protected String name;
    protected String description;
    private Object value;

    /**
     * Gets value inherent to argument.
     * @return value of argument.
     */
    abstract public Object getValue();

    /**
     * Set value of argument when parsing command line arguments.
     * @param value String from String[] args.
     */
    abstract protected void setValue(String value);
}
