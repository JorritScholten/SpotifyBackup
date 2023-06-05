package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import spotifybackup.cmd.argument.integer.DefaultBoundedIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryBoundedIntArgument;
import spotifybackup.cmd.exception.MalformedInputException;

import static org.junit.jupiter.api.Assertions.*;

public class BoundedIntArgumentTest {
    @Test
    void mandatory_argument_rejects_below_bounds_value() {
        // Arrange
        final int min = 0, value = -10;
        final String[] args = {"-i", String.valueOf(value)};

        // Act
        var argParser = new CmdParser.Builder()
                .argument(new MandatoryBoundedIntArgument.Builder()
                        .name("int")
                        .shortName('i')
                        .description("")
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

        // Act
        var argParser = new CmdParser.Builder()
                .argument(new MandatoryBoundedIntArgument.Builder()
                        .name("int")
                        .shortName('i')
                        .description("")
                        .minimum(min)
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

        // Act
        var argParser = new CmdParser.Builder()
                .argument(new MandatoryBoundedIntArgument.Builder()
                        .name(name)
                        .shortName('i')
                        .description("")
                        .minimum(min)
                        .maximum(max)
                        .build())
                .build();

        // Assert
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue(name));
        });
    }

    @Test
    void default_argument_rejects_below_bounds_default_value() {
        // Arrange
        final int min = 0, defaultValue = -10;

        // Act
        var builder = new DefaultBoundedIntArgument.Builder()
                .name("int")
                .description("")
                .minimum(min)
                .defaultValue(defaultValue);

        // Assert
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void default_argument_rejects_below_bounds_value() {
        // Arrange
        final int min = 0, value = -10, defaultValue = 5;
        final String name = "int";
        final String[] args = {"--int", String.valueOf(value)};

        // Act & Assert
        assertDoesNotThrow(() -> {
            final var argParser = new CmdParser.Builder()
                    .argument(new DefaultBoundedIntArgument.Builder()
                            .name(name)
                            .description("")
                            .minimum(min)
                            .defaultValue(defaultValue)
                            .build())
                    .build();
            assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
        });
    }

    @Test
    void default_argument_accepts_in_bounds_value() {
        // Arrange
        final int min = 0, value = 10, defaultValue = 5;
        final String name = "int";
        final String[] args = {"--int", String.valueOf(value)};

        // Act
        var parser = new CmdParser.Builder()
                .argument(new DefaultBoundedIntArgument.Builder()
                        .name(name)
                        .description("")
                        .minimum(min)
                        .defaultValue(defaultValue)
                        .build())
                .build();

        // Assert
        assertDoesNotThrow(() -> {
            parser.parseArguments(args);
            assertEquals(value, parser.getValue(name));
            assertNotEquals(defaultValue, parser.getValue(name));
        });
    }
}
