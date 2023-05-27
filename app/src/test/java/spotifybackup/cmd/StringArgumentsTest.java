package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import spotifybackup.cmd.argument.string.DefaultStringArgument;
import spotifybackup.cmd.argument.string.MandatoryStringArgument;
import spotifybackup.cmd.exception.ArgumentsNotParsedException;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MissingArgumentException;

import static org.junit.jupiter.api.Assertions.*;

public class StringArgumentsTest {
    @Test
    void testStringArgument1() {
        final String value = "test_value";
        final String[] args = {"-h", "--extra", value};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryStringArgument.Builder()
                        .name("extra")
                        .description("")
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testStringArgument2() {
        final String value = "test_value";
        final String[] args = {"-h", "-e", value};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryStringArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultStringArgument1() {
        final String value = "test_value";
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultStringArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(value)
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultStringArgument2() {
        final String value = "test_value", defaultValue = "other_value";
        final String[] args = {"-h", "-e", value};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultStringArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertNotEquals(defaultValue, argParser.getValue("extra"));
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testMissingStringArgument() {
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryStringArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();
        assertThrows(MissingArgumentException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
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
