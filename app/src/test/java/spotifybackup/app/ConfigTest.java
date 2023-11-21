package spotifybackup.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import spotifybackup.app.exception.ConfigFileException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(named = "EnableMiscTests", matches = "true")
class ConfigTest {
    @Test
    void ensure_new_empty_file_created_properly(@TempDir Path tempDir) throws IOException {
        // Arrange
        final String configFileName = "config.json";
        final File file = tempDir.resolve(configFileName).toFile();
        final String newConfig;
        final String blankConfig = """
                {
                    "clientId":"",
                    "redirectURI":""
                }
                """;
        System.out.println(file);

        // Act
        assertThrows(ConfigFileException.class, () -> new Config(file));

        // Assert
        newConfig = Files.readString(file.toPath());
        assertEquals(blankConfig, newConfig);
    }
}
