package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void find_account_holder_user_account() throws IOException {
        // Arrange
        // account holder
        final User apiUser = new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user.json")))
        );
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
        final var accountHolder = spotifyObjectRepository.getAccountHolder();

        // Assert
        assertTrue(accountHolder.isPresent());
        assertEquals(user.getId(), accountHolder.get().getId());
        assertEquals(user.getSpotifyUserID(), accountHolder.get().getSpotifyUserID());
    }
}
