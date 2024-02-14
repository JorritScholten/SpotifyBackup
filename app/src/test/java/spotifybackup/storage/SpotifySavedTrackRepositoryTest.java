package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifySavedTrackRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String userDir = testDataDir + "user/";
    static final String libraryDir = testDataDir + "library/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_users_saved_tracks_can_be_persisted() throws IOException {
        // Arrange
        final User apiUser = new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user.json")))
        );
        final var user = spotifyObjectRepository.persist(apiUser);
        final SavedTrack[] apiSavedTracks = new SavedTrack.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_tracks.json"))), "items"
        );
        assertEquals(0, spotifyObjectRepository.countSavedTracks(user));

        // Act
        spotifyObjectRepository.persist(apiSavedTracks, user);

        // Assert
        assertEquals(apiSavedTracks.length, spotifyObjectRepository.countSavedTracks(user));
    }
}
