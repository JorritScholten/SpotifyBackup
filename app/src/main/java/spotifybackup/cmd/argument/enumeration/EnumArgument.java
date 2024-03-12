package spotifybackup.cmd.argument.enumeration;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.util.Arrays;

abstract class EnumArgument<E extends Enum<E>> extends Argument<E> {
    final Class<E> enumClass;
    E value;

    EnumArgument(Builder<?, E> builder) {
        super(builder);
        this.enumClass = builder.enumClass;
    }

    @Override
    public E getValue() {
        return value;
    }

    @Override
    protected void setValue(String value) throws MalformedInputException {
        try {
            this.value = E.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            throw new MalformedInputException("[" + value + "] can't be parsed as a value of " + enumClass.getName() +
                    " which has values: " + String.join(", ", Arrays.toString(enumClass.getEnumConstants())));
        }
    }

    @Override
    protected String getValueName() {
        return "ENUM";
    }

    protected abstract static class Builder<T extends Builder<T, E>, E extends Enum<E>> extends Argument.Builder<T, E> {
        private Class<E> enumClass;

        protected Builder(boolean isMandatory) {
            super(isMandatory, true);
        }

        /** @param enumClass Class capture of enum this argument references, this is needed due to a java language
         *                    quirk. */
        public T enumClass(Class<E> enumClass) {
            this.enumClass = enumClass;
            return getThis();
        }

        @Override
        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
            if (enumClass == null) throw new IllegalConstructorParameterException("className can not be null value.");
        }
    }
}
