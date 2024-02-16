package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyUserRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String playlistDir = testDataDir + "playlist/";
    static final String userDir = testDataDir + "user/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_user_can_be_persisted() throws IOException {
        // Arrange
        final User apiUser = new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user2.json")))
        );
        assertTrue(spotifyObjectRepository.find(apiUser.getId()).isEmpty());

        // Act
        final var user = spotifyObjectRepository.persist(apiUser);

        // Assert
        assertTrue(spotifyObjectRepository.find(apiUser.getId()).isPresent());
        assertEquals(user.getSpotifyUserID(), apiUser.getId());
    }

    @Test
    void find_account_holder_user_account() throws IOException {
        // Arrange
        // account holder
        final User apiUser = new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user.json")))
        );
        assertFalse(spotifyObjectRepository.getAccountHolders()
                .stream().anyMatch(u -> u.getSpotifyUserID().equals(apiUser.getId())));
        final var user = spotifyObjectRepository.persist(apiUser);
        // source of non account holder users
        final PlaylistSimplified[] playlists = {
                new PlaylistSimplified.JsonUtil().createModelObject(
                        new String(Files.readAllBytes(Path.of(playlistDir + "Spotify_Web_API_Testing_playlist.json")))),
                new PlaylistSimplified.JsonUtil().createModelObject(
                        new String(Files.readAllBytes(Path.of(playlistDir + "The_Blue_Stones.json")))
                )
        };
        spotifyObjectRepository.persist(playlists[0]);
        spotifyObjectRepository.persist(playlists[1]);

        // Act
        final var accountHolders = spotifyObjectRepository.getAccountHolders();

        // Assert
        assertTrue(accountHolders.stream().anyMatch(u -> u.getSpotifyUserID().equals(user.getSpotifyUserID())));
        assertTrue(accountHolders.stream().anyMatch(u -> u.getId() == user.getId()));
    }
}
