package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import spotifybackup.cmd.argument.integer.DefaultBoundedIntArgument;
import spotifybackup.cmd.argument.integer.DefaultIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryBoundedIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryIntArgument;
import spotifybackup.cmd.exception.ArgumentsNotParsedException;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import static org.junit.jupiter.api.Assertions.*;

public class IntArgumentsTest {
    @Test
    void testIntArgument1() {
        final Integer value = 34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testIntArgument2() {
        final Integer value = -34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .build())
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultIntArgument1() {
        final int value = 34, defaultValue = 12;
        final String[] args = {"-e", Integer.toString(value)};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .build())
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertNotEquals(defaultValue, argParser.getValue("extra"));
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultIntArgument2() {
        final int value = -34;
        final String[] args = {"--extra", Integer.toString(value)};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .defaultValue(value)
                        .build())
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testBoundedIntArgument() {
        final Integer value = 34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .minimum(1)
                        .build())
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultBoundedIntArgument1() {
        final int value = 34, defaultValue = 12;
        final String[] args = {"-e", Integer.toString(value)};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(defaultValue)
                        .minimum(1)
                        .build())
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertNotEquals(defaultValue, argParser.getValue("extra"));
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultBoundedIntArgument2() {
        final int value = 34, defaultValue = 12, value2 = 5;
        final String[] args = {};
        CmdParser argParser = new CmdParser.Builder().arguments(
                        new DefaultBoundedIntArgument.Builder()
                                .name("extra")
                                .description("")
                                .shortName('e')
                                .defaultValue(defaultValue)
                                .minimum(1)
                                .build(),
                        new DefaultBoundedIntArgument.Builder()
                                .name("int")
                                .description("")
                                .shortName('i')
                                .defaultValue(value2)
                                .minimum(1)
                                .maximum(20)
                                .build())
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertNotEquals(value, argParser.getValue("extra"));
            assertEquals(defaultValue, argParser.getValue("extra"));
            assertEquals(value2, argParser.getValue("int"));
        });
    }

    @Test
    void testMalformedBoundedIntArgument1() {
        final Integer value = -34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .minimum(1)
                        .build())
                .build();
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                assertEquals(value, argParser.getValue("extra"))
        );
    }

    @Test
    void testMalformedBoundedIntArgument2() {
        final Integer value = 34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .minimum(1)
                        .maximum(20)
                        .build())
                .build();
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                assertEquals(value, argParser.getValue("extra"))
        );
    }

    @Test
    void testNullDefaultValueInt1() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .build()
        ).build());
    }

    @Test
    void testNullDefaultValueInt2() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .minimum(20)
                        .build()
        ).build());
    }

    @Test
    void testNullDefaultValueInt3() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .defaultValue(20)
                        .build()
        ).build());
    }

    @Test
    void testRangeCheckDefaultBoundedIntConstructor1() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .defaultValue(-5)
                        .minimum(20)
                        .build()
        ).build());
    }

    @Test
    void testRangeCheckDefaultBoundedIntConstructor2() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .minimum(20)
                        .defaultValue(25)
                        .maximum(23)
                        .build()
        ).build());
    }

    @Test
    void testRangeCheckDefaultBoundedIntConstructor3() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .minimum(20)
                        .defaultValue(23)
                        .maximum(20)
                        .build()
        ).build());
    }

    @Test
    void testRangeCheckDefaultBoundedIntConstructor4() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder().argument(
                new DefaultBoundedIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .minimum(20)
                        .defaultValue(23)
                        .maximum(19)
                        .build()
        ).build());
    }

    @Test
    void testMalformedIntArgument1() {
        final String[] args = {"-he"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
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
    void testMalformedIntArgument2() {
        final String[] args = {"-he", "21.2"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
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
    void testMalformedIntArgument3() {
        final String[] args = {"-he", "sdf"};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryIntArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
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
}
