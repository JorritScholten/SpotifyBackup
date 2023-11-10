package spotifybackup.cmd;

import org.apache.commons.text.WordUtils;
import spotifybackup.cmd.exception.*;

import java.util.Formatter;

public abstract class Argument {
    /** If true and missing from input then program will throw an Exception, flags cannot be mandatory. */
    protected final boolean isMandatory;
    protected final boolean hasValue;
    protected final Character shortName;
    protected final String name;
    protected final String description;

    /** true if present in input. */
    protected boolean isPresent;

    protected Argument(Builder<?> builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.shortName = builder.shortName;
        this.isMandatory = builder.isMandatory;
        this.hasValue = builder.hasValue;
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

        // generate usage/name block
        try (Formatter formatter = new Formatter(helpText)) {
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
        }

        // switch to new line if usage/name block is too wide
        if (helpText.length() >= (nameWidth - 1)) {
            helpText.append("\n").append(" ".repeat(nameWidth));
        } else {
            helpText.append(" ".repeat(nameWidth - helpText.length()));
        }

        // add argument description with word wrapping
        final int descriptionWidth = maxWidth - nameWidth;
        final String newLineStr = "\n" + " ".repeat(nameWidth);
        helpText.append(WordUtils.wrap(getDescription(), descriptionWidth, newLineStr, false));
        return helpText.toString();
    }

    protected String getDescription() {
        return description;
    }

    /**
     * Test whether shortName has been assigned a value.
     * @return true if shortName is not null.
     */
    protected boolean hasShortName() {
        return shortName != null;
    }

    /** Sets isPresent to true, meant for lambda usage. */
    protected void confirmPresent() {
        isPresent = true;
    }

    protected boolean getHasValue() {
        return hasValue;
    }

    protected boolean getMandatory() {
        return isMandatory;
    }

    protected abstract String getValueName();

    /**
     * Gets value inherent to argument.
     * @return value of argument.
     */
    public abstract Object getValue();

    /**
     * Set value of argument when parsing command line arguments.
     * @param value String from String[] args.
     * @throws MalformedInputException when input string cannot be parsed as underlying type.
     */
    protected abstract void setValue(final String value) throws MalformedInputException;

    protected abstract static class Builder<T extends Builder<T>> {
        private final boolean isMandatory;
        private final boolean hasValue;
        private String name;
        private String description;
        private Character shortName;

        protected Builder(boolean isMandatory, boolean hasValue) {
            this.isMandatory = isMandatory;
            this.hasValue = hasValue;
        }

        /** @param name Identifying name of argument, --{name} is used as identifier. */
        public T name(String name) {
            this.name = name;
            return getThis();
        }

        /** @param description Description of argument printed in help. */
        public T description(String description) {
            this.description = description;
            return getThis();
        }

        /** @param shortName Identifying character of argument, -{Character} is used as identifier. */
        public T shortName(char shortName) {
            this.shortName = shortName;
            return getThis();
        }

        public abstract Argument build() throws IllegalConstructorParameterException;

        protected abstract T getThis();

        /**
         * @throws IllegalConstructorParameterException when shortname isn't in the alphabet, description or name is
         *                                              null.
         */
        protected void validate() throws IllegalConstructorParameterException {
            if (name == null) {
                throw new IllegalArgumentNameException("Argument name can not be null value.");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentNameException("Argument name can not be empty string.");
            }
            if (description == null) {
                throw new IllegalArgumentDescriptionException("Argument description can not be null value.");
            }
            if (shortName != null && !((shortName >= 'a' && shortName <= 'z') || (shortName >= 'A' && shortName <= 'Z'))) {
                throw new IllegalArgumentShortnameException("Character used for short name should be in the alphabet.");
            }
        }
    }
}
