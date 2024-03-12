package spotifybackup.cmd.argument.enumeration;

import spotifybackup.cmd.Argument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.util.Arrays;

public class MandatoryEnumArgument<E extends Enum<E>> extends Argument {
    final Class<E> enumClass;
    E value;

    private MandatoryEnumArgument(Builder builder, Class<E> enumClass) {
        super(builder);
        this.enumClass = enumClass;
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

    public static class Builder extends Argument.Builder<Builder> {
        public Builder() {
            super(true, true);
        }

        /**
         * @throws IllegalConstructorParameterException When used at all.
         * @apiNote Do not use, can't be omitted due to class structure.
         * @deprecated Raw type builder call not to be used.
         */
        @Override
        @Deprecated
        public MandatoryEnumArgument build() throws IllegalConstructorParameterException {
            validate();
            throw new IllegalConstructorParameterException("Can't use raw type builder for Generic argument.");
        }

        public <E extends Enum<E>> MandatoryEnumArgument<E> build(Class<E> enumClass)
                throws IllegalConstructorParameterException {
            validate();
            return new MandatoryEnumArgument<>(this, enumClass);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
