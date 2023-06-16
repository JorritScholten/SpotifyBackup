package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import spotifybackup.cmd.argument.string.DefaultStringArgument;
import spotifybackup.cmd.argument.string.MandatoryStringArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;
import spotifybackup.cmd.exception.MissingArgumentException;

import static org.junit.jupiter.api.Assertions.*;

public class StringArgumentsTest {
    @Test
    void mandatory_argument_loads_value_from_name() {
        // Arrange
        final String value = "test_value";
        final String name = "extra";
        final String[] args = {"-h", "--" + name, value};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryStringArgument.Builder()
                        .name(name)
                        .description("")
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue(name));
        });
    }

    @Test
    void mandatory_argument_loads_value_from_shortName() {
        // Arrange
        final String value = "test_value";
        final String name = "extra";
        final String[] args = {"-h", "-e", value};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryStringArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue(name));
        });
    }

    @Test
    void default_argument_loads_value_from_name() {
        // Arrange
        final String value = "23.6", default_value = "a_value";
        final String name = "extra";
        final String[] args = {"-h", "--" + name, value};
        var parser = new CmdParser.Builder()
                .argument(new DefaultStringArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .defaultValue(default_value)
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue(name));
            assertNotEquals(default_value, parser.getValue(name));
            assertTrue(parser.isPresent(name));
        });
    }

    @Test
    void default_argument_loads_value_from_shortName() {
        // Arrange
        final String value = "test_value", defaultValue = "other_value";
        final String name = "extra";
        final String[] args = {"-h", "-e", value};
        var parser = new CmdParser.Builder()
                .argument(new DefaultStringArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue(name));
            assertNotEquals(defaultValue, parser.getValue(name));
            assertTrue(parser.isPresent(name));
        });
    }

    @Test
    void mandatory_argument_missing_in_input_throws_exception() {
        // Arrange
        final String[] args = {"-h"};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryStringArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();

        // Act & Assert
        assertThrows(MissingArgumentException.class, () -> parser.parseArguments(args));
    }

    @Test
    void mandatory_argument_without_value_throws_exception() {
        // Arrange
        final String[] args = {"-he"};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryStringArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();

        // Act & Assert
        assertThrows(MalformedInputException.class, () -> parser.parseArguments(args));
    }

    @Test
    void testDefaultStringArgumentConstructor1() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultStringArgument.Builder()
                        .name("extra")
                        .description("")
                        .build()
        ));
    }

    @Test
    void testDefaultStringArgumentConstructor2() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultStringArgument.Builder()
                        .description("")
                        .defaultValue("string")
                        .build()
        ));
    }
}
