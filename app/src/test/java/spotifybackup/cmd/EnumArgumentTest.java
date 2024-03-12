package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import spotifybackup.cmd.argument.enumeration.MandatoryEnumArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableCmdParserTests", matches = "true")
class EnumArgumentTest {
    @Test
    void mandatory_argument_loads_value() {
        // Arrange
        final TestEnum value = TestEnum.ABC;
        final String[] args = {"-e", value.toString()};
        final String name = "extra";
        final var enumArg =  new MandatoryEnumArgument.Builder<TestEnum>()
                .enumClass(TestEnum.class)
                .name(name)
                .description("")
                .shortName('e')
                .build();
        var parser = new CmdParser.Builder().argument(enumArg).build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue(name));
        });
        assertEquals(value, enumArg.getValue());
    }

    @Test
    void mandatory_argument_ensures_className_not_null() {
        // Arrange
        final TestEnum value = TestEnum.ABC;
        final String[] args = {"-e", value.toString()};
        final String name = "extra";
        var builder = new MandatoryEnumArgument.Builder<TestEnum>()
                .name(name)
                .description("")
                .shortName('e');

        // Act
//        builder.enumClass(TestEnum.class);

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    public enum TestEnum {
        abc,
        ABC,
        none;
    }

    public enum OtherEnum{
        ONE,
        TWO,
        THREE;
    }
}
