package spotifybackup.cmd.argument.enumeration;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.util.Arrays;

public class MandatoryEnumArgument<E extends Enum<E>> extends Argument<E> {
    final Class<E> enumClass;
    E value;

    private MandatoryEnumArgument(Builder<E> builder) {
        super(builder);
        this.enumClass = builder.enumClass;
    }

    @Override
    protected String getValueName() {
        return "ENUM";
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

    public static class Builder<E extends Enum<E>> extends Argument.Builder<Builder<E>, E> {
        private Class<E> enumClass;

        public Builder() {
            super(true, true);
        }

        public MandatoryEnumArgument<E> build() throws IllegalConstructorParameterException {
            validate();
            return new MandatoryEnumArgument<>(this);
        }

        public Builder<E> enumClass(Class<E> enumClass) {
            this.enumClass = enumClass;
            return getThis();
        }

        @Override
        protected void validate() throws IllegalConstructorParameterException {
            super.validate();
            if (enumClass == null) throw new IllegalConstructorParameterException("className can not be null value.");
        }

        @Override
        protected Builder<E> getThis() {
            return this;
        }
    }
}
