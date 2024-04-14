package spotifybackup.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    @ValueSource(strings = {
            /* no fields are missing here, would not throw an exception
            """
            {
              "clientId": "sdf77e",
              "redirectURI": "http://localhost:1234",
              "clientSecret": "123",
              "users": []
            }
            """,*/
            """
                    {
                      "clientId": "",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": []
                    }
                    """,
            """
                    {
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": []
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "redirectURI": "",
                      "clientSecret": "123",
                      "users": []
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "clientSecret": "123",
                      "users": []
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "",
                      "users": []
                    }
                    """,
            /* clientSecret is allowed to be missing, it is after all not essential
            """
            {
              "clientId": "sdf77e",
              "redirectURI": "http://localhost:1234",
              "users": []
            }
            """,*/
            /* missing "users" array will get filled in with an empty array
            """
            {
              "clientId": "sdf77e",
              "redirectURI": "http://localhost:1234",
              "clientSecret": "123"
            }
            """,*/
            /* no user fields are missing here, would not throw an exception
            """
            {
              "clientId": "sdf77e",
              "redirectURI": "http://localhost:1234",
              "clientSecret": "123",
              "users": [
                {
                  "spotifyId": "user1",
                  "displayName": "User 1",
                  "refreshToken": "token-1",
                  "doBackup": false
                }
              ]
            }
            """,*/
            """
                    {
                      "clientId": "~sdf77e",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": [
                        {
                          "spotifyId": "user1",
                          "displayName": "User 1",
                          "refreshToken": "token-1"
                        }
                      ]
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": [
                        {
                          "spotifyId": "",
                          "displayName": "User 1",
                          "refreshToken": "token-1",
                          "doBackup": false
                        }
                      ]
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": [
                        {
                          "displayName": "User 1",
                          "refreshToken": "token-1",
                          "doBackup": false
                        }
                      ]
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": [
                        {
                          "spotifyId": "user1",
                          "displayName": "",
                          "refreshToken": "token-1",
                          "doBackup": false
                        }
                      ]
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": [
                        {
                          "spotifyId": "user1",
                          "refreshToken": "token-1",
                          "doBackup": false
                        }
                      ]
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": [
                        {
                          "spotifyId": "user1",
                          "displayName": "User 1",
                          "refreshToken": "",
                          "doBackup": false
                        }
                      ]
                    }
                    """,
            """
                    {
                      "clientId": "sdf77e",
                      "redirectURI": "http://localhost:1234",
                      "clientSecret": "123",
                      "users": [
                        {
                          "spotifyId": "user1",
                          "displayName": "User 1",
                          "doBackup": false
                        }
                      ]
                    }
                    """,
    })
    void ensure_blank_or_missing_fields_are_rejected(final String configContents) throws IOException {
        // Arrange
        Files.writeString(configFile.toPath(), configContents);

        // Act & Assert
        assertThrows(BlankConfigFieldException.class, () -> Config.loadAppConfigFromFile(configFile));
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
                      "doBackup": false
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
                new Config.UserInfo(null, "user1", "User 1", "token-1", false),
                new Config.UserInfo(null, "user2", "User 2", "token-2", false)
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
                      "doBackup": true
                    }
                  ]
                }
                """;
        final String clientId = "abcdefg";
        final URI redirectURI = new URI("http://localhost:1234");
        final String clientSecret = "123";
        final List<Config.UserInfo> users = List.of(
                new Config.UserInfo(null, "user1", "User 1", "token-1", false),
                new Config.UserInfo(null, "user2", "User 2", "token-2", true)
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
                new Config.UserInfo(null, "user1", "User 1", "token-1", false),
                new Config.UserInfo(null, "user2", "User 2", "token-2", false)
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
                      "doBackup": false
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "y6u7i8o9p0",
                      "doBackup": false
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
                      "doBackup": false
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "y6u7i8o9p0",
                      "doBackup": false
                    },
                    {
                      "spotifyId": "user3",
                      "displayName": "User 3",
                      "refreshToken": "1a2b3c",
                      "doBackup": true
                    }
                  ]
                }
                """;
        final Config.UserInfo newUser = new Config.UserInfo(null, "user3", "User 3", "1a2b3c", true);
        Files.writeString(configFile.toPath(), initialConfigContents);
        Config.loadAppConfigFromFile(configFile);

        // Act
        {
            var emptyUser = App.config.addEmptyUser(false);
            emptyUser.setSpotifyId(newUser.getSpotifyId().orElseThrow());
            emptyUser.setDisplayName(newUser.getDisplayName().orElseThrow());
            emptyUser.setRefreshToken(newUser.getRefreshToken().orElseThrow());
            emptyUser.setDoBackup(newUser.getDoBackup());
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
                      "doBackup": true
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "y6u7i8o9p0",
                      "doBackup": false
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
                      "doBackup": true
                    },
                    {
                      "spotifyId": "user2",
                      "displayName": "User 2",
                      "refreshToken": "y6u7i8o9p0",
                      "doBackup": false
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
