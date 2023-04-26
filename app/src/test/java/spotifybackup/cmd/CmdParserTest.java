package spotifybackup.cmd;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CmdParserTest {
    @Test
    void testFlagByName() {
        final String[] args = {"--help"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        assertDoesNotThrow(() -> argParser.parseArguments(args));
        try {
            System.out.println("help.getValue(): " + argParser.getValue("help"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void testFlagByShortName() {
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        assertDoesNotThrow(() -> argParser.parseArguments(args));
        try {
            System.out.println("help.getValue(): " + argParser.getValue("help"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void testFlagByMalformedName() {
        final String[] args = {"--hel"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
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
