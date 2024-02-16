package spotifybackup.storage;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpotifySavedTrackRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String userDir = testDataDir + "user/";
    static final String libraryDir = testDataDir + "library/";
    static final String trackDir = testDataDir + "track/";
    static private SpotifyObjectRepository spotifyObjectRepository;
    static private final Function<String, SpotifyUser> getUserFromId =
            id -> (SpotifyUser) spotifyObjectRepository.find(id).orElseThrow();

    @BeforeAll
    static void setup() throws IOException {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
        spotifyObjectRepository.persist(new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user.json")))
        ));
        spotifyObjectRepository.persist(new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user2.json")))
        ));
    }

    @Test
    @Order(1)
    void ensure_user_with_no_saved_tracks_returns_empty() {
        // Arrange
        final var user = getUserFromId.apply("testaccount");

        // Assert
        assertTrue(spotifyObjectRepository.getNewestSavedTrack(user).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"testaccount", "testaccount2"})
    @Order(2)
    void ensure_users_saved_tracks_can_be_persisted(String userId) throws IOException {
        // Arrange
        final var user = getUserFromId.apply(userId);
        final SavedTrack[] apiSavedTracks = new SavedTrack.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_tracks.json"))), "items"
        );
        assertEquals(0, spotifyObjectRepository.countSavedTracks(user));

        // Act
        spotifyObjectRepository.persist(apiSavedTracks, user);

        // Assert
        assertEquals(apiSavedTracks.length, spotifyObjectRepository.countSavedTracks(user));
    }

    @ParameterizedTest
    @ValueSource(strings = {"testaccount", "testaccount2"})
    @Order(3)
        // this test needs to be done after ensure_users_saved_tracks_can_be_persisted()
    void ensure_most_recent_saved_track_already_stored_can_be_retrieved(String userId) throws IOException {
        // Arrange
        final var user = getUserFromId.apply(userId);
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
    void retrieve_saved_track_ids() throws IOException {
        // Arrange
        final var user = getUserFromId.apply("testaccount");
        final SavedTrack[] apiSavedTracks = new SavedTrack.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_tracks.json"))), "items"
        );
        final List<String> apiSavedTrackIds = Arrays.stream(apiSavedTracks)
                .map(savedTrack -> savedTrack.getTrack().getId()).toList();

        // Act
        final var savedTrackIds = spotifyObjectRepository.getSavedTrackIds(user);

        // Assert
        assertTrue(savedTrackIds.containsAll(apiSavedTrackIds));
    }

    @Test
    @Order(5)
    void retrieve_saved_tracks() throws IOException {
        // Arrange
        final var user = getUserFromId.apply("testaccount");
        final SavedTrack[] apiSavedTracks = new SavedTrack.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_tracks.json"))), "items"
        );
        final List<String> apiSavedTrackIds = Arrays.stream(apiSavedTracks)
                .map(savedTrack -> savedTrack.getTrack().getId()).toList();

        // Act
        final var savedTracks = spotifyObjectRepository.getSavedTracks(user);

        // Assert
        final var savedTrackIds = savedTracks.stream().map(t -> t.getTrack().getSpotifyID().getId()).toList();
        assertTrue(savedTrackIds.containsAll(apiSavedTrackIds));
    }

    @ParameterizedTest
    @ValueSource(strings = {"testaccount", "testaccount2"})
    @Order(6)
    void ensure_most_recent_saved_track_can_be_retrieved(String userId) throws IOException {
        // Arrange
        final var user = getUserFromId.apply(userId);
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
