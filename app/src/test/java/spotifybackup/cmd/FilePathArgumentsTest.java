package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import spotifybackup.cmd.argument.file.DefaultFilePathArgument;
import spotifybackup.cmd.argument.file.MandatoryFilePathArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FilePathArgumentsTest {
    @TempDir
    static File sharedTempDir;

    @ParameterizedTest
    @ValueSource(strings = {"--extra", "-e"})
    void mandatory_argument_loads_directory_path_from_input(String identifier) {
        // Arrange
        final String value = sharedTempDir.toString();
        assert new File(value).exists();
        final String name = "extra";
        final String[] args = {"-h", identifier, value};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryFilePathArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .isDirectory()
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(new File(value).getAbsoluteFile(), parser.getValue(name));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"--extra", "-e"})
    void default_argument_loads_directory_path_from_input(String identifier) {
        // Arrange
        final String value = sharedTempDir.toString();
        assert new File(value).exists();
        final String name = "extra";
        final String[] args = {"-h", identifier, value};
        var parser = new CmdParser.Builder()
                .argument(new DefaultFilePathArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .defaultValue(new File(value))
                        .isDirectory()
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(new File(value).getAbsoluteFile(), parser.getValue(name));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"--extra", "-e"})
    void mandatory_argument_loads_file_path_from_input(String identifier) throws IOException {
        // Arrange
        File temp_file = File.createTempFile("test", ".txt", sharedTempDir);
        assert temp_file.exists();
        temp_file.deleteOnExit();
        final String value = temp_file.toString();
        assert new File(value).exists();
        final String name = "extra";
        final String[] args = {"-h", identifier, value};
        var parser = new CmdParser.Builder()
                .argument(new MandatoryFilePathArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .isFile()
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(new File(value).getAbsoluteFile(), parser.getValue(name));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"--extra", "-e"})
    void default_argument_loads_file_path_from_input(String identifier) throws IOException {
        // Arrange
        File temp_file = File.createTempFile("test", ".txt", sharedTempDir);
        assert temp_file.exists();
        temp_file.deleteOnExit();
        File temp_file2 = File.createTempFile("test2", ".txt", sharedTempDir);
        assert temp_file2.exists();
        temp_file2.deleteOnExit();
        final String value = temp_file.toString(), value2 = temp_file2.toString();
        assert new File(value).exists();
        final String name = "extra";
        final String[] args = {"-h", identifier, value};
        var parser = new CmdParser.Builder()
                .argument(new DefaultFilePathArgument.Builder()
                        .name("extra")
                        .description("")
                        .shortName('e')
                        .defaultValue(new File(value))
                        .isFile()
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(new File(value).getAbsoluteFile(), parser.getValue(name));
            assertNotEquals(new File(value2).getAbsoluteFile(), parser.getValue(name));
        });
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"--extra", "-e"})
    void default_argument_present_in_input_without_value_returns_defaultValue(String identifier) throws IOException {
        // Arrange
        File temp_file = File.createTempFile("test", ".txt", sharedTempDir);
        assert temp_file.exists();
        temp_file.deleteOnExit();
        final String default_value = temp_file.toString();
        assert new File(default_value).exists();
        final String name = "extra";
        final String[] args;
        if (identifier != null) {
            args = new String[]{"-h", identifier};
        } else {
            args = new String[]{"-h"};
        }
        var parser = new CmdParser.Builder()
                .argument(new DefaultFilePathArgument.Builder()
                        .name(name)
                        .description("")
                        .shortName('e')
                        .defaultValue(new File(default_value))
                        .isFile()
                        .build())
                .addHelp()
                .build();

        // Act
        assertDoesNotThrow(() -> parser.parseArguments(args));

        // Assert
        assertDoesNotThrow(() -> {
            assertEquals(new File(default_value).getAbsoluteFile(), parser.getValue(name));
            if (identifier != null) {
                assertTrue(parser.isPresent(name));
            } else {
                assertFalse(parser.isPresent(name));
            }
        });
    }

    @Test
    void argument_expecting_file_path_but_passed_directory_path_throws_exception() {
        // Arrange
        final String value = sharedTempDir.toString();
        assert new File(value).exists();
        final String[] args = {"-h", "--extra", value};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryFilePathArgument.Builder()
                        .name("extra")
                        .description("")
                        .isFile()
                        .shortName('e')
                        .build())
                .build();

        // Act & Assert
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void argument_expecting_directory_path_but_passed_file_path_throws_exception() throws IOException {
        // Arrange
        File temp_file = File.createTempFile("test", "txt", sharedTempDir);
        assert temp_file.exists();
        temp_file.deleteOnExit();
        final String value = temp_file.toString();
        assert new File(value).exists();
        final String[] args = {"-h", "--extra", value};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryFilePathArgument.Builder()
                        .name("extra")
                        .description("")
                        .isDirectory()
                        .shortName('e')
                        .build())
                .build();

        // Act & Assert
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void mandatory_argument_validates_that_path_type_is_not_null() {
        // Arrange
        var builder = new MandatoryFilePathArgument.Builder()
                .name("extra")
                .description("");

        // Act
//        builder.isDirectory();

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void default_argument_validates_that_path_type_is_not_null() {
        // Arrange
        var builder = new DefaultFilePathArgument.Builder()
                .name("extra")
                .description("")
                .defaultValue(sharedTempDir);

        // Act
//        builder.isDirectory();

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }

    @Test
    void default_argument_validates_defaultValue_not_null() {
        // Arrange
        var builder = new DefaultFilePathArgument.Builder()
                .name("extra")
                .description("")
                .isDirectory();

        // Act
        // builder.defaultValue()

        // Assert
        assertThrows(IllegalConstructorParameterException.class, builder::build);
    }
}
