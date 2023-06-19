package spotifybackup.cmd;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.BeforeAll;
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
    private static ScanResult scanResult;

    @BeforeAll
    static void bypassEncapsulationAndScanCmdPackage() {
        ClassGraph.CIRCUMVENT_ENCAPSULATION = ClassGraph.CircumventEncapsulationMethod.JVM_DRIVER;
        scanResult = new ClassGraph()
//                .verbose()
                .enableAllInfo()
                .acceptPackages("spotifybackup.cmd")
                .scan();
    }

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
    void testInvalidShortArgumentName4() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new FlagArgument.Builder()
                        .name("")
                        .description("flag argument.")
                        .shortName('s')
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
                        new DefaultStringArgument.Builder()
                                .name("append")
                                .description("")
                                .shortName('a')
                                .defaultValue("other")
                                .build())
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
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
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
                        new MandatoryStringArgument.Builder()
                                .name("string")
                                .description("")
                                .build(),
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
                        new MandatoryStringArgument.Builder()
                                .name("string")
                                .description("")
                                .shortName('a')
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
    void ensure_that_getHelp_output_is_formatted_correctly_for_default_width() {
        // Arrange
        final String expectedOutput = """
                Usage: --int INTEGER --txt FILEPATH [-h] [-i [INTEGER]] [-s [STRING]]

                Mandatory arguments:
                  --int INTEGER     some integer
                  --txt FILEPATH    Lorem ipsum dolor sit amet, consectetur adipiscing elit,

                Optional arguments:
                  -h, --help        Show this help message and exit.
                  -i [INTEGER], --int2 [INTEGER]
                                    Lorem ipsum dolor sit amet, consectetur adipiscing elit,
                  -s [STRING], --str [STRING]
                                    some sort of string, dunno, not gonna use it.
                """;
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
                        new DefaultStringArgument.Builder()
                                .name("str")
                                .description("some sort of string, dunno, not gonna use it.")
                                .shortName('s')
                                .defaultValue("string")
                                .build(),
                        new MandatoryFilePathArgument.Builder()
                                .name("txt")
                                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit,")
                                .isFile()
                                .build())
                .addHelp()
                .build();

        // Act & Assert
        assertEquals(expectedOutput, argParser.getHelp());
    }

    @Test
    void ensure_that_getHelp_output_is_formatted_correctly_for_125_width() {
        // Arrange
        final String expectedOutput = """
                Usage: testName.jar --int INTEGER --txt FILEPATH [-h] [-i [INTEGER]] [-s [STRING]]
                                
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut
                                
                Mandatory arguments:
                  --int INTEGER                         some integer
                  --txt FILEPATH                        Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
                                                        incididunt ut
                                
                Optional arguments:
                  -h, --help                            Show this help message and exit.
                  -i [INTEGER], --int2 [INTEGER]        Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
                                                        incididunt ut
                  -s [STRING], --str [STRING]           some sort of string, dunno, not gonna use it.
                                
                labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation
                """;
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
                        new DefaultStringArgument.Builder()
                                .name("str")
                                .description("some sort of string, dunno, not gonna use it.")
                                .shortName('s')
                                .defaultValue("string")
                                .build(),
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

        // Act & Assert
        assertEquals(expectedOutput, argParser.getHelp(125, 40));
    }

    @Test
    void ensure_that_getHelp_output_is_formatted_correctly_for_60_width() {
        final String expectedOutput = """
                Usage: testName.jar --int INTEGER --txt FILEPATH [-h] [-i
                [INTEGER]] [-s [STRING]]

                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed
                do eiusmod tempor incididunt ut

                Mandatory arguments:
                  --int INTEGER
                               some integer
                  --txt FILEPATH
                               Lorem ipsum dolor sit amet, consectetur
                               adipiscing elit, sed do eiusmod tempor
                               incididunt ut

                Optional arguments:
                  -h, --help   Show this help message and exit.
                  -i [INTEGER], --int2 [INTEGER]
                               Lorem ipsum dolor sit amet, consectetur
                               adipiscing elit, sed do eiusmod tempor
                               incididunt ut
                  -s [STRING], --str [STRING]
                               some sort of string, dunno, not gonna use it.

                labore et dolore magna aliqua. Ut enim ad minim veniam, quis
                nostrud exercitation
                """;
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
                        new DefaultStringArgument.Builder()
                                .name("str")
                                .description("some sort of string, dunno, not gonna use it.")
                                .shortName('s')
                                .defaultValue("string")
                                .build(),
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

        // Act & Assert
        assertEquals(expectedOutput, argParser.getHelp(60));
    }

    /**
     * This test ensures that each Argument child has its constructor access set correctly. This test was implemented to
     * ease future development.
     */
    @Test
    void ensure_all_argument_constructor_access_is_correct_with_reflection() {
        for (var argument : scanResult.getSubclasses(Argument.class)) {
            var declaredConstructorInfo = argument.getDeclaredConstructorInfo();
            if (declaredConstructorInfo.size() != 1) {
                throw new RuntimeException("Argument declaration should have only one constructor, " +
                        "parameters are to be handled using builder: " + argument.getName());
            }
            var constructor = declaredConstructorInfo.get(0);
            if (argument.isAbstract() && (constructor.isPrivate() || constructor.isPublic())) {
                throw new RuntimeException("Abstract Argument should have a protected or package-private " +
                        "constructor: " + argument.getName());
            } else if (!argument.isAbstract() && !constructor.isPrivate()) {
                throw new RuntimeException("Implemented Argument should have a private constructor: " +
                        argument.getName());
            }
        }
    }

    /**
     * This test is to ensure that each Argument child class has an implemented Builder class which is necessary for the
     * Builder class to properly work. It also ensures that the various Builders have the correct access type. This test
     * was implemented to ease future development.
     */
    @Test
    void ensure_argument_builder_access_is_correct_with_reflection() {
        for (var argument : scanResult.getSubclasses(Argument.class)) {
            var optionallyBuilder = argument
                    .getInnerClasses()
                    .filter(classInfo -> classInfo.getSimpleName().equals("Builder"));
            if (optionallyBuilder.size() != 1) {
                throw new RuntimeException("Each Argument class should have an implemented builder class: " +
                        argument.getName());
            }
            var builder = optionallyBuilder.get(0);
            if (argument.isAbstract()) {
                if (!builder.isAbstract()) {
                    throw new RuntimeException("Abstract Argument should contain an abstract Builder: " +
                            argument.getName());
                }
            } else {
                if (builder.isAbstract()) {
                    throw new RuntimeException("Implemented Argument should contain a non-abstract Builder: " +
                            argument.getName());
                }
                if (!builder.isPublic()) {
                    throw new RuntimeException("Implemented Argument should have a public Builder: " +
                            argument.getName());
                }
            }
        }
    }
}
