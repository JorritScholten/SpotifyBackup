package spotifybackup.cmd;

/**
 * A flag argument has no inherent value, rather it either is or isn't present in the input. A flag cannot be
 * mandatory.
 */
public class FlagArgument extends Argument {
    /**
     * Returns true if flag is present in input.
     * @return true if flag is present in input.
     */
    @Override
    public Boolean getValue() {
        return (Boolean) isPresent;
    }
}
