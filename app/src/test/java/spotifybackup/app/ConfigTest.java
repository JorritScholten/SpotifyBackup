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
import spotifybackup.app.exception.ConfigReferenceLoopException;
import spotifybackup.app.exception.ConfigUsersFieldException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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
    @CsvFileSource(files = "src/test/java/spotifybackup/app/blank_or_missing_fields_config_test.csv",
                   numLinesToSkip = 1, delimiter = '`')
    void ensure_blank_or_missing_fields_are_rejected(final boolean shouldThrow, final String message, final String json)
    throws IOException {
        // Arrange
        Files.writeString(configFile.toPath(), json.substring(2, json.length() - 1));

        // Act & Assert
        if (shouldThrow) {
            boolean noIssue = false;
            try {
                Config.loadAppConfigFromFile(configFile);
            } catch (BlankConfigFieldException | ConfigUsersFieldException e) {
                if (e.getMessage().split("\n").length == 2) noIssue = true;
                else throw new AssertionFailedError("Test case should only test one failure point in isolation, test " +
                                                            "case tests " + (e.getMessage().split("\n").length - 1) +
                                                            " failure points. Message: " + e.getMessage());
            }
            if (!noIssue) throw AssertionFailureBuilder.assertionFailure().message(message)
                                                       .reason(String.format(
                                                               "Expected %s to be thrown, but nothing was thrown.",
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
                               .doBackup(false)
                               .build());
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
                      "doBackup": false,
                      "cloneTargets": []
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
                               .doBackup(false)
                               .build(),
                Config.UserInfo.builder()
                               .spotifyId("user2")
                               .displayName("User 2")
                               .refreshToken("token-2")
                               .doBackup(true)
                               .build());

        // Act
        assertDoesNotThrow(() -> {
            App.config.setClientId(clientId);
            App.config.setRedirectURI(redirectURI);
            App.config.setClientSecret(clientSecret);
            for (var user : users) {
                var newUser = App.config.addEmptyUser(false);
                newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
                newUser.setDisplayName(user.getDisplayName().orElseThrow());
                newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
                newUser.setDoBackup(user.getDoBackup());
            }
            App.config.getUsers().getLast().addCloneTarget(App.config.getUsers().getFirst());
        });

        // Assert
        final String newConfig = Files.readString(configFile.toPath());
        assertEquals(configContents, newConfig);
    }

    @Test
    void create_new_config_file_and_load_values_into_it() throws URISyntaxException {
        // Arrange
        final String clientId = "some-client-id";
        final URI redirectURI = new URI("http://localhost:5678");
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                               .spotifyId("user1")
                               .displayName("User 1")
                               .refreshToken("token-1")
                               .doBackup(false)
                               .build(),
                Config.UserInfo.builder()
                               .spotifyId("user2")
                               .displayName("User 2")
                               .refreshToken("token-2")
                               .doBackup(false)
                               .build());

        // Act
        assertThrows(ConfigFileException.class, () -> Config.loadAppConfigFromFile(configFile));
        assertDoesNotThrow(() -> {
            App.config.setClientId(clientId);
            App.config.setRedirectURI(redirectURI);
            for (var user : users) {
                var newUser = App.config.addEmptyUser(false);
                newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
                newUser.setDisplayName(user.getDisplayName().orElseThrow());
                newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
                newUser.setDoBackup(user.getDoBackup());
            }
        });

        // Assert
        assertDoesNotThrow(() -> Config.loadAppConfigFromFile(configFile));
        assertEquals(clientId, App.config.getClientId());
        assertEquals(redirectURI, App.config.getRedirectURI());
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
                      "cloneTargets": []
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
                      "cloneTargets": [
                        "user1"
                      ]
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
        assertDoesNotThrow(() -> {
            var emptyUser = App.config.addEmptyUser(false);
            emptyUser.setSpotifyId(newUser.getSpotifyId().orElseThrow());
            emptyUser.setDisplayName(newUser.getDisplayName().orElseThrow());
            emptyUser.setRefreshToken(newUser.getRefreshToken().orElseThrow());
            emptyUser.setDoBackup(newUser.getDoBackup());
            emptyUser.addCloneTarget(App.config.getUsers().getFirst());
        });

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

    @Test
    void prevent_account_targeted_for_cloning_from_being_marked_for_backup() {
        // Arrange
        final String cloneTargetId = "clone-target";
        final String backupAccountId = "user1";
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                               .spotifyId(backupAccountId)
                               .displayName("User 1")
                               .refreshToken("token-1")
                               .doBackup(true)
                               .cloneTargets(List.of(cloneTargetId))
                               .build(),
                Config.UserInfo.builder()
                               .spotifyId("user2")
                               .displayName("User 2")
                               .refreshToken("token-2")
                               .doBackup(false)
                               .cloneTargets(Collections.emptyList())
                               .build(),
                Config.UserInfo.builder()
                               .spotifyId(cloneTargetId)
                               .displayName("Clone target")
                               .refreshToken("token-3")
                               .doBackup(false)
                               .cloneTargets(Collections.emptyList())
                               .build());
        assertDoesNotThrow(() -> {
            try {
                Config.loadAppConfigFromFile(configFile);
            } catch (ConfigFileException ignored) {
                App.config.setClientId("some-client-id");
                App.config.setRedirectURI(new URI("http://localhost:5678"));
                for (var user : users) {
                    var newUser = App.config.addEmptyUser(user.getDoBackup());
                    newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
                    newUser.setDisplayName(user.getDisplayName().orElseThrow());
                    newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
                }
            }
        });
        final var backupAccount = App.config.getUsers().getFirst();
        assertEquals(backupAccountId, backupAccount.getSpotifyId().orElseThrow(),
                     "First user should be the user1 account, this a sanity check for the assert phase.");
        final var cloneTarget = App.config.getUsers().getLast();
        assertEquals(cloneTargetId, cloneTarget.getSpotifyId().orElseThrow(),
                     "Last user should be the clone target, this a sanity check for the assert phase.");
        assertDoesNotThrow(() -> backupAccount.addCloneTarget(cloneTarget),
                           "UserInfo::addCloneTarget() should not throw anything here, starting from known good source.");

        // Act & Assert
        assertFalse(cloneTarget.getDoBackup());
        assertThrows(ConfigReferenceLoopException.class, () -> cloneTarget.setDoBackup(true));
    }

    @Test
    void prevent_account_marked_for_backup_with_clone_target_from_being_unmarked_for_backup() {
        // Arrange
        final String cloneTargetId = "clone-target";
        final String backupAccountId = "user1";
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                               .spotifyId(backupAccountId)
                               .displayName("User 1")
                               .refreshToken("token-1")
                               .doBackup(true)
                               .cloneTargets(List.of(cloneTargetId))
                               .build(),
                Config.UserInfo.builder()
                               .spotifyId(cloneTargetId)
                               .displayName("Clone target")
                               .refreshToken("token-3")
                               .doBackup(false)
                               .cloneTargets(Collections.emptyList())
                               .build());
        assertDoesNotThrow(() -> {
            try {
                Config.loadAppConfigFromFile(configFile);
            } catch (ConfigFileException ignored) {
                App.config.setClientId("some-client-id");
                App.config.setRedirectURI(new URI("http://localhost:5678"));
                for (var user : users) {
                    var newUser = App.config.addEmptyUser(user.getDoBackup());
                    newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
                    newUser.setDisplayName(user.getDisplayName().orElseThrow());
                    newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
                }
            }
        });
        final var backupAccount = App.config.getUsers().getFirst();
        assertEquals(backupAccountId, backupAccount.getSpotifyId().orElseThrow(),
                     "First user should be the user1 account, this a sanity check for the assert phase.");
        final var cloneTarget = App.config.getUsers().getLast();
        assertEquals(cloneTargetId, cloneTarget.getSpotifyId().orElseThrow(),
                     "Last user should be the clone target, this a sanity check for the assert phase.");
        assertDoesNotThrow(() -> backupAccount.addCloneTarget(cloneTarget),
                           "UserInfo::addCloneTarget() should not throw anything here, starting from known good source.");

        // Act & Assert
        assertTrue(backupAccount.getDoBackup());
        assertThrows(ConfigReferenceLoopException.class, () -> backupAccount.setDoBackup(false));
    }

    @Test
    void ensure_added_clone_target_is_persisted() throws IOException {
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
                      "doBackup": true,
                      "cloneTargets": [
                        "user2"
                      ]
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
        final var user1 = App.config.getUsers().getFirst();
        assertEquals("user1", user1.getSpotifyId().orElseThrow(),
                     "First user should be the user1 account, this a sanity check for the assert phase.");
        final var cloneTarget = App.config.getUsers().getLast();
        assertEquals("user2", cloneTarget.getSpotifyId().orElseThrow(),
                     "Last user should be the clone target, this a sanity check for the assert phase.");

        // Act
        assertDoesNotThrow(() -> {
            user1.setDoBackup(true);
            user1.addCloneTarget(cloneTarget);
        });

        // Assert
        assertTrue(user1.hasCloningTargets());
        final String newConfig = Files.readString(configFile.toPath());
        assertEquals(finalConfigContents, newConfig);
    }

    @Test
    void ensure_removed_clone_target_is_persisted() throws IOException {
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
                      "cloneTargets": [
                        "user2"
                      ]
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
        final var user1 = App.config.getUsers().getFirst();
        assertEquals("user1", user1.getSpotifyId().orElseThrow(),
                     "First user should be the user1 account, this a sanity check for the assert phase.");
        final var cloneTarget = App.config.getUsers().getLast();
        assertEquals("user2", cloneTarget.getSpotifyId().orElseThrow(),
                     "Last user should be the clone target, this a sanity check for the assert phase.");

        // Act
        user1.removeCloneTarget(cloneTarget);

        // Assert
        assertFalse(user1.hasCloningTargets());
        final String newConfig = Files.readString(configFile.toPath());
        assertEquals(finalConfigContents, newConfig);
    }

    // prevents self reference loop
    @Test
    void ensure_account_targeting_clones_is_marked_for_backup() {
        // Arrange
        final String cloneTargetId = "clone-target";
        final String backupAccountId = "user1";
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                               .spotifyId(backupAccountId)
                               .displayName("User 1")
                               .refreshToken("token-1")
                               .doBackup(false)
                               .build(),
                Config.UserInfo.builder()
                               .spotifyId(cloneTargetId)
                               .displayName("Clone target")
                               .refreshToken("token-3")
                               .doBackup(false)
                               .build());
        assertDoesNotThrow(() -> {
            try {
                Config.loadAppConfigFromFile(configFile);
            } catch (ConfigFileException ignored) {
                App.config.setClientId("some-client-id");
                App.config.setRedirectURI(new URI("http://localhost:5678"));
                for (var user : users) {
                    var newUser = App.config.addEmptyUser(user.getDoBackup());
                    newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
                    newUser.setDisplayName(user.getDisplayName().orElseThrow());
                    newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
                }
            }
        });
        final var backupAccount = App.config.getUsers().getFirst();
        assertEquals(backupAccountId, backupAccount.getSpotifyId().orElseThrow(),
                     "First user should be the user1 account, this a sanity check for the assert phase.");
        final var cloneTarget = App.config.getUsers().getLast();
        assertEquals(cloneTargetId, cloneTarget.getSpotifyId().orElseThrow(),
                     "Last user should be the clone target, this a sanity check for the assert phase.");

        // Act & Assert
        try {
            assertFalse(backupAccount.getDoBackup());
            backupAccount.addCloneTarget(cloneTarget);
        } catch (ConfigReferenceLoopException ignored) {
            assertDoesNotThrow(() -> {
                backupAccount.setDoBackup(true);
                backupAccount.addCloneTarget(cloneTarget);
            });
        }
    }

    // prevents self reference loop
    @Test
    void prevent_adding_clone_target_marked_for_backup() {
        // Arrange
        final String cloneTargetId = "clone-target";
        final String backupAccountId = "user1";
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                               .spotifyId(backupAccountId)
                               .displayName("User 1")
                               .refreshToken("token-1")
                               .doBackup(true)
                               .build(),
                Config.UserInfo.builder()
                               .spotifyId(cloneTargetId)
                               .displayName("Clone target")
                               .refreshToken("token-3")
                               .doBackup(true)
                               .build());
        assertDoesNotThrow(() -> {
            try {
                Config.loadAppConfigFromFile(configFile);
            } catch (ConfigFileException ignored) {
                App.config.setClientId("some-client-id");
                App.config.setRedirectURI(new URI("http://localhost:5678"));
                for (var user : users) {
                    var newUser = App.config.addEmptyUser(user.getDoBackup());
                    newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
                    newUser.setDisplayName(user.getDisplayName().orElseThrow());
                    newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
                }
            }
        });
        final var backupAccount = App.config.getUsers().getFirst();
        assertEquals(backupAccountId, backupAccount.getSpotifyId().orElseThrow(),
                     "First user should be the user1 account, this a sanity check for the assert phase.");
        final var cloneTarget = App.config.getUsers().getLast();
        assertEquals(cloneTargetId, cloneTarget.getSpotifyId().orElseThrow(),
                     "Last user should be the clone target, this a sanity check for the assert phase.");

        // Act & Assert
        assertTrue(cloneTarget.getDoBackup());
        assertFalse(backupAccount.hasCloningTargets());
        assertTrue(backupAccount.getDoBackup());
        assertThrows(ConfigReferenceLoopException.class, () -> backupAccount.addCloneTarget(cloneTarget));
    }

    // prevents self reference loop
    @Test
    void ensure_added_clone_target_is_not_self() {
        // Arrange
        final String notCloneTargetId = "not-clone-target";
        final String backupAccountId = "user1";
        final List<Config.UserInfo> users = List.of(
                Config.UserInfo.builder()
                               .spotifyId(backupAccountId)
                               .displayName("User 1")
                               .refreshToken("token-1")
                               .doBackup(true)
                               .build(),
                Config.UserInfo.builder()
                               .spotifyId(notCloneTargetId)
                               .displayName("not a Clone target")
                               .refreshToken("token-3")
                               .doBackup(false)
                               .build());
        assertDoesNotThrow(() -> {
            try {
                Config.loadAppConfigFromFile(configFile);
            } catch (ConfigFileException ignored) {
                App.config.setClientId("some-client-id");
                App.config.setRedirectURI(new URI("http://localhost:5678"));
                for (var user : users) {
                    var newUser = App.config.addEmptyUser(user.getDoBackup());
                    newUser.setSpotifyId(user.getSpotifyId().orElseThrow());
                    newUser.setDisplayName(user.getDisplayName().orElseThrow());
                    newUser.setRefreshToken(user.getRefreshToken().orElseThrow());
                }
            }
        });
        final var backupAccount = App.config.getUsers().getFirst();
        assertEquals(backupAccountId, backupAccount.getSpotifyId().orElseThrow(),
                     "First user should be the user1 account, this a sanity check for the assert phase.");
        final var backupAccount2 = App.config.getUsers().getLast();
        assertEquals(notCloneTargetId, backupAccount2.getSpotifyId().orElseThrow(),
                     "Last user should be the not a clone target, this a sanity check for the assert phase.");

        // Act & Assert
        assertTrue(backupAccount.getDoBackup());
        assertThrows(ConfigReferenceLoopException.class, () -> backupAccount.addCloneTarget(backupAccount));
        assertFalse(backupAccount2.getDoBackup());
        assertThrows(ConfigReferenceLoopException.class, () -> backupAccount2.addCloneTarget(backupAccount2));
    }
}
