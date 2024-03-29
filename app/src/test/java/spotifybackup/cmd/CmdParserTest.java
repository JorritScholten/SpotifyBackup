package spotifybackup.cmd;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.argument.enumeration.DefaultEnumArgument;
import spotifybackup.cmd.argument.enumeration.MandatoryEnumArgument;
import spotifybackup.cmd.argument.enumeration.OptionalEnumArgument;
import spotifybackup.cmd.argument.file.MandatoryFilePathArgument;
import spotifybackup.cmd.argument.file.OptionalFilePathArgument;
import spotifybackup.cmd.argument.integer.*;
import spotifybackup.cmd.argument.string.DefaultStringArgument;
import spotifybackup.cmd.argument.string.MandatoryStringArgument;
import spotifybackup.cmd.argument.string.OptionalStringArgument;
import spotifybackup.cmd.exception.*;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableCmdParserTests", matches = "true")
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
    void parser_identifies_argument_by_name() {
        // Arrange
        final String[] args = {"--help"};
        var parser = new CmdParser.Builder().addHelp().build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(Boolean.TRUE, parser.getValue("help"));
        });
        assertThrows(ArgumentNotPresentException.class, () ->
                // "extra" flag not defined and thus should throw an exception
                parser.getValue("extra")
        );
    }

    @Test
    void parser_identifies_argument_by_shortName() {
        // Arrange
        final String[] args = {"-h"};
        var parser = new CmdParser.Builder()
                .argument(new FlagArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(Boolean.TRUE, parser.getValue("help"));
            assertEquals(Boolean.FALSE, parser.getValue("extra"));
        });
    }

    @Test
    void ensure_parser_handles_empty_input() {
        final String[] args = {};
        var parser = new CmdParser.Builder()
                .argument(new FlagArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(Boolean.FALSE, parser.getValue("help"));
            assertEquals(Boolean.FALSE, parser.getValue("extra"));
        });
    }

    @Test
    void ensure_CmdParser_rejects_duplicate_names() {
        // Arrange & Act
        var parser = new CmdParser.Builder()
                .argument(new FlagArgument.Builder()
                        .name("help")
                        .description("")
                        .shortName('e')
                        .build()
                ).addHelp();

        // Assert
        assertThrows(IllegalConstructorParameterException.class, parser::build);
    }

    @Test
    void ensure_CmdParser_rejects_duplicate_shortNames() {
        // Arrange & Act
        var parser = new CmdParser.Builder().arguments(
                new FlagArgument.Builder()
                        .name("flag")
                        .description("flag argument.")
                        .shortName('h')
                        .build(),
                new FlagArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('h')
                        .build()
        );

        // Assert
        assertThrows(IllegalConstructorParameterException.class, parser::build);
    }

    @Test
    void ensure_unidentifiable_name_in_input_throws_exception() {
        // Arrange
        CmdParser argParser = new CmdParser.Builder().addHelp().build();

        // Act
        final String[] args = {"--hel"};

        // Assert
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void parser_missing_mandatory_argument_in_input_throws_exception() {
        // Arrange
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .addHelp()
                .build();

        // Act
        final String[] args = {"-h"};

        // Assert
        assertThrows(MissingArgumentException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void parser_with_optional_string_argument_ensures_value_present() {
        // Arrange
        final var name = "opt";
        for (String[] args : new String[][]{{"--" + name}, {"--" + name, "-h"}, {"--" + name, "-f"}}) {
            final var arg = new OptionalStringArgument.Builder()
                    .name(name)
                    .description("")
                    .build();
            final var argParser = new CmdParser.Builder()
                    .argument(arg)
                    .addHelp()
                    .build();
            assertTrue(arg.valMandatory);
            assertFalse(arg.argMandatory);

            // Act & Assert
            assertThrows(MissingValueException.class, () -> argParser.parseArguments(args));
        }
    }

    @Test
    void parser_with_optional_file_argument_ensures_value_present() {
        // Arrange
        final var name = "opt";
        for (String[] args : new String[][]{{"--" + name}, {"--" + name, "-h"}, {"--" + name, "-f"}}) {
            final var arg = new OptionalFilePathArgument.Builder()
                    .name(name)
                    .description("")
                    .isFile()
                    .build();
            final var argParser = new CmdParser.Builder()
                    .argument(arg)
                    .addHelp()
                    .build();
            assertTrue(arg.valMandatory);
            assertFalse(arg.argMandatory);

            // Act & Assert
            assertThrows(MissingValueException.class, () -> argParser.parseArguments(args));
        }
    }

    @Test
    void parser_with_optional_enum_argument_ensures_value_present() {
        // Arrange
        final var name = "opt";
        for (String[] args : new String[][]{{"--" + name}, {"--" + name, "-h"}, {"--" + name, "-f"}}) {
            final var arg = new OptionalEnumArgument.Builder<EnumArgumentTest.TestEnum>()
                    .name(name)
                    .description("")
                    .enumClass(EnumArgumentTest.TestEnum.class)
                    .build();
            final var argParser = new CmdParser.Builder()
                    .argument(arg)
                    .addHelp()
                    .build();
            assertTrue(arg.valMandatory);
            assertFalse(arg.argMandatory);

            // Act & Assert
            assertThrows(MissingValueException.class, () -> argParser.parseArguments(args));
        }
    }

    @Test
    void parser_with_optional_int_argument_ensures_value_present() {
        // Arrange
        final var name = "opt";
        for (String[] args : new String[][]{{"--" + name}, {"--" + name, "-h"}, {"--" + name, "-f"}}) {
            final var arg = new OptionalIntArgument.Builder()
                    .name(name)
                    .description("")
                    .build();
            final var argParser = new CmdParser.Builder()
                    .argument(arg)
                    .addHelp()
                    .build();
            assertTrue(arg.valMandatory);
            assertFalse(arg.argMandatory);

            // Act & Assert
            assertThrows(MissingValueException.class, () -> argParser.parseArguments(args));
        }
    }

    @Test
    void parser_with_optional_bounded_int_argument_ensures_value_present() {
        // Arrange
        final var name = "opt";
        for (String[] args : new String[][]{{"--" + name}, {"--" + name, "-h"}, {"--" + name, "-f"}}) {
            final var arg = new OptionalBoundedIntArgument.Builder()
                    .name(name)
                    .description("")
                    .minimum(10)
                    .build();
            final var argParser = new CmdParser.Builder()
                    .argument(arg)
                    .addHelp()
                    .build();
            assertTrue(arg.valMandatory);
            assertFalse(arg.argMandatory);

            // Act & Assert
            assertThrows(MissingValueException.class, () -> argParser.parseArguments(args));
        }
    }

    @Test
    void parser_missing_default_argument_in_input_is_handled() {
        // Arrange
        final int defaultValue = 23;
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

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertFalse(argParser.isPresent("extra"));
        });
    }

    @Test
    void parser_with_default_argument_identified_by_shortName_missing_value_in_input_is_handled() {
        // Arrange
        final int defaultValue = 23;
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

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertTrue(argParser.isPresent("extra"));
        });
    }

    @Test
    void parser_with_default_argument_identified_by_name_missing_value_in_input_is_handled() {
        // Arrange
        final int defaultValue = 23;
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

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertTrue(argParser.isPresent("extra"));
        });
    }

    @Test
    void parser_handles_multiple_default_arguments_in_input() {
        // Arrange
        final int defaultValue1 = 23, defaultValue2 = 12;
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

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue1, argParser.getValue("extra"));
            assertEquals(defaultValue2, argParser.getValue("int"));
            assertEquals(defaultValue3, argParser.getValue("append"));
            assertTrue(argParser.isPresent("extra"));
            assertTrue(argParser.isPresent("int"));
            assertTrue(argParser.isPresent("append"));
        });
    }

    @Test
    void parser_handles_multiple_default_arguments_identified_by_shortName_in_same_block() {
        // Arrange
        final int defaultValue = 23;
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

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertEquals(defaultValue, argParser.getValue("int"));
            assertTrue(argParser.isPresent("extra"));
            assertTrue(argParser.isPresent("int"));
        });
    }

    @Test
    void parser_handles_multiple_arguments_identified_by_shortName_in_same_block() {
        // Arrange
        final int defaultValue1 = 23, defaultValue2 = 12, defaultValue3 = -98;
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

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue1, argParser.getValue("extra"));
            assertEquals(defaultValue2, argParser.getValue("int"));
            assertEquals(defaultValue3, argParser.getValue("mint"));
            assertFalse(argParser.isPresent("extra"));
            assertTrue(argParser.isPresent("int"));
            assertTrue(argParser.isPresent("mint"));
        });
    }

    @Test
    void parser_handles_single_value_argument_with_multiple_nonvalue_arguments_in_same_block() {
        // Arrange
        final int defaultValue1 = 23, defaultValue2 = 12;
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

        // Act
        assertDoesNotThrow(() -> argParser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(defaultValue2, argParser.getValue("int"));
            assertTrue(argParser.isPresent("extra"));
            assertTrue(argParser.isPresent("int"));
            assertTrue(argParser.isPresent("mint"));
            assertTrue(argParser.isPresent("help"));
        });
    }

    @Test
    void ensure_parser_rejects_grouped_value_arguments_in_same_block_with_a_value() {
        // Arrange
        final int defaultValue1 = 23, defaultValue2 = 12;
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

        // Act
        final String[] args = {"-hxi", String.valueOf(defaultValue2)};

        // Assert
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void ensure_parser_rejects_grouped_mandatory_value_arguments_in_same_block_with_a_value() {
        // Arrange
        final int defaultValue1 = 23;
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

        // Act
        final String[] args = {"-hxi", Integer.toString(defaultValue1)};

        // Assert
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void ensure_mandatory_argument_missing_value_in_input_throws_exception() {
        // Arrange
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

        // Act
        final String[] args = {"-he", "-28", "--string", "test", "-i"};

        // Assert
        assertThrows(MissingValueException.class, () -> argParser.parseArguments(args));
    }

    /**
     * This test ensures that each Argument child has its constructor access set correctly. This test was implemented to
     * ease future development.
     */
    @Test
    void ensure_all_argument_constructor_access_is_correct_with_reflection() {
        assertDoesNotThrow(() -> {
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
        });
    }

    /**
     * This test is to ensure that each Argument child class has an implemented Builder class which is necessary for the
     * Builder class to properly work. It also ensures that the various Builders have the correct access type. This test
     * was implemented to ease future development.
     */
    @Test
    void ensure_argument_builder_access_is_correct_with_reflection() {
        assertDoesNotThrow(() -> {
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
        });
    }

    @Nested
    class testGetHelpOutput {
        final CmdParser argParser;

        testGetHelpOutput() {
            argParser = new CmdParser.Builder().arguments(
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
                                    .shortName('t')
                                    .isDirectory()
                                    .build(),
                            new MandatoryEnumArgument.Builder<EnumArgumentTest.TestEnum>()
                                    .name("enum")
                                    .description("enum argument description")
                                    .enumClass(EnumArgumentTest.TestEnum.class)
                                    .build(),
                            new DefaultEnumArgument.Builder<EnumArgumentTest.TestEnum>()
                                    .name("enum2")
                                    .description("enum2 argument description")
                                    .enumClass(EnumArgumentTest.TestEnum.class)
                                    .defaultValue(EnumArgumentTest.TestEnum.ABC)
                                    .build(),
                            new OptionalStringArgument.Builder()
                                    .name("opt")
                                    .description("argument can be missing from input, its value cannot")
                                    .build(),
                            new OptionalEnumArgument.Builder<EnumArgumentTest.OtherEnum>()
                                    .name("optionalEnum")
                                    .description("an enum value, passed optionally")
                                    .enumClass(EnumArgumentTest.OtherEnum.class)
                                    .build(),
                            new MandatoryBoundedIntArgument.Builder()
                                    .name("mandatoryBoundedInteger")
                                    .description("A description.")
                                    .minimum(8)
                                    .build(),
                            new DefaultBoundedIntArgument.Builder()
                                    .name("defaultBoundedInteger")
                                    .description("A description.")
                                    .defaultValue(4)
                                    .minimum(3)
                                    .makeValueMandatory()
                                    .build(),
                            new DefaultBoundedIntArgument.Builder()
                                    .name("defaultBoundedInteger2")
                                    .description("A description.")
                                    .shortName('b')
                                    .defaultValue(9347)
                                    .minimum(234)
                                    .maximum(87435)
                                    .build(),
                            new OptionalBoundedIntArgument.Builder()
                                    .name("optionalBoundedInteger")
                                    .description("a bounded integer value, passed optionally")
                                    .minimum(-3)
                                    .maximum(42)
                                    .build(),
                            new OptionalFilePathArgument.Builder()
                                    .name("errorLogFile")
                                    .description("File to write error messages to in case of an exception.")
                                    .isFile()
                                    .shortName('e')
                                    .build())
                    .addHelp()
                    .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut")
                    .programName("testName.jar")
                    .epilogue("labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation")
                    .build();
        }

        @Test
        void correctly_formatted_for_default_width() {
            // Arrange
            final String expectedOutput = """
                    Usage: testName.jar --int INTEGER -t FILEPATH --enum ENUM
                    --mandatoryBoundedInteger INTEGER [-h] [-i [INTEGER]] [-s [STRING]] [--enum2
                    [ENUM]] [--opt STRING] [--optionalEnum ENUM] [--defaultBoundedInteger INTEGER]
                    [-b [INTEGER]] [--optionalBoundedInteger INTEGER] [-e FILEPATH]
                                        
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
                    incididunt ut
                                        
                    Mandatory arguments:
                        --int INTEGER   some integer
                    -t, --txt FILEPATH  Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed
                                        do eiusmod tempor incididunt ut Expects a folder.
                        --enum ENUM     enum argument description Possible values: [abc, ABC, none]
                        --mandatoryBoundedInteger INTEGER
                                        A description. Minimum: [8]
                                        
                    Optional arguments:
                    -h, --help          Show this help message and exit.
                    -i, --int2 [INTEGER]
                                        Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed
                                        do eiusmod tempor incididunt ut Default value: [23]
                    -s, --str [STRING]  some sort of string, dunno, not gonna use it. Default value:
                                        [string]
                        --enum2 [ENUM]  enum2 argument description Possible values: [abc, ABC, none]
                                        Default value: [ABC]
                        --opt STRING    argument can be missing from input, its value cannot
                        --optionalEnum ENUM
                                        an enum value, passed optionally Possible values: [ONE, TWO,
                                        THREE]
                        --defaultBoundedInteger INTEGER
                                        A description. Minimum: [3] Default value: [4]
                    -b, --defaultBoundedInteger2 [INTEGER]
                                        A description. Minimum: [234] Maximum: [87435] Default
                                        value: [9347]
                        --optionalBoundedInteger INTEGER
                                        a bounded integer value, passed optionally Minimum: [-3]
                                        Maximum: [42]
                    -e, --errorLogFile FILEPATH
                                        File to write error messages to in case of an exception.
                                        Expects a file.
                                        
                    labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
                    exercitation
                    """;

            // Act & Assert
            assertEquals(expectedOutput, argParser.getHelp());
        }

        @Test
        void correctly_formatted_for_125_width() {
            // Arrange
            final String expectedOutput = """
                    Usage: testName.jar --int INTEGER -t FILEPATH --enum ENUM --mandatoryBoundedInteger INTEGER [-h] [-i [INTEGER]] [-s [STRING]]
                    [--enum2 [ENUM]] [--opt STRING] [--optionalEnum ENUM] [--defaultBoundedInteger INTEGER] [-b [INTEGER]]
                    [--optionalBoundedInteger INTEGER] [-e FILEPATH]
                                       
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut
                                       
                    Mandatory arguments:
                        --int INTEGER                       some integer
                    -t, --txt FILEPATH                      Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
                                                            incididunt ut Expects a folder.
                        --enum ENUM                         enum argument description Possible values: [abc, ABC, none]
                        --mandatoryBoundedInteger INTEGER   A description. Minimum: [8]
                                       
                    Optional arguments:
                    -h, --help                              Show this help message and exit.
                    -i, --int2 [INTEGER]                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
                                                            incididunt ut Default value: [23]
                    -s, --str [STRING]                      some sort of string, dunno, not gonna use it. Default value: [string]
                        --enum2 [ENUM]                      enum2 argument description Possible values: [abc, ABC, none] Default value: [ABC]
                        --opt STRING                        argument can be missing from input, its value cannot
                        --optionalEnum ENUM                 an enum value, passed optionally Possible values: [ONE, TWO, THREE]
                        --defaultBoundedInteger INTEGER     A description. Minimum: [3] Default value: [4]
                    -b, --defaultBoundedInteger2 [INTEGER]  A description. Minimum: [234] Maximum: [87435] Default value: [9347]
                        --optionalBoundedInteger INTEGER    a bounded integer value, passed optionally Minimum: [-3] Maximum: [42]
                    -e, --errorLogFile FILEPATH             File to write error messages to in case of an exception. Expects a file.
                                       
                    labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation
                    """;

            // Act & Assert
            assertEquals(expectedOutput, argParser.getHelp(125, 40));
        }

        @Test
        void correctly_formatted_for_60_width() {
            final String expectedOutput = """
                    Usage: testName.jar --int INTEGER -t FILEPATH --enum ENUM
                    --mandatoryBoundedInteger INTEGER [-h] [-i [INTEGER]] [-s
                    [STRING]] [--enum2 [ENUM]] [--opt STRING] [--optionalEnum
                    ENUM] [--defaultBoundedInteger INTEGER] [-b [INTEGER]]
                    [--optionalBoundedInteger INTEGER] [-e FILEPATH]
                                        
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed
                    do eiusmod tempor incididunt ut
                                        
                    Mandatory arguments:
                        --int INTEGER
                                   some integer
                    -t, --txt FILEPATH
                                   Lorem ipsum dolor sit amet, consectetur
                                   adipiscing elit, sed do eiusmod tempor
                                   incididunt ut Expects a folder.
                        --enum ENUM
                                   enum argument description Possible values:
                                   [abc, ABC, none]
                        --mandatoryBoundedInteger INTEGER
                                   A description. Minimum: [8]
                                        
                    Optional arguments:
                    -h, --help     Show this help message and exit.
                    -i, --int2 [INTEGER]
                                   Lorem ipsum dolor sit amet, consectetur
                                   adipiscing elit, sed do eiusmod tempor
                                   incididunt ut Default value: [23]
                    -s, --str [STRING]
                                   some sort of string, dunno, not gonna use it.
                                   Default value: [string]
                        --enum2 [ENUM]
                                   enum2 argument description Possible values:
                                   [abc, ABC, none] Default value: [ABC]
                        --opt STRING
                                   argument can be missing from input, its value
                                   cannot
                        --optionalEnum ENUM
                                   an enum value, passed optionally Possible
                                   values: [ONE, TWO, THREE]
                        --defaultBoundedInteger INTEGER
                                   A description. Minimum: [3] Default value:
                                   [4]
                    -b, --defaultBoundedInteger2 [INTEGER]
                                   A description. Minimum: [234] Maximum:
                                   [87435] Default value: [9347]
                        --optionalBoundedInteger INTEGER
                                   a bounded integer value, passed optionally
                                   Minimum: [-3] Maximum: [42]
                    -e, --errorLogFile FILEPATH
                                   File to write error messages to in case of an
                                   exception. Expects a file.
                                        
                    labore et dolore magna aliqua. Ut enim ad minim veniam, quis
                    nostrud exercitation
                    """;

            // Act & Assert
            assertEquals(expectedOutput, argParser.getHelp(60));
        }
    }
}
