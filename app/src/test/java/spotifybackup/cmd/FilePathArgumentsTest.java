package spotifybackup.cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spotifybackup.cmd.argument.file.DefaultFilePathArgument;
import spotifybackup.cmd.argument.file.MandatoryFilePathArgument;
import spotifybackup.cmd.exception.IllegalConstructorParameterException;
import spotifybackup.cmd.exception.MalformedInputException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class FilePathArgumentsTest {
    @Test
    void testFilePathArgument1(@TempDir File temp_dir) {
        final String value = temp_dir.toString();
        assert new File(value).exists();
        final String[] args = {"-h", "--extra", value};
        CmdParser argParser = new CmdParser.Builder()
                .argument(new MandatoryFilePathArgument.Builder()
                        .name("extra")
                        .description("")
                        .isDirectory()
                        .build())
                .addHelp()
                .build();
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
    void testMalformedFilePathArgument1(@TempDir File temp_dir) {
        final String value = temp_dir.toString();
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
    void testMalformedFilePathArgument2(@TempDir File temp_dir) {
        assertDoesNotThrow(() -> {
            File temp_file = File.createTempFile("test", "txt", temp_dir);
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
    void testMissingBuilderParameter2(@TempDir File temp_dir) {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder()
                        .argument(new DefaultFilePathArgument.Builder()
                                        .name("extra")
                                        .description("")
//                        .isDirectory()
                                        .defaultValue(temp_dir)
                                        .build()
                        )
        );
    }

    @Test
    void testMissingBuilderParameter3(@TempDir File temp_dir) {
        assertThrows(IllegalConstructorParameterException.class, () -> new CmdParser.Builder()
                        .argument(new DefaultFilePathArgument.Builder()
                                        .name("extra")
                                        .description("")
                                        .isDirectory()
//                        .defaultValue(temp_dir)
                                        .build()
                        )
        );
    }

    @Test
    void testDefaultFilePathArgument1(@TempDir File temp_dir) {
        final String value = temp_dir.toString();
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
