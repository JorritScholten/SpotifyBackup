package spotifybackup.storage;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpotifySavedTrackRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String userDir = testDataDir + "user/";
    static final String libraryDir = testDataDir + "library/";
    static final String trackDir = testDataDir + "track/";
    static private SpotifyObjectRepository spotifyObjectRepository;
    static private SpotifyUser user;

    @BeforeAll
    static void setup() throws IOException {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
        user = spotifyObjectRepository.persist(new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user.json")))
        ));
    }

    @Test
    @Order(1)
    void ensure_user_with_no_saved_tracks_returns_empty() {
        assertTrue(spotifyObjectRepository.getNewestSavedTrack(user).isEmpty());
    }

    @Test
    @Order(2)
    void ensure_users_saved_tracks_can_be_persisted() throws IOException {
        // Arrange
        final SavedTrack[] apiSavedTracks = new SavedTrack.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_tracks.json"))), "items"
        );
        assertEquals(0, spotifyObjectRepository.countSavedTracks(user));

        // Act
        spotifyObjectRepository.persist(apiSavedTracks, user);

        // Assert
        assertEquals(apiSavedTracks.length, spotifyObjectRepository.countSavedTracks(user));
    }

    @Test
    @Order(3)
        // this test needs to be done after ensure_users_saved_tracks_can_be_persisted()
    void ensure_most_recent_saved_track_already_stored_can_be_retrieved() throws IOException {
        // Arrange
        final SavedTrack[] apiSavedTracks = new SavedTrack.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_tracks.json"))), "items"
        );
        final SavedTrack mostRecentlyAddedApi = Arrays.stream(apiSavedTracks)
                .max(Comparator.comparing(SavedTrack::getAddedAt))
                .orElseThrow(NoSuchElementException::new);

        // Act
        final var mostRecentlyAdded = spotifyObjectRepository.getNewestSavedTrack(user).orElseThrow();

        // Assert
        assertEquals(mostRecentlyAddedApi.getTrack().getId(), mostRecentlyAdded.getTrack().getSpotifyID().getId());
    }

    @Test
    @Order(4)
    void ensure_most_recent_saved_track_can_be_retrieved() throws IOException {
        // Arrange
        final var previousMostRecentlyAdded = spotifyObjectRepository.getNewestSavedTrack(user).orElseThrow();
        final SavedTrack[] apiSavedTrack = {new SavedTrack.Builder()
                .setAddedAt(Date.from(previousMostRecentlyAdded.getDateAdded().plusYears(1).toInstant()))
                .setTrack(new Track.JsonUtil().createModelObject(
                        new String(Files.readAllBytes(Path.of(trackDir + "King.json")))
                )).build()};
        spotifyObjectRepository.persist(apiSavedTrack, user);

        // Act
        final var newMostRecentlyAdded = spotifyObjectRepository.getNewestSavedTrack(user).orElseThrow();

        // Assert
        assertNotEquals(previousMostRecentlyAdded.getTrack().getSpotifyID(),
                newMostRecentlyAdded.getTrack().getSpotifyID());
        assertTrue(newMostRecentlyAdded.getDateAdded().isAfter(previousMostRecentlyAdded.getDateAdded()));
    }
}
