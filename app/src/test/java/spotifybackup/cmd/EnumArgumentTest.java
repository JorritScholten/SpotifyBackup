package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import spotifybackup.cmd.argument.enumeration.MandatoryEnumArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableCmdParserTests", matches = "true")
class EnumArgumentTest {
    @Test
    void mandatory_argument_loads_value() {
        // Arrange
        final TestEnum value = TestEnum.ABC;
        final String[] args = {"-e", value.toString()};
        final String name = "extra";
        final var enumArg =  new MandatoryEnumArgument.Builder()
                .name(name)
                .description("")
                .shortName('e')
                .build(TestEnum.class);
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
    void mandatory_argument_detects_wrong_enum() {
        // Arrange
        final TestEnum value = TestEnum.ABC;
        final String[] args = {"-e", value.toString()};
        final String name = "extra";

        // Act
        var parser = new CmdParser.Builder().argument(new MandatoryEnumArgument.Builder()
                .name(name)
                .description("")
                .shortName('e')
                .build(OtherEnum.class)).build();

        // Assert
        assertThrows(MalformedInputException.class, () -> parser.parseArguments(args));
    }

    @Test
    void mandatory_argument_warns_against_raw_type_builder() {
        // Arrange
        final String name = "extra";
        final var enumArgBuilder =  new MandatoryEnumArgument.Builder()
                .name(name)
                .description("")
                .shortName('e');

        // Act & Assert
        assertThrows(IllegalConstructorParameterException.class, enumArgBuilder::build);
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
