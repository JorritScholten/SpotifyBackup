package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spotifybackup.cmd.argument.file.DefaultFilePathArgument;
import spotifybackup.cmd.argument.file.MandatoryFilePathArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.io.File;

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

    @Test
    void testDefaultFilePathArgument1() {
        final String value = sharedTempDir.toString();
        assert new File(value).exists();
        final String[] args = {};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new DefaultFilePathArgument.Builder()
                        .name("extra")
                        .description("")
                        .defaultValue(new File(value))
                        .isDirectory()
                        .build())
                .build();
        assertDoesNotThrow(() -> {
            argParser.parseArguments(args);
            assertEquals(new File(value).getAbsoluteFile(), argParser.getValue("extra"));
        });
    }

    @Test
    void testFilePathArgument2() {
        assertDoesNotThrow(() -> {
            File temp_file = File.createTempFile("test", ".txt", sharedTempDir);
            assert temp_file.exists();
            temp_file.deleteOnExit();
            final String value = temp_file.toString();
            assert new File(value).exists();

            final String[] args = {"-h", "--extra", value};
            CmdParser argParser = new CmdParser.Builder()
                    .argument(new MandatoryFilePathArgument.Builder()
                            .name("extra")
                            .description("")
                            .isFile()
                            .build())
                    .addHelp()
                    .build();
            argParser.parseArguments(args);
            assertEquals(new File(value).getAbsoluteFile(), argParser.getValue("extra"));
        });
    }

    @Test
    void testDefaultFilePathArgument2() {
        assertDoesNotThrow(() -> {
            File temp_file = File.createTempFile("test", ".txt", sharedTempDir);
            assert temp_file.exists();
            temp_file.deleteOnExit();
            File temp_file2 = File.createTempFile("test", ".txt", sharedTempDir);
            assert temp_file2.exists();
            temp_file2.deleteOnExit();
            final String value = temp_file.toString(), value2 = temp_file2.toString();
            assert new File(value).exists();

            final String[] args = {"-e", value2};
            CmdParser argParser = new CmdParser.Builder()
                    .argument(new DefaultFilePathArgument.Builder()
                            .name("extra")
                            .description("")
                            .shortName('e')
                            .defaultValue(new File(value))
                            .isFile()
                            .build())
                    .build();
            argParser.parseArguments(args);
            assertEquals(new File(value2).getAbsoluteFile(), argParser.getValue("extra"));
            assertNotEquals(new File(value).getAbsoluteFile(), argParser.getValue("extra"));
        });
    }

    @Test
    void testMalformedFilePathArgument1() {
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
        assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
    }

    @Test
    void testMalformedFilePathArgument2() {
        assertDoesNotThrow(() -> {
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
            assertThrows(MalformedInputException.class, () -> argParser.parseArguments(args));
        });
    }

    @Test
    void testMissingBuilderParameter1() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder()
                        .argument(new MandatoryFilePathArgument.Builder()
                                        .name("extra")
                                        .description("")
//                        .isDirectory()
                                        .build()
                        )
        );
    }

    @Test
    void testMissingBuilderParameter2() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder()
                        .argument(new DefaultFilePathArgument.Builder()
                                        .name("extra")
                                        .description("")
//                        .isDirectory()
                                        .defaultValue(sharedTempDir)
                                        .build()
                        )
        );
    }

    @Test
    void testMissingBuilderParameter3() {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder()
                        .argument(new DefaultFilePathArgument.Builder()
                                        .name("extra")
                                        .description("")
                                        .isDirectory()
//                        .defaultValue(sharedTempDir)
                                        .build()
                        )
        );
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
