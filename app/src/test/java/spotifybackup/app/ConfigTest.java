package spotifybackup.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import spotifybackup.app.exception.BlankConfigFieldException;
import spotifybackup.app.exception.ConfigFileException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableMiscTests", matches = "true")
class ConfigTest {
    File configFile;

    @BeforeEach
    void create_new_config_file(@TempDir Path tempDir) {
        configFile = tempDir.resolve("config.json").toFile();
        Config.reloadFieldsForTesting();
    }

    @Test
    void ensure_new_empty_file_created_properly() throws IOException {
        // Arrange
        final String newConfig;
        final String blankConfig = """
                {
                  "clientId": "",
                  "redirectURI": "",
                  "clientSecret": "",
                  "refreshToken": ""
                }
                """;

        // Act
        assertThrows(ConfigFileException.class, () -> Config.loadFromFile(configFile));

        // Assert
        newConfig = Files.readString(configFile.toPath());
        assertEquals(blankConfig, newConfig);
    }

    @Test
    void ensure_blank_config_fields_are_rejected() throws IOException {
        // Arrange
        final String configContents = """
                {
                  "clientId": "",
                  "redirectURI": "http://localhost:1234"
                }
                """;
        Files.writeString(configFile.toPath(), configContents);

        // Act & Assert
        assertThrows(BlankConfigFieldException.class, () -> Config.loadFromFile(configFile));
    }

    @Test
    void ensure_required_fields_are_present() throws IOException {
        // Arrange
        final String configContents = """
                {
                  "redirectURI": "http://localhost:1234",
                  "clientSecret": "123"
                }
                """;
        Files.writeString(configFile.toPath(), configContents);

        // Act & Assert
        assertThrows(BlankConfigFieldException.class, () -> Config.loadFromFile(configFile));
    }

    @Test
    void ensure_all_fields_are_retrieved() throws IOException, URISyntaxException {
        // Arrange
        final String configContents = """
                {
                  "clientId": "abcdefg",
                  "redirectURI": "http://localhost:1234",
                  "clientSecret": "123",
                  "refreshToken": "1a2b3c"
                }
                """;
        final String clientId = "abcdefg";
        final URI redirectURI = new URI("http://localhost:1234");
        final String clientSecret = "123";
        final String refreshToken = "1a2b3c";
        Files.writeString(configFile.toPath(), configContents);

        // Act
        Config.loadFromFile(configFile);

        // Assert
        assertEquals(clientId, Config.clientId.get());
        assertEquals(redirectURI, Config.redirectURI.get());
        assertEquals(clientSecret, Config.clientSecret.get().orElseThrow());
        assertEquals(refreshToken, Config.refreshToken.get().orElseThrow());
    }

    @Test
    void create_new_config_file_and_load_values_into_it() throws IOException, URISyntaxException {
        // Arrange
        assertThrows(ConfigFileException.class, () -> Config.loadFromFile(configFile));
        final String clientId = "some-client-id";
        final URI redirectURI = new URI("http://localhost:5678");

        // Act
        Config.clientId.set(clientId);
        Config.redirectURI.set(redirectURI);

        // Assert
        assertDoesNotThrow(() -> Config.loadFromFile(configFile));
        assertEquals(clientId, Config.clientId.get());
        assertEquals(redirectURI, Config.redirectURI.get());
    }
}
