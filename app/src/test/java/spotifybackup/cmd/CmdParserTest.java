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
    void testMissingArgument() {
        final String[] args = {"-he"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        // extra needs to be a non-Flag argument because flags are per definition non-mandatory
        assert false;
        argParser.addArgument(new FlagArgument("extra", "", 'e'));
        assertDoesNotThrow(() -> argParser.parseArguments(args));
        assertThrows(ArgumentNotPresentException.class, () ->
                System.out.println("help.getValue(): " + argParser.getValue("help"))
        );
    }

//    @Test
//    void testIncorrectParseArguments() {
//        final String[] args = {"-x", "--help", "-l", "20", "d", "--d"};
//        CmdParser argParser = new CmdParser();
//        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
//        argParser.parseArguments(args);
//        try {
//            System.out.println("help.getValue(): " + argParser.getValue("help"));
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }
}
