package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.argument.file.MandatoryFilePathArgument;
import spotifybackup.cmd.argument.integer.DefaultIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryBoundedIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryIntArgument;
import spotifybackup.cmd.argument.string.DefaultStringArgument;
import spotifybackup.cmd.argument.string.MandatoryStringArgument;
import spotifybackup.cmd.exception.*;

import static org.junit.jupiter.api.Assertions.*;

class CmdParserTest {
    @Test
    void testFlagByName() {
        final String[] args = {"--help"};
        CmdParser argParser = new CmdParser.Builder().addHelp().build();
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
        CmdParser argParser = new CmdParser.Builder()
                .argument(new FlagArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(Boolean.TRUE, argParser.getValue("help"));
            assertEquals(Boolean.FALSE, argParser.getValue("extra"));
        });
    }

    @Test
    void testNullArgs() {
        final String[] args = {};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new FlagArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(Boolean.FALSE, argParser.getValue("help"));
            assertEquals(Boolean.FALSE, argParser.getValue("extra"));
        });
    }

    @Test
    void testDuplicateArgumentNames() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().arguments(
                new FlagArgument.Builder()
                        .name("flag")
                        .description("flag argument.")
                        .shortName('f')
                        .build(),
                new FlagArgument.Builder()
                        .name("help")
                        .description("")
                        .shortName('e')
                        .build()
        ).addHelp().build());
    }

    @Test
    void testDuplicateArgumentShortNames() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().arguments(
                new FlagArgument.Builder()
                        .name("flag")
                        .description("flag argument.")
                        .shortName('f')
                        .build(),
                new FlagArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('h')
                        .build()
        ).addHelp().build());
    }

    @Test
    void testFlagByMalformedName() {
        final String[] args = {"--hel"};
        CmdParser argParser = new CmdParser.Builder().addHelp().build();
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }

    @Test
    void testNullArgumentName() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new FlagArgument.Builder()
                        .description("flag argument.")
                        .shortName('f')
                        .build()
        ).build());
    }

    @Test
    void testInvalidShortArgumentName1() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new FlagArgument.Builder()
                        .name("null")
                        .description("flag argument.")
                        .shortName('3')
                        .build()
        ).build());
    }

    @Test
    void testInvalidShortArgumentName2() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new FlagArgument.Builder()
                        .name("null")
                        .description("flag argument.")
                        .shortName('$')
                        .build()
        ).build());
    }

    @Test
    void testInvalidShortArgumentName3() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new FlagArgument.Builder()
                        .name("null")
                        .description("flag argument.")
                        .shortName(' ')
                        .build()
        ).build());
    }

    @Test
    void testMissingMandatoryArgument() {
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
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
    void testMissingDefaultArgument() {
        int defaultValue = 23;
        final String[] args = {"-h"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertFalse(argParser.isPresent("extra"));
        });
    }

    @Test
    void testFlaggedDefaultArgument1() {
        int defaultValue = 23;
        final String[] args = {"-hx"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('x')
                        .defaultValue(defaultValue)
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertTrue(argParser.isPresent("extra"));
        });
    }

    @Test
    void testFlaggedDefaultArgument2() {
        int defaultValue = 23;
        final String[] args = {"-h", "--extra"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('x')
                        .defaultValue(defaultValue)
                        .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertTrue(argParser.isPresent("extra"));
        });
    }

    @Test
    void testFlaggedDefaultArgument3() {
        int defaultValue1 = 23, defaultValue2 = 12;
        String defaultValue3 = "some_test_string";
        final String[] args = {"-h", "--extra", "--int", "-a", defaultValue3};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new DefaultIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('x')
                                .defaultValue(defaultValue1)
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int")
                                .description("")
                                .shortName('i')
                                .defaultValue(defaultValue2)
                                .build(),
                        new DefaultStringArgument("append", "", 'a', "other"))
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue1, argParser.getValue("extra"));
            assertEquals(defaultValue2, argParser.getValue("int"));
            assertEquals(defaultValue3, argParser.getValue("append"));
            assertTrue(argParser.isPresent("extra"));
        });
    }

    @Test
    void testMultipleFlaggedDefaultArguments1() {
        int defaultValue = 23;
        final String[] args = {"-hxi"};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new DefaultIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('x')
                                .defaultValue(defaultValue)
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int")
                                .description("")
                                .shortName('i')
                                .defaultValue(defaultValue)
                                .build())
                .addHelp()
                .build();
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
        int defaultValue1 = 23, defaultValue2 = 12, defaultValue3 = -98;
        final String[] args = {"-him", String.valueOf(defaultValue3)};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new DefaultIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('x')
                                .defaultValue(defaultValue1)
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int")
                                .description("")
                                .shortName('i')
                                .defaultValue(defaultValue2)
                                .build(),
                        new MandatoryIntArgument.Builder()
                                .name("mint")
                                .description("")
                                .shortName('m')
                                .build())
                .addHelp()
                .build();
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
    void testMultipleFlaggedDefaultArguments3() {
        int defaultValue1 = 23, defaultValue2 = 12;
        final String[] args = {"-hxim", String.valueOf(defaultValue2)};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new FlagArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('x')
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int")
                                .description("")
                                .shortName('i')
                                .defaultValue(defaultValue1)
                                .build(),
                        new FlagArgument.Builder()
                                .name("mint")
                                .description("")
                                .shortName('m')
                                .build())
                .addHelp()
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(defaultValue2, argParser.getValue("int"));
            assertTrue(argParser.isPresent("extra"));
            assertTrue(argParser.isPresent("int"));
            assertTrue(argParser.isPresent("mint"));
            assertTrue(argParser.isPresent("help"));
        });
    }

    @Test
    void testMultipleFlaggedDefaultArguments4() {
        int defaultValue1 = 23, defaultValue2 = 12;
        final String[] args = {"-hxi", String.valueOf(defaultValue2)};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new DefaultIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('x')
                                .defaultValue(defaultValue1)
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int")
                                .description("")
                                .shortName('i')
                                .defaultValue(defaultValue1)
                                .build())
                .addHelp()
                .build();
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void testMultipleMandatoryArguments1() {
        int defaultValue1 = 23;
        final String[] args = {"-hxi", Integer.toString(defaultValue1)};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new MandatoryIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('x')
                                .build(),
                        new MandatoryIntArgument.Builder()
                                .name("int")
                                .description("")
                                .shortName('i')
                                .build())
                .addHelp()
                .build();
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void testMalformedMultipleFlaggedDefaultArguments1() {
        int defaultValue = 23;
        final String[] args = {"-hxi"};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new DefaultIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('x')
                                .defaultValue(defaultValue)
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int")
                                .description("")
                                .defaultValue(defaultValue)
                                .build())
                .addHelp()
                .build();
        assertThrows(MalformedInputException.class, () -> {
            argParser.parseArguments(args);
        });
    }

    @Test
    void testMalformedArgumentMissingValue() {
        final String[] args = {"-he", "-28", "--string", "test", "-i"};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new MandatoryIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('e')
                                .build(),
                        new MandatoryStringArgument("string", ""),
                        new MandatoryBoundedIntArgument.Builder()
                                .name("int2")
                                .description(" ")
                                .shortName('i')
                                .minimum(23)
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
    void testMalformedArgumentMultipleShortValue1() {
        final String[] args = {"-hea", "-28"};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new MandatoryIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('e')
                                .build(),
                        new MandatoryStringArgument("string", "", 'a'))
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
    void testMalformedArgumentMultipleShortValue2() {
        int defaultValue = 1;
        final String[] args = {"-hea", String.valueOf(defaultValue)};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new MandatoryIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('e')
                                .build(),
                        new MandatoryIntArgument.Builder()
                                .name("string")
                                .description("")
                                .shortName('a')
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int")
                                .description("")
                                .shortName('i')
                                .defaultValue(defaultValue)
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
    void testGetHelp1() {
        final String expectedOutput = "Usage: --int INTEGER --txt FILEPATH [-h] [-i [INTEGER]] [-s [STRING]]\n" +
                "\n" +
                "Mandatory arguments:\n" +
                "  --int INTEGER     some integer\n" +
                "  --txt FILEPATH    Lorem ipsum dolor sit amet, consectetur adipiscing elit,\n" +
                "\n" +
                "Optional arguments:\n" +
                "  -h, --help        Show this help message and exit.\n" +
                "  -i [INTEGER], --int2 [INTEGER]\n" +
                "                    Lorem ipsum dolor sit amet, consectetur adipiscing elit,\n" +
                "  -s [STRING], --str [STRING]\n" +
                "                    some sort of string, dunno, not gonna use it." +
                "\n";
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new MandatoryIntArgument.Builder()
                                .name("int")
                                .description("some integer")
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int2")
                                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit,")
                                .shortName('i')
                                .defaultValue(23)
                                .build(),
                        new DefaultStringArgument("str", "some sort of string, dunno, not gonna use it.",
                                's', "string"),
                        new MandatoryFilePathArgument.Builder()
                                .name("txt")
                                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit,")
                                .isFile()
                                .build())
                .addHelp()
                .build();
        assertEquals(expectedOutput, argParser.getHelp(80));
    }

    @Test
    void testGetHelp2() {
        final String expectedOutput = "Usage: testName.jar --int INTEGER --txt FILEPATH [-h] [-i [INTEGER]] [-s [STRING]]\n" +
                "\n" +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut\n" +
                "\n" +
                "Mandatory arguments:\n" +
                "  --int INTEGER                some integer\n" +
                "  --txt FILEPATH               Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut\n" +
                "\n" +
                "Optional arguments:\n" +
                "  -h, --help                   Show this help message and exit.\n" +
                "  -i [INTEGER], --int2 [INTEGER]\n" +
                "                               Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut\n" +
                "  -s [STRING], --str [STRING]  some sort of string, dunno, not gonna use it.\n" +
                "\n" +
                "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation" +
                "\n";
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new MandatoryIntArgument.Builder()
                                .name("int")
                                .description("some integer")
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int2")
                                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod" +
                                        " tempor incididunt ut")
                                .shortName('i')
                                .defaultValue(23)
                                .build(),
                        new DefaultStringArgument("str", "some sort of string, dunno, not gonna use it.",
                                's', "string"),
                        new MandatoryFilePathArgument.Builder()
                                .name("txt")
                                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod" +
                                        " tempor incididunt ut")
                                .isFile()
                                .build())
                .addHelp()
                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut")
                .programName("testName.jar")
                .epilogue("labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation")
                .build();
        assertEquals(expectedOutput, argParser.getHelp(125));
    }

    @Test
    void testGetHelp3() {
        final String expectedOutput = "Usage: testName.jar --int INTEGER --txt FILEPATH [-h] [-i\n" +
                "[INTEGER]] [-s [STRING]]\n" +
                "\n" +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed\n" +
                "do eiusmod tempor incididunt ut\n" +
                "\n" +
                "Mandatory arguments:\n" +
                "  --int INTEGER\n" +
                "               some integer\n" +
                "  --txt FILEPATH\n" +
                "               Lorem ipsum dolor sit amet, consectetur\n" +
                "               adipiscing elit, sed do eiusmod tempor\n" +
                "               incididunt ut\n" +
                "\n" +
                "Optional arguments:\n" +
                "  -h, --help   Show this help message and exit.\n" +
                "  -i [INTEGER], --int2 [INTEGER]\n" +
                "               Lorem ipsum dolor sit amet, consectetur\n" +
                "               adipiscing elit, sed do eiusmod tempor\n" +
                "               incididunt ut\n" +
                "  -s [STRING], --str [STRING]\n" +
                "               some sort of string, dunno, not gonna use it.\n" +
                "\n" +
                "labore et dolore magna aliqua. Ut enim ad minim veniam, quis\n" +
                "nostrud exercitation" +
                "\n";
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new MandatoryIntArgument.Builder()
                                .name("int")
                                .description("some integer")
                                .build(),
                        new DefaultIntArgument.Builder()
                                .name("int2")
                                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod" +
                                        " tempor incididunt ut")
                                .shortName('i')
                                .defaultValue(23)
                                .build(),
                        new DefaultStringArgument("str", "some sort of string, dunno, not gonna use it.",
                                's', "string"),
                        new MandatoryFilePathArgument.Builder()
                                .name("txt")
                                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod" +
                                        " tempor incididunt ut")
                                .isFile()
                                .build())
                .addHelp()
                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut")
                .programName("testName.jar")
                .epilogue("labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation")
                .build();
        assertEquals(expectedOutput, argParser.getHelp(60));
    }
}
