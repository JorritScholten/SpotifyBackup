package spotifybackup.app;

import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.opentest4j.AssertionFailedError;
import spotifybackup.app.exception.BlankConfigFieldException;
import spotifybackup.app.exception.ConfigFileException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableMiscTests", matches = "true")
class ConfigTest {
    File configFile;

    @BeforeEach
    void create_new_config_file(@TempDir Path tempDir) {
        configFile = tempDir.resolve("config.json").toFile();
        configFile.deleteOnExit();
    }

    @Test
    void ensure_new_empty_file_created_properly() throws IOException {
        // Arrange
        final String newConfig;
        final String blankConfig = """
                {
                  "clientId": "",
                  "redirectURI": "",
                  "users": []
                }
                """;

        // Act
        assertThrows(ConfigFileException.class, () -> Config.loadAppConfigFromFile(configFile));

        // Assert
        newConfig = Files.readString(configFile.toPath());
        assertEquals(blankConfig, newConfig);
    }

    @ParameterizedTest
    @CsvFileSource(files = "src/test/java/spotifybackup/app/blank_or_missing_fields_config_test.csv", numLinesToSkip = 1, delimiter = '`')
    void ensure_blank_or_missing_fields_are_rejected(final boolean shouldThrow, final String message, final String json) throws IOException {
        // Arrange
        Files.writeString(configFile.toPath(), json.substring(2, json.length() - 1));

        // Act & Assert
        if (shouldThrow) {
            boolean noIssue = false;
            try {
                Config.loadAppConfigFromFile(configFile);
            } catch (BlankConfigFieldException e) {
                if (e.getMessage().split("\n").length == 2) noIssue = true;
                else throw new AssertionFailedError("Test case should only test one failure point in isolation, test " +
                        "case tests " + (e.getMessage().split("\n").length - 1) + " failure points.");
            }
            if (!noIssue)
                throw AssertionFailureBuilder.assertionFailure().message(message)
                        .reason(String.format("Expected %s to be thrown, but nothing was thrown.",
                                BlankConfigFieldException.class.getCanonicalName())
                        ).build();
        } else assertDoesNotThrow(() -> Config.loadAppConfigFromFile(configFile), message);
    }

    @Test
    void ensure_all_fields_are_retrieved() throws IOException, URISyntaxException {
        // Arrange
        final String configContents = """
                {
                  "clientId": "abcdefg",
                  "redirectURI": "http://localhost:1234",
                  "clientSecret": "123",
                  "users": [
                    {
                      "spotifyId": "user1",
                      "displayName": "User 1",
                      "refreshToken": "token-1",
                      "doBackup": true
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "token-2",
                      "doBackup": false
                    }
                  ]
                }
                """;
        final String clientId = "abcdefg";
        final URI redirectURI = new URI("http://localhost:1234");
        final String clientSecret = "123";
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                        .spotifyId("user1")
                        .displayName("User 1")
                        .refreshToken("token-1")
                        .doBackup(true)
                        .build(),
                Config.UserInfo.builder()
                        .spotifyId("user2")
                        .displayName("User 2")
                        .refreshToken("token-2")
                        .build()
        );
        Files.writeString(configFile.toPath(), configContents);

        // Act
        Config.loadAppConfigFromFile(configFile);

