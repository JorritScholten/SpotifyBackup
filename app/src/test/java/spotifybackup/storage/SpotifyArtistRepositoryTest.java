package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyArtistRepositoryTest {
    static final String artistDir = "src/test/java/spotifybackup/storage/spotify_api_get/artist/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
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
        var persistedArtist = spotifyObjectRepository.persist(apiArtist);

        // Assert
        assertTrue(spotifyObjectRepository.exists(apiArtist.getId(), SpotifyID.class),
                "Can't find Artist by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedArtist), "Can't find Artist by Object reference.");
        assertTrue(apiArtist.getGenres().length > 0);
        assertEquals(apiArtist.getGenres().length, persistedArtist.getGenres().size());
        assertTrue(apiArtist.getImages().length > 0);
        assertEquals(apiArtist.getImages().length, persistedArtist.getImages().size());
    }

    @Test
    void ensure_multiple_artists_can_be_persisted() throws IOException {
        // Arrange
        final long oldCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.ARTIST);
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
        var persistedArtists = spotifyObjectRepository.persist(apiArtists);

        // Assert
        for (var apiArtist : apiArtists) {
            var persistedArtist = persistedArtists.stream()
                    .filter(a -> a.getSpotifyID().getId().equals(apiArtist.getId()))
                    .findAny();
            assertTrue(spotifyObjectRepository.exists(apiArtist.getId(), SpotifyID.class),
                    "Can't find Artist by Spotify ID.");
            assertTrue(spotifyObjectRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
            assertTrue(persistedArtist.isPresent());
            assertTrue(spotifyObjectRepository.exists(persistedArtist.get()),
                    "Can't find Artist by Object reference.");
            assertTrue(apiArtist.getGenres().length > 0);
            assertEquals(apiArtist.getGenres().length, persistedArtist.get().getGenres().size());
            assertTrue(apiArtist.getImages().length > 0);
            assertEquals(apiArtist.getImages().length, persistedArtist.get().getImages().size());
        }
        assertEquals(oldCount + apiArtists.length, spotifyObjectRepository.count(SpotifyObject.SubTypes.ARTIST));
    }

    @Test
    void ensure_simplified_artist_can_be_filled_in_with_unsimplified() throws IOException {
        // Arrange
        final String artistJson = new String(Files.readAllBytes(Path.of(artistDir + "Texas.json")));
        final var apiArtistSimple = new ArtistSimplified.JsonUtil().createModelObject(artistJson);
        final var apiArtist = new Artist.JsonUtil().createModelObject(artistJson);
        assertNotEquals(0, apiArtist.getGenres().length);
        final var artistSimple = spotifyObjectRepository.persist(apiArtistSimple);
        assertTrue(spotifyObjectRepository.exists(artistSimple));
        assertTrue(artistSimple.getIsSimplified());
        assertEquals(0, artistSimple.getGenres().size());

        // Act
        final var artist = spotifyObjectRepository.persist(apiArtist);

        // Assert
        assertEquals(artistSimple.getId(), artist.getId());
        assertEquals(artistSimple.getSpotifyID(), artist.getSpotifyID());
        assertFalse(artist.getIsSimplified());
        assertNotEquals(0, artist.getGenres().size());
        assertEquals(apiArtist.getGenres().length, artist.getGenres().size());
    }

    @Test
    void ensure_simplified_artist_can_be_retrieved() throws IOException {
        // Arrange
        final String artistJson = new String(Files.readAllBytes(Path.of(artistDir + "Jake_Chudnow.json")));
        final var apiArtistSimple = new ArtistSimplified.JsonUtil().createModelObject(artistJson);
        final var apiArtist = new Artist.JsonUtil().createModelObject(artistJson);
        assertFalse(spotifyObjectRepository.exists(apiArtistSimple));
        final var artistSimple = spotifyObjectRepository.persist(apiArtistSimple);
        assertTrue(artistSimple.getIsSimplified());

        // Act
        final var oldSimpleArtistIds = spotifyObjectRepository.getSimplifiedArtistsSpotifyIDs();

        // Assert
        assertTrue(oldSimpleArtistIds.contains(apiArtistSimple.getId()));

        // Act 2
        spotifyObjectRepository.persist(apiArtist);
        final var newSimpleArtistIds = spotifyObjectRepository.getSimplifiedArtistsSpotifyIDs();

        // Assert 2
        assertFalse(newSimpleArtistIds.contains(apiArtistSimple.getId()));
    }
}
