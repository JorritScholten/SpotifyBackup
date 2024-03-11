package spotifybackup.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyAlbumRepositoryTest {
    static final String albumDir = "src/test/java/spotifybackup/storage/spotify_api_get/album/";
    private SpotifyObjectRepository spotifyObjectRepository;

    static Album loadFromPath(String fileName) throws IOException {
        return new Album.JsonUtil().createModelObject(new String(Files.readAllBytes(Path.of(albumDir + fileName))));
    }

    @BeforeEach
    void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_album_can_be_persisted() throws IOException {
        // Arrange
        final Album apiAlbum = loadFromPath("The_Heist.json");
        assertFalse(spotifyObjectRepository.exists(apiAlbum),
                "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertTrue(apiAlbum.getGenres().length > 0, "Album should have 1 or more genres.");

        // Act
        var persistedAlbum = spotifyObjectRepository.persist(apiAlbum);

        // Assert
        assertTrue(spotifyObjectRepository.exists(apiAlbum.getId(), SpotifyID.class),
                "Can't find Album by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiAlbum), "Can't find Album by apiAlbum/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedAlbum), "Can't find Album by Object reference.");
        assertTrue(apiAlbum.getArtists().length > 0);
        assertEquals(apiAlbum.getArtists().length, persistedAlbum.getArtists().size());
        assertTrue(apiAlbum.getGenres().length > 0);
        assertEquals(apiAlbum.getGenres().length, persistedAlbum.getGenres().size());
        assertTrue(apiAlbum.getImages().length > 0);
        assertEquals(apiAlbum.getImages().length, persistedAlbum.getImages().size());
        assertEquals(apiAlbum.getTracks().getTotal(), persistedAlbum.getTracks().size());
    }

    @Test
    void ensure_singles_album_can_be_persisted() throws IOException {
        // Arrange
        final Album apiAlbum = loadFromPath("Embers_Rise.json");
        assertFalse(spotifyObjectRepository.exists(apiAlbum),
                "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertTrue(apiAlbum.getArtists().length > 0, "Album should have 1 or more artists.");

        // Act
        var persistedAlbum = spotifyObjectRepository.persist(apiAlbum);

        // Assert
        assertTrue(spotifyObjectRepository.exists(apiAlbum.getId(), SpotifyID.class),
                "Can't find Album by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiAlbum), "Can't find Album by apiAlbum/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedAlbum), "Can't find Album by Object reference.");
        assertTrue(apiAlbum.getArtists().length > 0);
        assertEquals(apiAlbum.getArtists().length, persistedAlbum.getArtists().size());
        assertTrue(apiAlbum.getImages().length > 0);
        assertEquals(apiAlbum.getImages().length, persistedAlbum.getImages().size());
        assertEquals(apiAlbum.getTracks().getTotal(), persistedAlbum.getTracks().size());
    }

    @Test
    void ensure_simplified_album_can_be_filled_in_with_unsimplified() throws IOException {
        // Arrange
        final String albumJson = new String(Files.readAllBytes(Path.of(albumDir + "Ecliptica_(International_Version).json")));
        final AlbumSimplified apiAlbumSimple = new AlbumSimplified.JsonUtil().createModelObject(albumJson);
        final Album apiAlbum = new Album.JsonUtil().createModelObject(albumJson);
        final var albumSimple = spotifyObjectRepository.persist(apiAlbumSimple);
        assertTrue(spotifyObjectRepository.exists(albumSimple));
        assertTrue(albumSimple.getIsSimplified());
        assertNotEquals(0, apiAlbum.getTracks().getTotal());
        assertEquals(0, albumSimple.getTracks().size());

        // Act
        final var album = spotifyObjectRepository.persist(apiAlbum);

        // Assert
        assertEquals(albumSimple.getId(), album.getId());
        assertEquals(albumSimple.getSpotifyID(), album.getSpotifyID());
        assertFalse(album.getIsSimplified());
        assertNotEquals(0, album.getTracks().size());
        assertEquals(apiAlbum.getTracks().getTotal(), album.getTracks().size());
    }

    @Test
    void ensure_simplified_album_can_be_retrieved() throws IOException {
        // Arrange
        final String albumJson = new String(Files.readAllBytes(Path.of(albumDir + "The_Trick_To_Life.json")));
        final AlbumSimplified apiAlbumSimple = new AlbumSimplified.JsonUtil().createModelObject(albumJson);
        final Album apiAlbum = new Album.JsonUtil().createModelObject(albumJson);
        assertFalse(spotifyObjectRepository.exists(apiAlbumSimple));
        final var albumSimple = spotifyObjectRepository.persist(apiAlbumSimple);
        assertTrue(albumSimple.getIsSimplified());

        // Act
        final var oldSimpleAlbumIds = spotifyObjectRepository.getSimplifiedAlbumsSpotifyIDs();

        // Assert
        assertTrue(oldSimpleAlbumIds.contains(apiAlbumSimple.getId()));

        // Act 2
        spotifyObjectRepository.persist(apiAlbum);
        final var newSimpleAlbumIds = spotifyObjectRepository.getSimplifiedAlbumsSpotifyIDs();

        // Assert 2
        assertFalse(newSimpleAlbumIds.contains(apiAlbumSimple.getId()));
    }

    @Test
    void ensure_album_array_can_be_persisted() throws IOException {
        // Arrange
        final Album[] apiAlbums = {
                loadFromPath("The_Heist.json"),
                loadFromPath("The_Trick_To_Life.json"),
                loadFromPath("King.json")
        };
        for (var apiAlbum : apiAlbums)
            assertFalse(spotifyObjectRepository.exists(apiAlbum),
                    "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");

        // Act
        var persistedAlbums = spotifyObjectRepository.persist(apiAlbums);

        // Assert
        for (var persistedAlbum : persistedAlbums) {
            final var apiAlbum = Arrays.stream(apiAlbums)
                    .filter(a -> a.getId().equals(persistedAlbum.getSpotifyID().getId()))
                    .findFirst().orElseThrow();
            assertTrue(spotifyObjectRepository.exists(apiAlbum.getId(), SpotifyID.class),
                    "Can't find Album by Spotify ID.");
            assertTrue(spotifyObjectRepository.exists(apiAlbum), "Can't find Album by apiAlbum/Spotify ID.");
            assertTrue(spotifyObjectRepository.exists(persistedAlbum), "Can't find Album by Object reference.");
            assertTrue(apiAlbum.getArtists().length > 0);
            assertEquals(apiAlbum.getArtists().length, persistedAlbum.getArtists().size());
            assertTrue(apiAlbum.getImages().length > 0);
            assertEquals(apiAlbum.getImages().length, persistedAlbum.getImages().size());
            assertFalse(persistedAlbum.getTracks().isEmpty());
            assertEquals(apiAlbum.getTracks().getTotal(), persistedAlbum.getTracks().size());
        }
    }

    @Test
    void ensure_album_array_can_be_persisted_shallowly() throws IOException {
        // Arrange
        final Album[] apiAlbums = {
                loadFromPath("The_Heist.json"),
                loadFromPath("The_Trick_To_Life.json"),
                loadFromPath("King.json")
        };
        for (var apiAlbum : apiAlbums)
            assertFalse(spotifyObjectRepository.exists(apiAlbum),
                    "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertEquals(0, spotifyObjectRepository.count(SpotifyObject.SubTypes.TRACK));

        // Act
        var persistedAlbums = spotifyObjectRepository.persistWithoutTracks(apiAlbums);

        // Assert
        assertEquals(apiAlbums.length, spotifyObjectRepository.count(SpotifyObject.SubTypes.ALBUM));
        assertEquals(0, spotifyObjectRepository.count(SpotifyObject.SubTypes.TRACK));
        for (var album : persistedAlbums) {
            assertTrue(album.getTracks().isEmpty());
        }
    }
}