        // Assert
        assertEquals(clientId, App.config.getClientId());
        assertEquals(redirectURI, App.config.getRedirectURI());
        assertEquals(clientSecret, App.config.getClientSecret().orElseThrow());
        assertEquals(users, App.config.getUsers());
    }

    @Test
    void ensure_new_config_file_with_loaded_values_is_properly_formatted() throws IOException, URISyntaxException {
        // Arrange
        assertThrows(ConfigFileException.class, () -> Config.loadAppConfigFromFile(configFile));
        final String configContents = """
                {
                  "clientId": "abcdefg",
                  "redirectURI": "http://localhost:1234",
                  "clientSecret": "123",
                  "users": [
                    {
                      "spotifyId": "user1",
                      "displayName": "User 1",
                      "refreshToken": "token-1",
                      "doBackup": false
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "token-2",
                      "doBackup": true,
                      "cloneTargets": [
                        "user1"
                      ]
                    }
                  ]
                }
                """;
        final String clientId = "abcdefg";
        final URI redirectURI = new URI("http://localhost:1234");
        final String clientSecret = "123";
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                        .spotifyId("user1")
                        .displayName("User 1")
                        .refreshToken("token-1")
                        .build(),
                Config.UserInfo.builder()
                        .spotifyId("user2")
                        .displayName("User 2")
                        .refreshToken("token-2")
                        .doBackup(true)
                        .build()
        );
        final Config config = Config.createNewForTesting(configFile);

        // Act
        config.setClientId(clientId);
        config.setRedirectURI(redirectURI);
        config.setClientSecret(clientSecret);
        for (var user : users) {
            var newUser = config.addEmptyUser(false);
            newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
            newUser.setDisplayName(user.getDisplayName().orElseThrow());
            newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
            newUser.setDoBackup(user.getDoBackup());
        }
        users.getLast().addCloneTarget(users.getFirst());

        // Assert
        final String newConfig = Files.readString(configFile.toPath());
        assertEquals(configContents, newConfig);
    }

    @Test
    void create_new_config_file_and_load_values_into_it() throws URISyntaxException {
        // Arrange
        assertThrows(ConfigFileException.class, () -> Config.loadAppConfigFromFile(configFile));
        final String clientId = "some-client-id";
        final URI redirectURI = new URI("http://localhost:5678");
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                        .spotifyId("user1")
                        .displayName("User 1")
                        .refreshToken("token-1")
                        .build(),
                Config.UserInfo.builder()
                        .spotifyId("user2")
                        .displayName("User 2")
                        .refreshToken("token-2")
                        .build()
        );
        final Config config = Config.createNewForTesting(configFile);

        // Act
        config.setClientId(clientId);
        config.setRedirectURI(redirectURI);
        for (var user : users) {
            var newUser = config.addEmptyUser(false);
            newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
            newUser.setDisplayName(user.getDisplayName().orElseThrow());
            newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
            newUser.setDoBackup(user.getDoBackup());
        }

        // Assert
        assertDoesNotThrow(() -> Config.loadAppConfigFromFile(configFile));
        assertEquals(clientId, config.getClientId());
        assertEquals(redirectURI, config.getRedirectURI());
        assertEquals(users, App.config.getUsers());
    }

    @Test
    void ensure_added_user_info_is_saved() throws IOException {
        // Arrange
        final String initialConfigContents = """
                {
                  "clientId": "abcdefg",
                  "redirectURI": "http://localhost:1234",
                  "clientSecret": "123",
                  "users": [
                    {
                      "spotifyId": "user1",
                      "displayName": "User 1",
                      "refreshToken": "q1w2e3r4t5",
                      "doBackup": false,
                      "cloneTargets": []
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "y6u7i8o9p0",
                      "doBackup": false,
                      "cloneTargets": []
                    }
                  ]
                }
                """;
        final String finalConfigContents = """
                {
                  "clientId": "abcdefg",
                  "redirectURI": "http://localhost:1234",
                  "clientSecret": "123",
                  "users": [
                    {
                      "spotifyId": "user1",
                      "displayName": "User 1",
                      "refreshToken": "q1w2e3r4t5",
                      "doBackup": false,
                      "cloneTargets": [
                        "user3"
                      ]
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "y6u7i8o9p0",
                      "doBackup": false,
                      "cloneTargets": []
                    },
                    {
                      "spotifyId": "user3",
                      "displayName": "User 3",
                      "refreshToken": "1a2b3c",
                      "doBackup": true,
                      "cloneTargets": []
                    }
                  ]
                }
                """;
        final Config.UserInfo newUser = Config.UserInfo.builder()
                .spotifyId("user3")
                .displayName("User 3")
                .refreshToken("1a2b3c")
                .doBackup(true)
                .build();
        Files.writeString(configFile.toPath(), initialConfigContents);
        Config.loadAppConfigFromFile(configFile);

        // Act
        {
            var emptyUser = App.config.addEmptyUser(false);
            emptyUser.setSpotifyId(newUser.getSpotifyId().orElseThrow());
            emptyUser.setDisplayName(newUser.getDisplayName().orElseThrow());
            emptyUser.setRefreshToken(newUser.getRefreshToken().orElseThrow());
            emptyUser.setDoBackup(newUser.getDoBackup());
            App.config.getUsers().getFirst().addCloneTarget(newUser);
        }

        // Assert
        final String newConfig = Files.readString(configFile.toPath());
        assertEquals(finalConfigContents, newConfig);
    }

    @Test
    void ensure_client_secret_can_be_cleared() throws IOException {
        // Arrange
        final String initialConfigContents = """
                {
                  "clientId": "abcdefg",
                  "redirectURI": "http://localhost:1234",
                  "clientSecret": "123",
                  "users": [
                    {
                      "spotifyId": "user1",
                      "displayName": "User 1",
                      "refreshToken": "q1w2e3r4t5",
                      "doBackup": true,
                      "cloneTargets": []
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "y6u7i8o9p0",
                      "doBackup": false,
                      "cloneTargets": []
                    }
                  ]
                }
                """;
        final String finalConfigContents = """
                {
                  "clientId": "abcdefg",
                  "redirectURI": "http://localhost:1234",
                  "users": [
                    {
                      "spotifyId": "user1",
                      "displayName": "User 1",
                      "refreshToken": "q1w2e3r4t5",
                      "doBackup": true,
                      "cloneTargets": []
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "y6u7i8o9p0",
                      "doBackup": false,
                      "cloneTargets": []
                    }
                  ]
                }
                """;
        Files.writeString(configFile.toPath(), initialConfigContents);
        Config.loadAppConfigFromFile(configFile);

        // Act
        App.config.clearClientSecret();

        // Assert
        final String newConfig = Files.readString(configFile.toPath());
        assertEquals(finalConfigContents, newConfig);
    }
}
