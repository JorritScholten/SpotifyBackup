package spotifybackup.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyUserRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String playlistDir = testDataDir + "playlist/";
    static final String userDir = testDataDir + "user/";
    static final String artistDir = testDataDir + "artist/";
    private SpotifyObjectRepository spotifyObjectRepository;
    private List<SpotifyPlaylist> playlists;
    private Set<SpotifyID> playlistIds;
    private List<SpotifyArtist> artists;
    private Set<SpotifyID> artistIds;

    private User loadFromPath(String fileName) throws IOException {
        return new User.JsonUtil().createModelObject(new String(Files.readAllBytes(Path.of(userDir + fileName))));
    }

    @BeforeEach
    void setup() throws IOException {
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
        final Artist[] apiArtists = {new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Macklemore_&_Ryan_Lewis.json")))
        ), new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Macklemore.json")))
        ), new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Ryan_Lewis.json")))
        )};
        artists = spotifyObjectRepository.persist(apiArtists);
        artistIds = artists.stream().map(SpotifyArtist::getSpotifyID).collect(Collectors.toSet());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user.json", "user2.json", "user3.json"})
    void ensure_user_can_be_persisted(final String fileName) throws IOException {
        // Arrange
        final User apiUser = loadFromPath(fileName);
        assertFalse(spotifyObjectRepository.exists(apiUser.getId(), SpotifyUser.class));

        // Act
        final var user = spotifyObjectRepository.persist(apiUser);

        // Assert
        assertTrue(spotifyObjectRepository.find(apiUser.getId()).isPresent());
        assertEquals(apiUser.getId(), user.getSpotifyUserID());
        assertEquals(apiUser.getImages().length, user.getImages().size());
        assertEquals(apiUser.getDisplayName(), user.getDisplayName().orElseThrow());
        assertEquals(apiUser.getCountry().getAlpha2(), user.getCountryCode().orElseThrow());
        assertEquals(apiUser.getProduct(), user.getProductType().orElseThrow());
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, value = {
            "total, w,      h,      value",
            "3,     -1,     -1,     ALL",
            "1,     640,    640,    ONLY_LARGEST",
            "1,     64,     64,     ONLY_SMALLEST",
            "0,     -1,     -1,     NONE"
    })
    void ensure_user_can_be_persisted_with_different_image_selections(final int expectedTotal, final int w, final int h,
                                                                      final ImageSelection selection)
            throws IOException {
        // Arrange
        final User apiUser = loadFromPath("user.json");
        assertFalse(spotifyObjectRepository.exists(apiUser.getId(), SpotifyUser.class));
        assertTrue(apiUser.getImages().length > 1);
        for (var apiImage : apiUser.getImages()) assertFalse(spotifyObjectRepository.exists(apiImage));
        final var oldImageCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE);

        // Act
        final SpotifyUser user = spotifyObjectRepository.persist(apiUser, selection);

        // Assert
        assertTrue(spotifyObjectRepository.find(apiUser.getId()).isPresent());
        assertEquals(user.getSpotifyUserID(), apiUser.getId());
        assertEquals(expectedTotal, user.getImages().size());
        assertEquals(expectedTotal + oldImageCount, spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE));
        switch (selection) {
            case ONLY_LARGEST, ONLY_SMALLEST -> {
                assertEquals(1, user.getImages().size());
                SpotifyImage image = user.getImages().iterator().next();
                assertEquals(w, image.getWidth().orElseThrow());
                assertEquals(h, image.getHeight().orElseThrow());
                assertEquals(1, Arrays.stream(apiUser.getImages())
                        .filter(i -> i.getHeight() == h && i.getWidth() == w).count());
                assertEquals(Arrays.stream(apiUser.getImages()).filter(i -> i.getHeight() == h && i.getWidth() == w)
                        .findFirst().orElseThrow().getUrl(), image.getUrl());
            }
            case ALL -> assertEquals(expectedTotal, apiUser.getImages().length);
        }
    }

    @Test
    void find_account_holder_user_account() throws IOException {
        // Arrange
        // account holder
        final User apiUser = loadFromPath("user.json");
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
        final User apiUser = loadFromPath("user.json");
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
        final User apiUser = loadFromPath("user2.json");
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
        assertTrue(oldFollowedPlaylistIds.contains(playlists.getFirst().getSpotifyID()));
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

    @Test
    void ensure_artists_can_be_followed() throws IOException {
        // Arrange
        final User apiUser = loadFromPath("user.json");
        final var user = spotifyObjectRepository.persist(apiUser);
        assertEquals(0, spotifyObjectRepository.getFollowedArtists(user).size());

        // Act
        spotifyObjectRepository.followArtists(artists, user);
        final var followedArtists = spotifyObjectRepository.getFollowedArtists(user);

        // Assert
        assertEquals(artists.size(), followedArtists.size());
        final var followedArtistIds = followedArtists.stream().map(SpotifyArtist::getSpotifyID).toList();
        assertTrue(followedArtistIds.containsAll(artistIds));
    }

    @Test
    void ensure_artists_can_be_unfollowed() throws IOException {
        // Arrange
        final User apiUser = loadFromPath("user3.json");
        final var user = spotifyObjectRepository.persist(apiUser);
        spotifyObjectRepository.followArtists(artists, user);
        final var oldFollowedArtists = spotifyObjectRepository.getFollowedArtists(user);
        assertEquals(artists.size(), oldFollowedArtists.size());
        final var oldFollowedArtistIds = oldFollowedArtists.stream().map(SpotifyArtist::getSpotifyID).toList();
        assertTrue(oldFollowedArtistIds.containsAll(artistIds));

        // Act
        spotifyObjectRepository.unfollowArtists(List.of(artists.getFirst()), user);

        // Assert
        final var newFollowedArtists = spotifyObjectRepository.getFollowedArtists(user);
        assertEquals(oldFollowedArtists.size() - 1, newFollowedArtists.size());
        final var newFollowedArtistIds = newFollowedArtists.stream().map(SpotifyArtist::getSpotifyID).toList();
        assertFalse(newFollowedArtistIds.contains(artists.getFirst().getSpotifyID()));
        assertTrue(oldFollowedArtistIds.contains(artists.getFirst().getSpotifyID()));
    }
}
