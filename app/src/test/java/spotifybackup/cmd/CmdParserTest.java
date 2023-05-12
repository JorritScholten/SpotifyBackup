package spotifybackup.cmd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CmdParserTest {
    @Test
    void testFlagByName() {
        final String[] args = {"--help"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h')
        });
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
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new FlagArgument("extra", "", 'e')
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(Boolean.TRUE, argParser.getValue("help"));
            assertEquals(Boolean.FALSE, argParser.getValue("extra"));
        });
    }

    @Test
    void testNullArgs() {
        final String[] args = {};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new FlagArgument("extra", "", 'e')
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(Boolean.FALSE, argParser.getValue("help"));
            assertEquals(Boolean.FALSE, argParser.getValue("extra"));
        });
    }

    @Test
    void testDuplicateArgumentNames() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new FlagArgument("help", "", 'e')
        }));
    }

    @Test
    void testDuplicateArgumentShortNames() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new FlagArgument("extra", "", 'h')
        }));
    }

    @Test
    void testFlagByMalformedName() {
        final String[] args = {"--hel"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h')
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testNullArgumentName() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new FlagArgument(null, "Print program help and exit.", 'h')
        }));
    }

    @Test
    void testMissingMandatoryArgument() {
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new MandatoryIntArgument("extra", "", 'e')
        });
        assertThrows(MissingArgumentException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testMissingNonMandatoryArgument() {
        int defaultValue = 23;
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new DefaultIntArgument("extra", "", 'e', defaultValue)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue, argParser.getValue("extra"));
        });
    }

    @Test
    void testMalformedArgumentMissingValue() {
        final String[] args = {"-he", "-28", "--string", "test", "-i"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new MandatoryIntArgument("extra", "", 'e'),
                new MandatoryStringArgument("string", ""),
                new BoundedIntArgument("int2", " ", 'i', 23)
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testMalformedArgumentMultipleShortValue() {
        final String[] args = {"-hea", "-28"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new MandatoryIntArgument("extra", "", 'e'),
                new MandatoryStringArgument("string", "", 'a')
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }
}
