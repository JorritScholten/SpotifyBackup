package spotifybackup.cmd;


import org.junit.jupiter.api.Test;

class CmdParserTest {
    @Test
    void testCorrectParseArguments() {
        final String[] args = {"--help"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        argParser.parseArguments(args);
        try {
            System.out.println("help.getValue(): " + argParser.getValue("help"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void testIncorrectParseArguments() {
        final String[] args = {"-x", "--help", "-l", "20", "d", "--d"};
        CmdParser argParser = new CmdParser();
        argParser.addArgument(new FlagArgument("help", "Print program help and exit.", 'h'));
        argParser.parseArguments(args);
        try {
            System.out.println("help.getValue(): " + argParser.getValue("help"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
