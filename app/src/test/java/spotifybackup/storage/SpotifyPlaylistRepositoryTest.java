package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyPlaylistRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String playlistDir = testDataDir + "playlist/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
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

    @Test
    void ensure_simplified_playlist_can_be_filled_in_with_unsimplified() throws IOException {
        // Arrange
        final String playlistJson = new String(Files.readAllBytes(Path.of(playlistDir + "The_Blue_Stones.json")));
        final PlaylistSimplified apiPlaylistSimple = new PlaylistSimplified.JsonUtil().createModelObject(playlistJson);
        final Playlist apiPlaylist = new Playlist.JsonUtil().createModelObject(playlistJson);
        final var playlistSimple = spotifyObjectRepository.persist(apiPlaylistSimple);
        assertTrue(spotifyObjectRepository.exists(playlistSimple));
        assertTrue(playlistSimple.getIsSimplified());
        assertNotEquals(0, apiPlaylist.getTracks().getTotal());
        assertEquals(0, playlistSimple.getTracks().size());

        // Act
        final var playlist = spotifyObjectRepository.persist(apiPlaylist);

        // Assert
        assertEquals(playlistSimple.getId(), playlist.getId());
        assertEquals(playlistSimple.getSpotifyID(), playlist.getSpotifyID());
        assertFalse(playlist.getIsSimplified());
        assertNotEquals(0, playlist.getTracks().size());
        assertEquals(apiPlaylist.getTracks().getTotal(), playlist.getTracks().size());
    }

    @Test
    void ensure_simplified_playlist_can_be_retrieved() throws IOException {
        // Arrange
        final String playlistJson = new String(Files.readAllBytes(Path.of(playlistDir + "This_Is_Yatao.json")));
        final PlaylistSimplified apiPlaylistSimple = new PlaylistSimplified.JsonUtil().createModelObject(playlistJson);
        final Playlist apiPlaylist = new Playlist.JsonUtil().createModelObject(playlistJson);
        assertFalse(spotifyObjectRepository.exists(apiPlaylistSimple));
        final var playlistSimple = spotifyObjectRepository.persist(apiPlaylistSimple);
        assertTrue(playlistSimple.getIsSimplified());

        // Act
        final var oldSimplePlaylistIds = spotifyObjectRepository.getSimplifiedPlaylistsSpotifyIDs();

        // Assert
        assertTrue(oldSimplePlaylistIds.contains(apiPlaylistSimple.getId()));

        // Act 2
        spotifyObjectRepository.persist(apiPlaylist);
        final var newSimplePlaylistIds = spotifyObjectRepository.getSimplifiedAlbumsSpotifyIDs();

        // Assert 2
        assertFalse(newSimplePlaylistIds.contains(apiPlaylistSimple.getId()));
    }
}
