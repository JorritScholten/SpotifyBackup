package spotifybackup.storage;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.SavedAlbum;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpotifySavedAlbumRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String userDir = testDataDir + "user/";
    static final String libraryDir = testDataDir + "library/";
    static final String albumDir = testDataDir + "album/";
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
    void ensure_user_with_no_saved_albums_returns_empty() {
        // Arrange
        final var user = getUserFromId.apply("testaccount");

        // Assert
        assertTrue(spotifyObjectRepository.getNewestSavedAlbum(user).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"testaccount", "testaccount2"})
    @Order(2)
    void ensure_users_saved_albums_can_be_persisted(String userId) throws IOException {
        fail("implement ImageSelection limit to persist methods");
        // Arrange
        final var user = getUserFromId.apply(userId);
        final SavedAlbum[] apiSavedAlbums = new SavedAlbum.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_albums.json"))), "items"
        );
        assertEquals(0, spotifyObjectRepository.countSavedAlbums(user));

        // Act
        spotifyObjectRepository.persist(apiSavedAlbums, user);

        // Assert
        assertEquals(apiSavedAlbums.length, spotifyObjectRepository.countSavedAlbums(user));
    }

    @ParameterizedTest
    @ValueSource(strings = {"testaccount", "testaccount2"})
    @Order(3)
        // this test needs to be done after ensure_users_saved_albums_can_be_persisted()
    void ensure_most_recent_saved_album_already_stored_can_be_retrieved(String userId) throws IOException {
        // Arrange
        final var user = getUserFromId.apply(userId);
        final SavedAlbum[] apiSavedAlbums = new SavedAlbum.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_albums.json"))), "items"
        );
        final SavedAlbum mostRecentlyAddedApi = Arrays.stream(apiSavedAlbums)
                .max(Comparator.comparing(SavedAlbum::getAddedAt))
                .orElseThrow(NoSuchElementException::new);

        // Act
        final var mostRecentlyAdded = spotifyObjectRepository.getNewestSavedAlbum(user).orElseThrow();

        // Assert
        assertEquals(mostRecentlyAddedApi.getAlbum().getId(), mostRecentlyAdded.getAlbum().getSpotifyID().getId());
    }

    @Test
    @Order(4)
    void retrieve_saved_album_ids() throws IOException {
        // Arrange
        final var user = getUserFromId.apply("testaccount");
        final SavedAlbum[] apiSavedAlbums = new SavedAlbum.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_albums.json"))), "items"
        );
        final List<String> apiSavedAlbumIds = Arrays.stream(apiSavedAlbums)
                .map(savedAlbum -> savedAlbum.getAlbum().getId()).toList();

        // Act
        final var savedAlbumIds = spotifyObjectRepository.getSavedAlbumIds(user);

        // Assert
        assertTrue(savedAlbumIds.containsAll(apiSavedAlbumIds));
    }

    @Test
    @Order(5)
    void retrieve_saved_albums() throws IOException {
        // Arrange
        final var user = getUserFromId.apply("testaccount");
        final SavedAlbum[] apiSavedAlbums = new SavedAlbum.JsonUtil().createModelObjectArray(
                new String(Files.readAllBytes(Path.of(libraryDir + "testaccount_saved_albums.json"))), "items"
        );
        final List<String> apiSavedAlbumIds = Arrays.stream(apiSavedAlbums)
                .map(savedAlbum -> savedAlbum.getAlbum().getId()).toList();

        // Act
        final var savedAlbums = spotifyObjectRepository.getSavedAlbums(user);

        // Assert
        final var savedAlbumIds = savedAlbums.stream().map(t -> t.getAlbum().getSpotifyID().getId()).toList();
        assertTrue(savedAlbumIds.containsAll(apiSavedAlbumIds));
    }

    @ParameterizedTest
    @ValueSource(strings = {"testaccount", "testaccount2"})
    @Order(6)
    void ensure_most_recent_saved_album_can_be_retrieved(String userId) throws IOException {
        // Arrange
        final var user = getUserFromId.apply(userId);
        final var previousMostRecentlyAdded = spotifyObjectRepository.getNewestSavedAlbum(user).orElseThrow();
        final SavedAlbum[] apiSavedAlbum = {new SavedAlbum.Builder()
                .setAddedAt(Date.from(previousMostRecentlyAdded.getDateAdded().plusYears(1).toInstant()))
                .setAlbum(new Album.JsonUtil().createModelObject(
                        new String(Files.readAllBytes(Path.of(albumDir + "The_Heist.json")))
                )).build()};
        spotifyObjectRepository.persist(apiSavedAlbum, user);

        // Act
        final var newMostRecentlyAdded = spotifyObjectRepository.getNewestSavedAlbum(user).orElseThrow();

        // Assert
        assertNotEquals(previousMostRecentlyAdded.getAlbum().getSpotifyID(),
                newMostRecentlyAdded.getAlbum().getSpotifyID());
        assertTrue(newMostRecentlyAdded.getDateAdded().isAfter(previousMostRecentlyAdded.getDateAdded()));
    }

    @Test
    @Order(7)
    void ensure_saved_album_can_be_removed() throws IOException {
        // Arrange
        final var user = getUserFromId.apply("testaccount");
        final var oldSavedAlbumCount = spotifyObjectRepository.countSavedAlbums(user);
        final var mostRecentlyAdded = spotifyObjectRepository.getNewestSavedAlbum(user).orElseThrow();
        final var mostRecentAlbumId = mostRecentlyAdded.getAlbum().getSpotifyID().getId();

        // Act
        final var removedAlbum = spotifyObjectRepository.removeSavedAlbum(mostRecentlyAdded.getAlbum(), user).orElseThrow();
        final var removedAlbums = spotifyObjectRepository.getRemovedSavedAlbums(user);

        // Assert
        assertEquals(oldSavedAlbumCount - 1, spotifyObjectRepository.countSavedAlbums(user));
        assertFalse(spotifyObjectRepository.getSavedAlbumIds(user).contains(mostRecentAlbumId));
        assertEquals(mostRecentAlbumId, removedAlbum.getAlbum().getSpotifyID().getId());
        assertTrue(removedAlbums.stream()
                .map(t -> t.getAlbum().getSpotifyID().getId())
                .anyMatch(s -> s.equals(mostRecentAlbumId))
        );
    }

    @Test
    @Order(8)
    void ensure_removed_saved_album_can_be_added_again() throws IOException {
        // Arrange
        final var user = getUserFromId.apply("testaccount");
        final var previousMostRecentlyAdded = spotifyObjectRepository.getNewestSavedAlbum(user).orElseThrow();
        final var originallyAdded = previousMostRecentlyAdded.getDateAdded().plusYears(1);
        final SavedAlbum[] apiSavedAlbum = {new SavedAlbum.Builder()
                .setAddedAt(Date.from(originallyAdded.plusDays(2).toInstant()))
                .setAlbum(new Album.JsonUtil().createModelObject(
                        new String(Files.readAllBytes(Path.of(albumDir + "The_Heist.json")))
                )).build()};
        final var oldSavedAlbumCount = spotifyObjectRepository.countSavedAlbums(user);
        assertEquals(1, spotifyObjectRepository.getRemovedSavedAlbums(user).size());
        assertTrue(spotifyObjectRepository.getRemovedSavedAlbums(user).stream()
                        .map(t -> t.getAlbum().getSpotifyID().getId())
                        .anyMatch(s -> s.equals(apiSavedAlbum[0].getAlbum().getId())),
                apiSavedAlbum[0].getAlbum().getName() + " should be in removed saved songs for user: "
                        + user.getSpotifyUserID()
        );

        // Act
        final var savedAlbums = spotifyObjectRepository.persist(apiSavedAlbum, user);
        final var newMostRecentlyAdded = spotifyObjectRepository.getNewestSavedAlbum(user).orElseThrow();

        // Assert
        assertEquals(oldSavedAlbumCount + 1, spotifyObjectRepository.countSavedAlbums(user));
        assertEquals(0, spotifyObjectRepository.getRemovedSavedAlbums(user).size());
        assertFalse(newMostRecentlyAdded.getIsRemoved());
        assertFalse(savedAlbums.getFirst().getIsRemoved());
        assertEquals(apiSavedAlbum[0].getAlbum().getId(), savedAlbums.getFirst().getAlbum().getSpotifyID().getId());
        assertNotEquals(originallyAdded, newMostRecentlyAdded.getDateAdded());
    }
}
