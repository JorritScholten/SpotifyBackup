package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyArtistRepositoryTest {
    static final String artistDir = "src/test/java/spotifybackup/storage/spotify_api_get/artist/";
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
    void ensure_artist_can_be_persisted() throws IOException {
        // Arrange
        final Artist apiArtist = new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Rivers_Cuomo.json")))
        );
        assertFalse(spotifyObjectRepository.exists(apiArtist),
                "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");

        // Act
        var persistedArtist = spotifyObjectRepository.persistArtist(apiArtist);

        // Assert
        assertTrue(spotifyObjectRepository.spotifyIDExists(apiArtist.getId()), "Can't find Artist by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedArtist), "Can't find Artist by Object reference.");
        assertTrue(apiArtist.getGenres().length > 0);
        assertEquals(apiArtist.getGenres().length, persistedArtist.getSpotifyGenres().size());
        assertTrue(apiArtist.getImages().length > 0);
        assertEquals(apiArtist.getImages().length, persistedArtist.getSpotifyImages().size());
    }

    @Test
    void ensure_multiple_artists_can_be_persisted() throws IOException {
        // Arrange
        final long oldCount = spotifyObjectRepository.countArtists();
        final Artist[] apiArtists = {new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Macklemore_&_Ryan_Lewis.json")))
        ), new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Macklemore.json")))
        ), new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Ryan_Lewis.json")))
        )};
        for (var apiArtist : apiArtists) {
            assertFalse(spotifyObjectRepository.exists(apiArtist),
                    "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");
        }

        // Act
        var persistedArtists = spotifyObjectRepository.persistArtists(apiArtists);

        // Assert
        for (var apiArtist : apiArtists) {
            var persistedArtist = persistedArtists.stream()
                    .filter(a -> a.getSpotifyID().getId().equals(apiArtist.getId()))
                    .findAny();
            assertTrue(spotifyObjectRepository.spotifyIDExists(apiArtist.getId()), "Can't find Artist by Spotify ID.");
            assertTrue(spotifyObjectRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
            assertTrue(persistedArtist.isPresent());
            assertTrue(spotifyObjectRepository.exists(persistedArtist.get()), "Can't find Artist by Object reference.");
            assertTrue(apiArtist.getGenres().length > 0);
            assertEquals(apiArtist.getGenres().length, persistedArtist.get().getSpotifyGenres().size());
            assertTrue(apiArtist.getImages().length > 0);
            assertEquals(apiArtist.getImages().length, persistedArtist.get().getSpotifyImages().size());
        }
        assertEquals(oldCount + apiArtists.length, spotifyObjectRepository.countArtists());
    }
}
