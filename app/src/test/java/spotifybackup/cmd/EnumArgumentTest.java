package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import spotifybackup.cmd.argument.enumeration.DefaultEnumArgument;
import spotifybackup.cmd.argument.enumeration.MandatoryEnumArgument;
import spotifybackup.cmd.argument.enumeration.OptionalEnumArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableCmdParserTests", matches = "true")
class EnumArgumentTest {
    @Test
    void mandatory_argument_loads_value() {
        // Arrange
        final TestEnum value = TestEnum.ABC;
        final String[] args = {"-e", value.toString()};
        final String name = "extra";
        final var enumArg = new MandatoryEnumArgument.Builder<TestEnum>()
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
    void enum_argument_ensures_className_not_null() {
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

    @Test
    void mandatory_argument_rejects_wrong_value() {
        // Arrange
        final String name = "extra";
        final var enumArg = new MandatoryEnumArgument.Builder<TestEnum>()
                .enumClass(TestEnum.class)
                .name(name)
                .description("")
                .shortName('e')
                .build();
        var parser = new CmdParser.Builder().argument(enumArg).build();

        // Act
        final OtherEnum value = OtherEnum.ONE;
        final String[] args = {"-e", value.toString()};

        // Assert
        assertThrows(MalformedInputException.class, () -> parser.parseArguments(args));
    }

    @Test
    void default_argument_loads_value() {
        // Arrange
        final TestEnum value = TestEnum.ABC;
        final TestEnum defaultValue = TestEnum.none;
        final String[] args = {"-e", value.toString()};
        final String name = "extra";
        final var enumArg = new DefaultEnumArgument.Builder<TestEnum>()
                .enumClass(TestEnum.class)
                .name(name)
                .description("")
                .defaultValue(defaultValue)
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
        assertNotEquals(defaultValue, enumArg.getValue());
    }

    @Test
    void default_argument_loads_default_value() {
        // Arrange
        final TestEnum defaultValue = TestEnum.none;
        final String[] args = {};
        final String name = "extra";
        final var enumArg = new DefaultEnumArgument.Builder<TestEnum>()
                .enumClass(TestEnum.class)
                .name(name)
                .description("")
                .defaultValue(defaultValue)
                .shortName('e')
                .build();
        var parser = new CmdParser.Builder().argument(enumArg).build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue, parser.getValue(name));
        });
        assertEquals(defaultValue, enumArg.getValue());
    }

    @Test
    void optional_argument_loads_value_from_name() {
        // Arrange
        final var name = "opt";
        final var value = TestEnum.ABC;
        final String[] args = {"--" + name, value.name()};
        final var arg = new OptionalEnumArgument.Builder<EnumArgumentTest.TestEnum>()
                .name(name)
                .description("")
                .shortName('o')
                .enumClass(TestEnum.class)
                .build();
        final var argParser = new CmdParser.Builder()
                .argument(arg)
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertEquals(value, arg.getValue());
    }

    @Test
    void optional_argument_loads_value_from_shortname() {
        // Arrange
        final var name = "opt";
        final var value = TestEnum.ABC;
        final String[] args = {"-o", value.name()};
        final var arg = new OptionalEnumArgument.Builder<EnumArgumentTest.TestEnum>()
                .name(name)
                .description("")
                .shortName('o')
                .enumClass(TestEnum.class)
                .build();
        final var argParser = new CmdParser.Builder()
                .argument(arg)
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertEquals(value, arg.getValue());
    }

    @Test
    void optional_argument_missing_in_input_throws_exception_when_getting_value() {
        // Arrange
        final var name = "opt";
        final String[] args = {};
        final var arg = new OptionalEnumArgument.Builder<EnumArgumentTest.TestEnum>()
                .name(name)
                .description("")
                .shortName('o')
                .enumClass(TestEnum.class)
                .build();
        final var argParser = new CmdParser.Builder()
                .argument(arg)
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertFalse(arg::isPresent);
        assertThrows(NoSuchElementException.class, arg::getValue);
    }

    public enum TestEnum {
        abc,
        ABC,
        none;
    }

    public enum OtherEnum {
        ONE,
        TWO,
        THREE;
    }
}
