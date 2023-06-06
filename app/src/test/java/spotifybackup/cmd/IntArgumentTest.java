package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import spotifybackup.cmd.argument.integer.DefaultIntArgument;
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
        var parser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue("extra"));
        });
    }

    @Test
    void mandatory_argument_loads_negative_value() {
        // Arrange
        final Integer value = -34;
        final String[] args = {"-e", value.toString()};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(value, parser.getValue("extra"));
        });
    }

    @Test
    void default_argument_loads_positive_value() {
        // Arrange
        final int value = 34, defaultValue = 12;
        final String[] args = {"-e", Integer.toString(value)};
        var parser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertNotEquals(defaultValue, parser.getValue("extra"));
            assertEquals(value, parser.getValue("extra"));
        });
    }

    @Test
    void default_argument_loads_negative_value() {
        // Arrange
        final int value = -34, defaultValue = 12;
        final String[] args = {"-e", Integer.toString(value)};
        var parser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .build())
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertNotEquals(defaultValue, parser.getValue("extra"));
            assertEquals(value, parser.getValue("extra"));
        });
    }

    @Test
    void testNullDefaultValueInt1() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultIntArgument.Builder()
//                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(20)
                        .build()
        ));
    }

    @Test
    void testNullDefaultValueInt2() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultIntArgument.Builder()
                        .name("extra")
//                        .description("")
                        .shortName('e')
                        .defaultValue(20)
                        .build()
        ));
    }

    @Test
    void testNullDefaultValueInt3() {
        assertDoesNotThrow(() -> new CmdParser.Builder().argument(
                new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
//                        .shortName('e')
                        .defaultValue(20)
                        .build()
        ));
    }

    @Test
    void testNullDefaultValueInt4() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
//                        .defaultValue(20)
                        .build()
        ));
    }

    @Test
    void testMalformedIntArgument1() {
        final String[] args = {"-he"};
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
