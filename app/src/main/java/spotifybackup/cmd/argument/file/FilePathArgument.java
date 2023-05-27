package spotifybackup.cmd.argument.file;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.MalformedInputException;

import java.io.File;

class FilePathArgument extends Argument {
    final boolean isFolder;
    File value;

    FilePathArgument(Builder<?> builder) {
        super(builder);
        this.isFolder = builder.isFolder;
    }

    static String generateErrorString(File value, boolean isFolder) {
        if (value.isDirectory() && !isFolder) {
            return "Supplied filepath points to a directory rather than a file: " + value;
        } else if (value.isFile() && isFolder) {
            return "Supplied filepath points to a file rather than a directory: " + value;
        }
        return "Supplied filepath cannot be checked: " + value;
    }

    static boolean errorWithValue(File value, boolean isFolder) {
        return (value.isDirectory() && !isFolder) || (value.isFile() && isFolder) || (value.exists() && !value.canRead());
    }

    @Override
    protected String getValueName() {
        return "FILEPATH";
    }

    @Override
    public File getValue() {
        return value;
    }

    @Override
    protected void setValue(final String value) throws MalformedInputException {
        try {
            this.value = new File(value);
        } catch (NullPointerException e) {
            throw new MalformedInputException("setValue can not be called with a null value.");
        }
        if (errorWithValue(this.value, isFolder)) {
            throw new MalformedInputException(generateErrorString(this.value, isFolder));
        }
    }

    abstract static class Builder<T extends Builder<T>> extends Argument.Builder<T> {
        Boolean isFolder;

        Builder(boolean isMandatory) {
            super(isMandatory, true);
        }

        /**
         * Indicate that supplied filepath should be a directory, Argument throws runtime error if supplied value does
         * not match.
         */
        public T isDirectory() {
            isFolder = true;
            return getThis();
        }

        /**
         * Indicate that supplied filepath should be a file, Argument throws runtime error if supplied value does not
         * match.
         */
        public T isFile() {
            isFolder = false;
            return getThis();
        }

        Boolean getIsFolder() {
            return isFolder;
        }
    }
}
