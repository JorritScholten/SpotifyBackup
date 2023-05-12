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
                new IntArgument("extra", "", 'e')
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
                new IntArgument("extra", "", 'e'),
                new StringArgument("string", ""),
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
                new IntArgument("extra", "", 'e'),
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
