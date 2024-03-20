package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import spotifybackup.cmd.argument.integer.*;
import spotifybackup.cmd.argument.string.OptionalStringArgument;
import spotifybackup.cmd.exception.ArgumentsNotParsedException;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;
import spotifybackup.cmd.exception.MissingValueException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableCmdParserTests", matches = "true")
class IntArgumentTest {
    @Test
    void mandatory_argument_loads_positive_value() {
        // Arrange
        final Integer value = 34;
        final String[] args = {"-e", value.toString()};
        final String name = "extra";
        var parser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue(name));
        });
    }

    @Test
    void mandatory_argument_loads_negative_value() {
        // Arrange
        final Integer value = -34;
        final String[] args = {"-e", value.toString()};
        final String name = "extra";
        var parser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue(name));
        });
    }

    @Test
    void default_argument_loads_positive_value() {
        // Arrange
        final int value = 34, defaultValue = 12;
        final String[] args = {"-e", Integer.toString(value)};
        final String name = "extra";
        var parser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertNotEquals(defaultValue, parser.getValue(name));
            assertEquals(value, parser.getValue(name));
            assertTrue(parser.isPresent(name));
        });
    }

    @Test
    void default_argument_loads_negative_value() {
        // Arrange
        final int value = -34, defaultValue = 12;
        final String[] args = {"-e", Integer.toString(value)};
        final String name = "extra";
        var parser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertNotEquals(defaultValue, parser.getValue(name));
            assertEquals(value, parser.getValue(name));
            assertTrue(parser.isPresent(name));
        });
    }

    @Test
    void default_argument_validates_defaultValue_not_null() {
        // Arrange
        var builder = new DefaultBoundedIntArgument.Builder();
        builder.description("a description")
                .name("int")
                .minimum(0);

        // Act
        // builder.defaultValue()

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void default_argument_missing_from_input_returns_defaultValue() {
        // Arrange
        final int defaultValue = 12;
        final String[] args = {};
        final String name = "extra";
        var parser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name(name)
                        .description("")
                        .defaultValue(defaultValue)
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue, parser.getValue(name));
            assertFalse(parser.isPresent(name));
        });
    }

    @Test
    void default_argument_present_in_input_without_value_returns_defaultValue() {
        // Arrange
        final int defaultValue = 12;
        final String name = "extra";
        final String[] args = {("--" + name),};
        var parser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name(name)
                        .description("")
                        .defaultValue(defaultValue)
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue, parser.getValue(name));
            assertTrue(parser.isPresent(name));
        });
    }

    @Test
    void mandatory_argument_without_value_throws_exception() {
        // Arrange
        final String name = "extra";
        final String[] args = {"-he"};
        var argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name(name)
                        .shortName('e')
                        .description("")
                        .build())
                .addHelp()
                .build();

        // Act & Assert
        assertThrows(MissingValueException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void argument_with_illegal_float_value_throws_exception() {
        // Arrange
        final String[] args = {"-e", "21.2"};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .build();

        // Act & Assert
        assertThrows(MalformedInputException.class, () -> parser.parseArguments(args));
    }

    @Test
    void argument_with_illegal_string_value_throws_exception() {
        // Arrange
        final String[] args = {"-e", "sdf"};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .build();

        // Act & Assert
        assertThrows(MalformedInputException.class, () -> parser.parseArguments(args));
    }

    @Test
    void optional_argument_loads_value_from_name() {
        // Arrange
        final var name = "opt";
        final var value = 8;
        final String[] args = {"--" + name, String.valueOf(value)};
        final var arg = new OptionalIntArgument.Builder()
                .name(name)
                .description("")
                .shortName('o')
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
        final var value = 33;
        final String[] args = {"-o", String.valueOf(value)};
        final var arg = new OptionalIntArgument.Builder()
                .name(name)
                .description("")
                .shortName('o')
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
        final var arg = new OptionalIntArgument.Builder()
                .name(name)
                .description("")
                .shortName('o')
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
}
