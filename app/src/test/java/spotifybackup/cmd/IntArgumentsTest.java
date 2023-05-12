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
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryIntArgument("extra", "", 'e')
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testIntArgument2() {
        final Integer value = -34;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryIntArgument("extra", "", 'e')
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultIntArgument1() {
        final Integer value = 34, defaultValue = 12;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultIntArgument("extra", "", 'e', defaultValue)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertNotEquals(defaultValue, argParser.getValue("extra"));
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultIntArgument2() {
        final Integer value = -34;
        final String[] args = {"--extra", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultIntArgument("extra", "", value)
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
                new MandatoryBoundedIntArgument("extra", "", 'e', 1)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultBoundedIntArgument1() {
        final Integer value = 34, defaultValue = 12;
        final String[] args = {"-e", value.toString()};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultBoundedIntArgument("extra", "", 'e', defaultValue, 1)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertNotEquals(defaultValue, argParser.getValue("extra"));
            assertEquals(value, argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultBoundedIntArgument2() {
        final Integer value = 34, defaultValue = 12, value2 = 5;
        final String[] args = {};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultBoundedIntArgument("extra", "", 'e', defaultValue, 1),
                new DefaultBoundedIntArgument("int", "", 'i', value2, 1, 10)
        });
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
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryBoundedIntArgument("extra", "", 1)
        });
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
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryBoundedIntArgument("extra", "", 1, 20)
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                assertEquals(value, argParser.getValue("extra"))
        );
    }

    @Test
    void testNullDefaultValueInt1() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new DefaultIntArgument("extra", "", null)
        }));
    }

    @Test
    void testNullDefaultValueInt2() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new DefaultBoundedIntArgument("extra", "", null, 20)
        }));
    }

    @Test
    void testNullDefaultValueInt3() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new DefaultBoundedIntArgument("extra", "", 20, null)
        }));
    }

    @Test
    void testNullDefaultValueInt4() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new DefaultBoundedIntArgument("extra", "", 20, 1, null)
        }));
    }

    @Test
    void testNullDefaultValueInt5() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new DefaultBoundedIntArgument("extra", "", 'e', 20, 1, null)
        }));
    }

    @Test
    void testRangeCheckDefaultBoundedIntConstructor1() {
        final Integer value = -5;
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new DefaultBoundedIntArgument("extra", "", value, 20)
        }));
    }

    @Test
    void testRangeCheckDefaultBoundedIntConstructor2() {
        final Integer value = 25;
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new DefaultBoundedIntArgument("extra", "", value, 20, 23)
        }));
    }

    @Test
    void testMalformedIntArgument1() {
        final String[] args = {"-he"};
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryIntArgument("extra", "", 'e')
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
                new MandatoryIntArgument("extra", "", 'e')
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
                new MandatoryIntArgument("extra", "", 'e')
        });
        assertThrows(MalformedInputException.class, () ->
                argParser.parseArguments(args)
        );
        assertThrows(ArgumentsNotParsedException.class, () ->
                argParser.getValue("help")
        );
    }
}
