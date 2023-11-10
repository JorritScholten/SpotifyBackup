package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyAlbumRepositoryTest {
    static final String albumDir = "src/test/java/spotifybackup/storage/spotify_api_get/album/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_album_can_be_persisted() throws IOException {
        // Arrange
        final Album apiAlbum = new Album.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(albumDir + "The_Heist.json")))
        );
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
}
