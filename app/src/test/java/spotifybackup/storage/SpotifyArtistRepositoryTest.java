package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

public class SpotifyArtistRepositoryTest {
    static private SpotifyArtistRepository spotifyArtistRepository;
    static private SpotifyObjectRepository spotifyObjectRepository;
    static final String artistDir = "src/test/java/spotifybackup/storage/spotify_api_get/artist/";

    @BeforeAll
    static void setup() {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "false");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            spotifyArtistRepository = new SpotifyArtistRepository(DB_ACCESS);
            spotifyObjectRepository = SpotifyObjectRepository.testFactory(true);
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
        assertFalse(spotifyArtistRepository.exists(apiArtist),
                "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");

        // Act
        var persistedArtist = spotifyArtistRepository.persist(apiArtist);

        // Assert
        assertTrue(spotifyArtistRepository.exists(apiArtist.getId()), "Can't find Artist by Spotify ID.");
        assertTrue(spotifyArtistRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
        assertTrue(spotifyArtistRepository.exists(persistedArtist), "Can't find Artist by Object reference.");
        assertEquals(apiArtist.getGenres().length, persistedArtist.getSpotifyGenres().size());
        assertEquals(apiArtist.getImages().length, persistedArtist.getSpotifyImages().size());
    }

    @Test
    void ensure_multiple_artists_can_be_persisted() throws IOException {
        // Arrange
        final long oldCount = spotifyArtistRepository.count();
        final Artist[] apiArtists = {new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Macklemore_&_Ryan_Lewis.json")))
                ), new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Macklemore.json")))
                ), new Artist.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(artistDir + "Ryan_Lewis.json")))
                ),
        };
        for (var apiArtist : apiArtists) {
            assertFalse(spotifyArtistRepository.exists(apiArtist),
                    "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");
        }

        // Act
        var persistedArtists = spotifyArtistRepository.persistAll(apiArtists);

        // Assert
        for (var apiArtist : apiArtists) {
            var persistedArtist = persistedArtists.stream()
                    .filter(a -> a.getSpotifyID().getId().equals(apiArtist.getId()))
                    .findAny();
            assertTrue(spotifyArtistRepository.exists(apiArtist.getId()), "Can't find Artist by Spotify ID.");
            assertTrue(spotifyArtistRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
            assertTrue(persistedArtist.isPresent());
            assertTrue(spotifyArtistRepository.exists(persistedArtist.get()), "Can't find Artist by Object reference.");
            assertEquals(apiArtist.getGenres().length, persistedArtist.get().getSpotifyGenres().size());
            assertEquals(apiArtist.getImages().length, persistedArtist.get().getSpotifyImages().size());
        }
        assertEquals(oldCount + apiArtists.length, spotifyArtistRepository.count());
    }
}
