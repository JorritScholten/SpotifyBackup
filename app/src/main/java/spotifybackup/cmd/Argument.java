package spotifybackup.cmd;

abstract public class Argument {
    /** true if present in input. */
    protected boolean isPresent;
    /** If true and missing from input then program will throw an Exception, flags cannot be mandatory. */
    protected boolean isMandatory;
    protected Character shortName;
    protected String name;
    protected String description;

    /**
     * Gets value inherent to argument.
     * @return value of argument.
     */
    abstract public Object getValue();
}
