package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Playlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpotifyPlaylistRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String playlistDir = testDataDir + "playlist/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        try {
            spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_playlist_can_be_persisted() throws IOException {
        // Arrange
        final Playlist apiPlaylist = new Playlist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(playlistDir + "Spotify_Web_API_Testing_playlist.json")))
        );
        assertFalse(spotifyObjectRepository.exists(apiPlaylist),
                "Playlist with Spotify ID " + apiPlaylist.getId() + " shouldn't already exist.");

        // Act
        var persistedPlaylist = spotifyObjectRepository.persist(apiPlaylist);

        // Assert
        assertTrue(spotifyObjectRepository.exists(apiPlaylist.getId(), SpotifyID.class),
                "Can't find SpotifyPlaylist by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiPlaylist), "Can't find SpotifyPlaylist by apiPlaylist/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedPlaylist), "Can't find SpotifyPlaylist by Object reference.");
        assertEquals(apiPlaylist.getId(), persistedPlaylist.getSpotifyID().getId());
    }
}
