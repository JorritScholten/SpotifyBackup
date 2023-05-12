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
    void testIntArgument1() {
        final Integer value = 34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new IntArgument("extra", "", 'e', true)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testIntArgument2() {
        final Integer value = -34;
        System.out.println("value: " + value.toString());
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new IntArgument("extra", "", 'e', true)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testBoundedIntArgument() {
        final Integer value = 34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new BoundedIntArgument("extra", "", 'e', true, 1)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testMalformedBoundedIntArgument1() {
        final Integer value = -34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new BoundedIntArgument("extra", "", true, 1)
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testMalformedBoundedIntArgument2() {
        final Integer value = 34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new BoundedIntArgument("extra", "", true, 1, 20)
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testMissingMandatoryArgument() {
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new IntArgument("extra", "", 'e', true)
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
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new IntArgument("extra", "", 'e', false)
        });
        assertDoesNotThrow(() ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentNotPresentException.class, () ->
                argParser.getValue("extra")
        );
    }

    @Test
    void testMalformedArgumentMissingValue() {
        final String[] args = {"-he", "-28", "--string", "test", "-i"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new IntArgument("extra", "", 'e', true),
                new StringArgument("string", "", true),
                new BoundedIntArgument("int2", " ", 'i', false, 23)
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
                new IntArgument("extra", "", 'e', false),
                new StringArgument("string", "", 'a', false)
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testMalformedIntArgument1() {
        final String[] args = {"-he"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new IntArgument("extra", "", 'e', true)
        });
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
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new IntArgument("extra", "", 'e', true)
        });
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
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new IntArgument("extra", "", 'e', true)
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testStringArgument() {
        final String value = "test_value";
        final String[] args = {"-h", "--extra", value};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new StringArgument("extra", "", true)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }
}
