package spotifybackup.cmd;

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
        if (Pattern.compile("[a-zA-Z]").matcher(shortName.toString()).find()) {
            this.shortName = shortName;
        } else {
            throw new IllegalConstructorParameterException("Character used for short name should be in the alphabet.");
        }
        this.isMandatory = isMandatory;
        this.hasValue = hasValue;
    }

    /**
     * @throws IllegalConstructorParameterException when name is null.
     */
    protected Argument(String name, String description, boolean isMandatory, boolean hasValue)
            throws IllegalConstructorParameterException {
        if (name == null) {
            throw new IllegalConstructorParameterException("Argument name can not be null value.");
        } else {
            this.name = name;
        }
        this.description = description;
        this.shortName = null;
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
        String helpString = "  ";
        if (shortName != null) {
            helpString += "-" + shortName;
            if (hasValue) {
                helpString += " ";
                if (!isMandatory) {
                    helpString += "[";
                }
                helpString += getValueName();
                if (!isMandatory) {
                    helpString += "]";
                }
            }
            helpString += ", ";
        }
        helpString += "--" + name;
        if (hasValue) {
            helpString += " ";
            if (!isMandatory) {
                helpString += "[";
            }
            helpString += getValueName();
            if (!isMandatory) {
                helpString += "]";
            }
        }
        if (helpString.length() >= (nameWidth - 1)) {
            helpString += "\n" + " ".repeat(nameWidth);
        } else {
            helpString += " ".repeat(nameWidth - helpString.length());
        }
        if (description.length() < (maxWidth - nameWidth)) {
            helpString += description;
            return helpString;
        } else {
            final int descriptionWidth = maxWidth - nameWidth;
            for (int i = 0; (description.length() - (descriptionWidth * i)) > 0; i++) {
                int endIndex = descriptionWidth * (i + 1) < description.length() ? descriptionWidth * (i + 1) : description.length() - 1;
                helpString += description.substring(descriptionWidth * i, endIndex) + "\n" + " ".repeat(nameWidth);
            }
            return helpString.substring(0, helpString.length() - (1 + nameWidth));
        }
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
