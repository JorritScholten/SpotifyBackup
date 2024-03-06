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

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyUserRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String playlistDir = testDataDir + "playlist/";
    static final String userDir = testDataDir + "user/";
    static private SpotifyObjectRepository spotifyObjectRepository;
    static private List<SpotifyPlaylist> playlists;

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
        assertTrue(playlists.containsAll(followedPlaylists) && playlists.size() == followedPlaylists.size());
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
        assertTrue(playlists.containsAll(oldFollowedPlaylists) && playlists.size() == oldFollowedPlaylists.size());

        // Act
        spotifyObjectRepository.unfollowPlaylist(playlists.getFirst(), user);

        // Assert
        final var newFollowedPlaylists = spotifyObjectRepository.getFollowedPlaylists(user);
        assertFalse(newFollowedPlaylists.contains(playlists.getFirst()));
    }

    @Test
    void ensure_playlist_owner_can_be_retrieved() {
        // Arrange
        final SpotifyUser user = (SpotifyUser) spotifyObjectRepository.find("spotify").orElseThrow();

        // Act
        final var ownedPlaylists = spotifyObjectRepository.getOwnedPlaylists(user);

        // Assert
        assertEquals(1, ownedPlaylists.size());
        assertEquals(ownedPlaylists.getFirst(), playlists.get(1));
        assertEquals(ownedPlaylists.getFirst().getOwner(), user);
    }
}
