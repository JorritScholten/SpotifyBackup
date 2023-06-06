package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import spotifybackup.cmd.argument.integer.DefaultBoundedIntArgument;
import spotifybackup.cmd.argument.integer.DefaultIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryBoundedIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryIntArgument;
import spotifybackup.cmd.exception.ArgumentsNotParsedException;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import static org.junit.jupiter.api.Assertions.*;

public class IntArgumentTest {
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
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void testMalformedIntArgument2() {
        final String[] args = {"-he", "21.2"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testMalformedIntArgument3() {
        final String[] args = {"-he", "sdf"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }
}
