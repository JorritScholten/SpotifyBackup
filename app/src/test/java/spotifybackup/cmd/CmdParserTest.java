package spotifybackup.cmd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CmdParserTest {
    @Test
    void testFlagByName() {
        final String[] args = {"--help"};
        CmdParser argParser = new CmdParser(new Argument[]{
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
                new FlagArgument("flag", "flag argument.", 'f'),
                new FlagArgument("help", "", 'e')
        }));
    }

    @Test
    void testDuplicateArgumentShortNames() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new FlagArgument("flag", "flag argument.", 'f'),
                new FlagArgument("extra", "", 'h')
        }));
    }

    @Test
    void testFlagByMalformedName() {
        final String[] args = {"--hel"};
        CmdParser argParser = new CmdParser(new Argument[]{
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
                new FlagArgument(null, "flag argument", 'f')
        }));
    }

    @Test
    void testMissingMandatoryArgument() {
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser(new Argument[]{
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
    void testMissingDefaultArgument() {
        int defaultValue = 23;
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultIntArgument("extra", "", 'e', defaultValue)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertFalse(argParser.isPresent("extra"));
        });
    }

    @Test
    void testFlaggedDefaultArgument() {
        int defaultValue = 23;
        final String[] args = {"-hx"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultIntArgument("extra", "", 'x', defaultValue)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertTrue(argParser.isPresent("extra"));
        });
    }

    @Test
    void testMultipleFlaggedDefaultArguments1() {
        int defaultValue = 23;
        final String[] args = {"-hxi"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultIntArgument("extra", "", 'x', defaultValue),
                new DefaultIntArgument("int", "", 'i', defaultValue)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertEquals(defaultValue, argParser.getValue("int"));
            assertTrue(argParser.isPresent("extra"));
            assertTrue(argParser.isPresent("int"));
        });
    }

    @Test
    void testMultipleFlaggedDefaultArguments2() {
        Integer defaultValue1 = 23, defaultValue2 = 12, defaultValue3 = -98;
        final String[] args = {"-him", defaultValue3.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultIntArgument("extra", "", 'x', defaultValue1),
                new DefaultIntArgument("int", "", 'i', defaultValue2),
                new MandatoryIntArgument("mint", "", 'm')
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue1, argParser.getValue("extra"));
            assertEquals(defaultValue2, argParser.getValue("int"));
            assertEquals(defaultValue3, argParser.getValue("mint"));
            assertFalse(argParser.isPresent("extra"));
            assertTrue(argParser.isPresent("int"));
            assertTrue(argParser.isPresent("mint"));
        });
    }

    @Test
    void testMalformedMultipleFlaggedDefaultArguments1() {
        int defaultValue = 23;
        final String[] args = {"-hxi"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultIntArgument("extra", "", 'x', defaultValue),
                new DefaultIntArgument("int", "",/*'i',*/ defaultValue)
        });
        assertThrows(MalformedInputException.class, () -> {
            argParser.parseArguments(args);
        });
    }

    @Test
    void testMalformedArgumentMissingValue() {
        final String[] args = {"-he", "-28", "--string", "test", "-i"};
        CmdParser argParser = new CmdParser(new Argument[]{
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
    void testMalformedArgumentMultipleShortValue1() {
        final String[] args = {"-hea", "-28"};
        CmdParser argParser = new CmdParser(new Argument[]{
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

    @Test
    void testMalformedArgumentMultipleShortValue2() {
        Integer defaultValue = 1;
        final String[] args = {"-hea", defaultValue.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryIntArgument("extra", "", 'e'),
                new MandatoryIntArgument("string", "", 'a'),
                new DefaultIntArgument("int", "", 'i', defaultValue)
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }
}
