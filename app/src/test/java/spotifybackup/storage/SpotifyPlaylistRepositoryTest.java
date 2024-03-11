package spotifybackup.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyPlaylistRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String playlistDir = testDataDir + "playlist/";
    private SpotifyObjectRepository spotifyObjectRepository;

    static String loadFromPath(String fileName) throws IOException {
        return new String(Files.readAllBytes(Path.of(playlistDir + fileName)));
    }

    @BeforeEach
    void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_playlist_can_be_persisted() throws IOException {
        // Arrange
        final Playlist apiPlaylist = new Playlist.JsonUtil().createModelObject(
                loadFromPath("Spotify_Web_API_Testing_playlist.json")
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
        final String playlistJson = loadFromPath("The_Blue_Stones.json");
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
        final String playlistJson = loadFromPath("This_Is_Yatao.json");
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

    @Test
    void ensure_all_playlists_can_be_retrieved() throws IOException {
        // Arrange
        final PlaylistSimplified apiPlaylist1 = new PlaylistSimplified.JsonUtil().createModelObject(
                loadFromPath("This_Is_Yatao.json"));
        assertFalse(spotifyObjectRepository.exists(apiPlaylist1));
        spotifyObjectRepository.persist(apiPlaylist1);
        final Playlist apiPlaylist2 = new Playlist.JsonUtil().createModelObject(
                loadFromPath("The_Blue_Stones.json"));
        assertFalse(spotifyObjectRepository.exists(apiPlaylist2));
        spotifyObjectRepository.persist(apiPlaylist2);
        final Playlist apiPlaylist3 = new Playlist.JsonUtil().createModelObject(
                loadFromPath("Spotify_Web_API_Testing_playlist.json"));
        assertFalse(spotifyObjectRepository.exists(apiPlaylist3));
        spotifyObjectRepository.persist(apiPlaylist3);


        // Act
        final List<SpotifyPlaylist> playlists = spotifyObjectRepository.findAllPlaylists();

        // Assert
        assertEquals(3, playlists.size());
        for (var playlist : playlists) {
            if (playlist.getSpotifyID().getId().equals(apiPlaylist1.getId())) assertTrue(playlist.getIsSimplified());
            else assertFalse(playlist.getIsSimplified());
        }
    }

    @Test
    void ensure_items_can_be_added_to_playlist_independently() throws IOException {
        // Arrange
        final Playlist apiPlaylist = new Playlist.JsonUtil().createModelObject(
                loadFromPath("Spotify_Web_API_Testing_playlist.json"));
        assertFalse(spotifyObjectRepository.exists(apiPlaylist));
        final var playlist = spotifyObjectRepository.persist(apiPlaylist);
        final var originalTracks = spotifyObjectRepository.getPlaylistTracks(playlist);
        assertEquals(apiPlaylist.getTracks().getTotal(), originalTracks.size());
        final Paging<PlaylistTrack> apiExtraTracks = new PlaylistTrack.JsonUtil().createModelObjectPaging(
                loadFromPath("The_Blue_Stones.json"), "tracks");
        final var apiExtraTracksList = Arrays.stream(apiExtraTracks.getItems()).toList();


        // Act
        final List<SpotifyPlaylistItem> extraTracks = spotifyObjectRepository.persist(apiExtraTracksList, playlist);
        final List<SpotifyPlaylistItem> allTracks = spotifyObjectRepository.getPlaylistTracks(playlist);

        // Assert
        assertEquals(apiExtraTracksList.size(), extraTracks.size());
        assertEquals(originalTracks.size() + extraTracks.size(), allTracks.size());
        final var extraTrackIds = apiExtraTracksList.stream()
                .map(pt -> ((SpotifyTrack) spotifyObjectRepository.find(pt.getTrack().getId()).orElseThrow()).getId())
                .toList();
        final var originalTrackIds = originalTracks.stream().map(t -> t.getTrack().getId()).toList();
        final var allTrackIds = allTracks.stream().map(t -> t.getTrack().getId()).toList();
        assertTrue(allTrackIds.containsAll(extraTrackIds));
        assertTrue(allTrackIds.containsAll(originalTrackIds));
    }
}
