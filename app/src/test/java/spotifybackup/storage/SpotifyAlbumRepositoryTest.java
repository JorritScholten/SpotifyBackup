package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Album;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyAlbumRepositoryTest {
    static final String albumDir = "src/test/java/spotifybackup/storage/spotify_api_get/album/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        try {
            spotifyObjectRepository = SpotifyObjectRepository.testFactory(true);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_album_can_be_persisted() throws IOException {
        // Arrange
        final Album apiAlbum = new Album.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(albumDir + "The_Heist.json")))
        );
        assertFalse(spotifyObjectRepository.albumExists(apiAlbum),
                "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertTrue(apiAlbum.getGenres().length > 0, "Album should have 1 or more genres.");

        // Act
        var persistedAlbum = spotifyObjectRepository.persistAlbum(apiAlbum);

        // Assert
        assertTrue(spotifyObjectRepository.spotifyIDExists(apiAlbum.getId()), "Can't find Album by Spotify ID.");
        assertTrue(spotifyObjectRepository.albumExists(apiAlbum), "Can't find Album by apiAlbum/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedAlbum), "Can't find Album by Object reference.");
        assertTrue(apiAlbum.getArtists().length > 0);
        assertEquals(apiAlbum.getArtists().length, persistedAlbum.getSpotifyArtists().size());
        assertTrue(apiAlbum.getGenres().length > 0);
        assertEquals(apiAlbum.getGenres().length, persistedAlbum.getSpotifyGenres().size());
        assertTrue(apiAlbum.getImages().length > 0);
        assertEquals(apiAlbum.getImages().length, persistedAlbum.getSpotifyImages().size());
        assertEquals(apiAlbum.getTracks().getTotal(), persistedAlbum.getSpotifyTracks().size());
    }
}
