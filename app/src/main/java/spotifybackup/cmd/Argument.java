package spotifybackup.cmd;

import java.util.Formatter;
import java.util.regex.Pattern;

abstract public class Argument {
    /**
     * If true and missing from input then program will throw an Exception, flags cannot be mandatory.
     */
    protected final boolean isMandatory;
    protected final boolean hasValue;
    protected final Character shortName;
    protected final String name;
    protected final String description;

    /**
     * true if present in input.
     */
    protected boolean isPresent;

    /**
     * @throws IllegalConstructorParameterException when shortname isn't in the alphabet or name is null.
     */
    protected Argument(String name, String description, Character shortName, boolean isMandatory, boolean hasValue)
            throws IllegalConstructorParameterException {
        if (name == null) {
            throw new IllegalConstructorParameterException("Argument name can not be null value.");
        } else {
            this.name = name;
        }
        this.description = description;
        if (shortName == null) {
            this.shortName = null;
        } else {
            if (Pattern.compile("[a-zA-Z]").matcher(shortName.toString()).find()) {
                this.shortName = shortName;
            } else {
                throw new IllegalConstructorParameterException("Character used for short name should be in the alphabet.");
            }
        }
        this.isMandatory = isMandatory;
        this.hasValue = hasValue;
    }

    /**
     * Get string representation of Argument to print to Command line.
     * @param nameWidth width of name and shortName printing block.
     * @param maxWidth  maximum width of terminal for description text wrapping.
     * @return Formatted String containing help printout representation of Argument.
     */
    protected String getHelp(int nameWidth, int maxWidth) {
        if (nameWidth <= 0 || maxWidth <= 0 || maxWidth <= nameWidth) {
            throw new IllegalArgumentException("Illegal value for nameWidth or maxWidth.");
        }
        StringBuilder helpText = new StringBuilder("  ");
        Formatter formatter = new Formatter(helpText);

        // generate usage
        if (isMandatory) {
            if (hasShortName()) {
                formatter.format("-%c%s, ", shortName, hasValue ? " " + getValueName() : "");
            }
            formatter.format("--%s%s", name, hasValue ? " " + getValueName() : "");
        } else {
            if (hasShortName()) {
                formatter.format("-%c%s, ", shortName, hasValue ? " [" + getValueName() + "]" : "");
            }
            formatter.format("--%s%s", name, hasValue ? " [" + getValueName() + "]" : "");
        }

        // switch to new line if usage is too wide
        if (helpText.length() >= (nameWidth - 1)) {
            helpText.append("\n").append(" ".repeat(nameWidth));
        } else {
            helpText.append(" ".repeat(nameWidth - helpText.length()));
        }

        // add argument description with terrible word wrapping TODO: wrap on word boundary instead of fixed width
        if (description.length() < (maxWidth - nameWidth)) {
            helpText.append(description);
            return helpText.toString();
        } else {
            final int descriptionWidth = maxWidth - nameWidth;
            for (int i = 0; (description.length() - (descriptionWidth * i)) > 0; i++) {
                int endIndex = descriptionWidth * (i + 1) < description.length() ? descriptionWidth * (i + 1) : description.length() - 1;
                formatter.format("%s\n%s", description.substring(descriptionWidth * i, endIndex), " ".repeat(nameWidth));
            }
            return helpText.substring(0, helpText.length() - (1 + nameWidth));
        }
    }

    /**
     * Test whether shortName has been assigned a value.
     * @return true if shortName is not null.
     */
    protected boolean hasShortName() {
        return shortName != null;
    }

    abstract protected String getValueName();

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
