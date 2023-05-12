package spotifybackup.cmd;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CmdParserTest {
    @Test
    void testFlagByName() {
        final String[] args = {"--help"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(Boolean.TRUE, argParser.getValue("help"));
        });
        assertThrows(ArgumentNotPresentException.class, () ->
                // "extra" flag not defined and thus should throw an exception
                argParser.getValue("extra")
        );
    }

    @Test
    void testFlagByShortName() {
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        argParser.addArgument(new FlagArgument("extra", "", 'e'));
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(Boolean.TRUE, argParser.getValue("help"));
            assertEquals(Boolean.FALSE, argParser.getValue("extra"));
        });
    }

    @Test
    void testFlagByMalformedName() {
        final String[] args = {"--hel"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        assertThrows(ArgumentNotPresentException.class, () ->
                argParser.parseArguments(args)
        );
    }

    @Test
    void testIntArgument() {
        final Integer value = 34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new IntArgument("extra", "", 'e', true));
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testMissingMandatoryArgument() {
        final String[] args = {"-he"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        argParser.addArgument(new IntArgument("extra", "", 'e', true));
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
    }
}
