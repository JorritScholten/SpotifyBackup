package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyUserRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String playlistDir = testDataDir + "playlist/";
    static final String userDir = testDataDir + "user/";
    static private SpotifyObjectRepository spotifyObjectRepository;
    static private List<SpotifyPlaylist> playlists;
    static private Set<SpotifyID> playlistIds;

    @BeforeAll
    static void setup() throws IOException {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
        final PlaylistSimplified[] apiPlaylists = {
                new PlaylistSimplified.JsonUtil().createModelObject(
                        new String(Files.readAllBytes(Path.of(playlistDir + "Spotify_Web_API_Testing_playlist.json")))),
                new PlaylistSimplified.JsonUtil().createModelObject(
                        new String(Files.readAllBytes(Path.of(playlistDir + "The_Blue_Stones.json")))
                )
        };
        playlists = List.of(spotifyObjectRepository.persist(apiPlaylists[0]),
                spotifyObjectRepository.persist(apiPlaylists[1]));
        playlistIds = playlists.stream().map(SpotifyPlaylist::getSpotifyID).collect(Collectors.toSet());
    }

    @Test
    void ensure_user_can_be_persisted() throws IOException {
        // Arrange
        final User apiUser = new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user2.json")))
        );
        assertTrue(spotifyObjectRepository.find(apiUser.getId()).isEmpty());

        // Act
        final var user = spotifyObjectRepository.persist(apiUser);

        // Assert
        assertTrue(spotifyObjectRepository.find(apiUser.getId()).isPresent());
        assertEquals(user.getSpotifyUserID(), apiUser.getId());
    }

    @Test
    void find_account_holder_user_account() throws IOException {
        // Arrange
        // account holder
        final User apiUser = new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user.json")))
        );
        final var user = spotifyObjectRepository.persist(apiUser);

        // Act
        final var accountHolders = spotifyObjectRepository.getAccountHolders();

        // Assert
        assertTrue(accountHolders.stream().anyMatch(u -> u.getSpotifyUserID().equals(user.getSpotifyUserID())));
        assertTrue(accountHolders.stream().anyMatch(u -> u.getId() == user.getId()));
    }

    @Test
    void ensure_playlists_can_be_followed() throws IOException {
        // Arrange
        final User apiUser = new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user.json")))
        );
        final var user = spotifyObjectRepository.persist(apiUser);
        assertTrue(spotifyObjectRepository.getFollowedPlaylists(user).isEmpty());

        // Act
        spotifyObjectRepository.followPlaylists(playlists, user);
        final var followedPlaylists = spotifyObjectRepository.getFollowedPlaylists(user);

        // Assert
        assertEquals(playlists.size(), followedPlaylists.size());
        final var followedPlaylistIds = followedPlaylists.stream().map(SpotifyPlaylist::getSpotifyID).toList();
        assertTrue(playlistIds.containsAll(followedPlaylistIds));
    }

    @Test
    void ensure_playlists_can_be_unfollowed() throws IOException {
        // Arrange
        final User apiUser = new User.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(userDir + "user2.json")))
        );
        final var user = spotifyObjectRepository.persist(apiUser);
        spotifyObjectRepository.followPlaylists(playlists, user);
        final var oldFollowedPlaylists = spotifyObjectRepository.getFollowedPlaylists(user);
        assertEquals(playlists.size(), oldFollowedPlaylists.size());
        final var oldFollowedPlaylistIds = oldFollowedPlaylists.stream().map(SpotifyPlaylist::getSpotifyID).toList();
        assertTrue(playlistIds.containsAll(oldFollowedPlaylistIds));

        // Act
        spotifyObjectRepository.unfollowPlaylists(List.of(playlists.getFirst()), user);

        // Assert
        final var newFollowedPlaylists = spotifyObjectRepository.getFollowedPlaylists(user);
        assertEquals(oldFollowedPlaylists.size() - 1, newFollowedPlaylists.size());
        final var newFollowedPlaylistIds = newFollowedPlaylists.stream().map(SpotifyPlaylist::getSpotifyID).toList();
        assertFalse(newFollowedPlaylistIds.contains(playlists.getFirst().getSpotifyID()));
    }

    @Test
    void ensure_playlist_owner_can_be_retrieved() {
        // Arrange
        final SpotifyUser user = (SpotifyUser) spotifyObjectRepository.find("spotify").orElseThrow();

        // Act
        final var ownedPlaylists = spotifyObjectRepository.getOwnedPlaylists(user);

        // Assert
        assertEquals(1, ownedPlaylists.size());
        final var ownedPlaylistIds = ownedPlaylists.stream().map(SpotifyPlaylist::getSpotifyID).toList();
        assertEquals(playlists.get(1).getSpotifyID(), ownedPlaylistIds.getFirst());
        assertTrue(ownedPlaylists.stream().allMatch(p -> p.getOwner().getSpotifyUserID().equals(user.getSpotifyUserID())));
    }
}
