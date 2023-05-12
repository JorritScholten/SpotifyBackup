package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class FilePathArgumentsTest {
    @Test
    void testFilePathArgument1(@TempDir File temp_dir) {
        final String value = temp_dir.toString();
        assert new File(value).exists();
        final String[] args = {"-h", "--extra", value};
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryFilePathArgument("extra", "", true)
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
                    new MandatoryFilePathArgument("extra", "", false)
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
                new MandatoryFilePathArgument("extra", "", 'e', false)
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
                    new MandatoryFilePathArgument("extra", "", 'e', true)
            });
            assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
        });
    }

    @Test
    void testDefaultFilePathArgument1(@TempDir File temp_dir) {
        final String value = temp_dir.toString();
        assert new File(value).exists();
        final String[] args = {};
        CmdParser argParser = new CmdParser(new Argument[]{
                new DefaultFilePathArgument("extra", "", new File(value), true)
        });
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(new File(value).getAbsoluteFile(), argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultFilePathArgument2(@TempDir File temp_dir) {
        assertDoesNotThrow(() -> {
            File temp_file = File.createTempFile("test", ".txt", temp_dir);
            assert temp_file.exists();
            temp_file.deleteOnExit();
            File temp_file2 = File.createTempFile("test", ".txt", temp_dir);
            assert temp_file2.exists();
            temp_file2.deleteOnExit();
            final String value = temp_file.toString(), value2 = temp_file2.toString();
            assert new File(value).exists();

            final String[] args = {"-e", value2};
            CmdParser argParser = new CmdParser(new Argument[]{
                    new DefaultFilePathArgument("extra", "", 'e', new File(value), false)
            });
            argParser.parseArguments(args);
            assertEquals(new File(value2).getAbsoluteFile(), argParser.getValue("extra"));
            assertNotEquals(new File(value).getAbsoluteFile(), argParser.getValue("extra"));
        });
    }

    @Test
    void testNullDefaultValue() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser(new Argument[]{
                new DefaultFilePathArgument("extra", "", null, true)
        }));
    }
}
