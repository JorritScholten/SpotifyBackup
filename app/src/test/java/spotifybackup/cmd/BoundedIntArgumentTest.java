package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import spotifybackup.cmd.argument.integer.DefaultBoundedIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryBoundedIntArgument;
import spotifybackup.cmd.argument.integer.OptionalBoundedIntArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;
import spotifybackup.cmd.exception.MissingValueException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableCmdParserTests", matches = "true")
class BoundedIntArgumentTest {
    @Test
    void mandatory_argument_rejects_below_bounds_value() {
        // Arrange
        final int min = 0, value = -10;
        final String[] args = {"-i", String.valueOf(value)};
        final var argument = new MandatoryBoundedIntArgument.Builder()
                .name("int")
                .shortName('i')
                .description("");

        // Act
        var argParser = new CmdParser.Builder()
                .argument(argument
                        .minimum(min)
                        .build())
                .build();

        // Assert
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void mandatory_argument_rejects_above_bounds_value() {
        // Arrange
        final int min = 0, max = 20, value = 30;
        final String[] args = {"-i", String.valueOf(value)};
        final var argument = new MandatoryBoundedIntArgument.Builder()
                .name("int")
                .shortName('i')
                .description("")
                .minimum(min);

        // Act
        var argParser = new CmdParser.Builder()
                .argument(argument
                        .maximum(max)
                        .build())
                .build();

        // Assert
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void mandatory_argument_without_value_throws_exception() {
        // Arrange
        final int min = 0, max = 20, value = 10;
        final String name = "int";
        final String[] args = {"-i"};
        var argParser = new CmdParser.Builder()
                .argument(new MandatoryBoundedIntArgument.Builder()
                        .name(name)
                        .shortName('i')
                        .description("")
                        .minimum(min)
                        .maximum(max)
                        .build())
                .build();

        // Act & Assert
        assertThrows(MissingValueException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void mandatory_argument_accepts_in_bounds_value() {
        // Arrange
        final int min = 0, max = 20, value = 10;
        final String name = "int";
        final String[] args = {"-i", String.valueOf(value)};
        var argParser = new CmdParser.Builder()
                .argument(new MandatoryBoundedIntArgument.Builder()
                        .name(name)
                        .shortName('i')
                        .description("")
                        .minimum(min)
                        .maximum(max)
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> assertEquals(value, argParser.getValue(name)));
    }

    @Test
    void default_argument_rejects_below_bounds_default_value() {
        // Arrange
        final int min = 0, defaultValue = -10;
        var builder = new DefaultBoundedIntArgument.Builder()
                .name("int")
                .description("");

        // Act
        builder
                .minimum(min)
                .defaultValue(defaultValue);

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void default_argument_rejects_below_bounds_value() {
        // Arrange
        final int min = 0, value = -10, defaultValue = 5;
        final String name = "int";
        final String[] args = {"--int", String.valueOf(value)};
        final var argument = new DefaultBoundedIntArgument.Builder()
                .name(name)
                .description("");

        // Act
        assertDoesNotThrow(() -> {
            final var argParser = new CmdParser.Builder()
                    .argument(argument
                            .minimum(min)
                            .defaultValue(defaultValue)
                            .build())
                    .build();

            // Assert
            assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
        });
    }

    @Test
    void default_argument_accepts_in_bounds_value() {
        // Arrange
        final int min = 0, value = 10, defaultValue = 5;
        final String name = "int", name2 = "append";
        final String[] args = {"--int", String.valueOf(value), "-a"};
        var parser = new CmdParser.Builder()
                .argument(new DefaultBoundedIntArgument.Builder()
                        .name(name)
                        .description("")
                        .minimum(min)
                        .defaultValue(defaultValue)
                        .build())
                .argument(new DefaultBoundedIntArgument.Builder()
                        .name(name2)
                        .defaultValue(defaultValue)
                        .minimum(min)
                        .description("")
                        .shortName('a')
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue(name));
            assertNotEquals(defaultValue, parser.getValue(name));
            assertEquals(defaultValue, parser.getValue(name2));
            assertNotEquals(value, parser.getValue(name2));
        });
    }

    @Test
    void default_argument_validates_defaultValue_not_null() {
        // Arrange
        var builder = new DefaultBoundedIntArgument.Builder();
        builder
                .description("a description")
                .name("int")
                .minimum(0);

        // Act
        // builder.defaultValue()

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void default_argument_validates_defaultValue_in_bounds() {
        // Arrange
        var builder = new DefaultBoundedIntArgument.Builder();
        builder
                .description("a description")
                .name("int");

        // Act
        builder
                .minimum(0)
                .defaultValue(-5);

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void argument_validates_maximum_is_greater_than_minimum() {
        // Arrange
        var builder = new MandatoryBoundedIntArgument.Builder();
        builder
                .description("a description")
                .name("int");

        // Act
        builder
                .minimum(20)
                .maximum(0);

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void argument_validates_maximum_does_not_equals_minimum() {
        // Arrange
        var builder = new MandatoryBoundedIntArgument.Builder();
        builder
                .description("a description")
                .name("int");

        // Act
        builder
                .minimum(20)
                .maximum(20);

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void argument_validates_minimum_not_null() {
        // Arrange
        var builder = new MandatoryBoundedIntArgument.Builder();
        builder
                .description("a description")
                .name("int");

        // Act
        // builder.minimum()

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void optional_argument_loads_value_from_name() {
        // Arrange
        final var name = "opt";
        final var value = 8;
        final String[] args = {"--" + name, String.valueOf(value)};
        final var arg = new OptionalBoundedIntArgument.Builder()
                .name(name)
                .description("")
                .shortName('o')
                .minimum(0)
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
        final var arg = new OptionalBoundedIntArgument.Builder()
                .name(name)
                .description("")
                .minimum(0)
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
        final var arg = new OptionalBoundedIntArgument.Builder()
                .name(name)
                .description("")
                .shortName('o')
                .minimum(0)
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
