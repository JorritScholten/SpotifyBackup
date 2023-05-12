package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

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
                new StringArgument("string", ""),
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
                new StringArgument("string", "", 'a')
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
    void testFilePathArgument1(@TempDir File temp_dir) {
        final String value = temp_dir.toString();
        assert new File(value).exists();
        final String[] args = {"-h", "--extra", value};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new FilePathArgument("extra", "", true, true)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(new File(value).getAbsoluteFile(), argParser.getValue("extra"));
        });
    }

    @Test
    void testFilePathArgument2(@TempDir File temp_dir) {
        assertDoesNotThrow(() -> {
            File temp_file = File.createTempFile("test", ".txt", temp_dir);
            assert temp_file.exists();
            temp_file.deleteOnExit();
            final String value = temp_file.toString();
            assert new File(value).exists();

            final String[] args = {"-h", "--extra", value};
            CmdParser argParser = new CmdParser(new Argument[]{
                    new FlagArgument("help", "Print program help and exit.", 'h'),
                    new FilePathArgument("extra", "", true, false)
            });
            argParser.parseArguments(args);
            assertEquals(new File(value).getAbsoluteFile(), argParser.getValue("extra"));
        });
    }

    @Test
    void testMalformedFilePathArgument1(@TempDir File temp_dir) {
        final String value = temp_dir.toString();
        assert new File(value).exists();
        final String[] args = {"-h", "--extra", value};
        CmdParser argParser = new CmdParser(new Argument[]{
                new FlagArgument("help", "Print program help and exit.", 'h'),
                new FilePathArgument("extra", "", true, false)
        });
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void testMalformedFilePathArgument2(@TempDir File temp_dir) {
        assertDoesNotThrow(() -> {
            File temp_file = File.createTempFile("test", "txt", temp_dir);
            assert temp_file.exists();
            temp_file.deleteOnExit();
            final String value = temp_file.toString();
            assert new File(value).exists();

            final String[] args = {"-h", "--extra", value};
            CmdParser argParser = new CmdParser(new Argument[]{
                    new FlagArgument("help", "Print program help and exit.", 'h'),
                    new FilePathArgument("extra", "", true, true)
            });
            assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
        });
    }
}
