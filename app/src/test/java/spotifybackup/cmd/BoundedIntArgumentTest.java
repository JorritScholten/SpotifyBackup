package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import spotifybackup.cmd.argument.integer.DefaultBoundedIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryBoundedIntArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import static org.junit.jupiter.api.Assertions.*;

public class BoundedIntArgumentTest {
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
        // builder.defaultValue();

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
        // builder.minimum();

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }
}
