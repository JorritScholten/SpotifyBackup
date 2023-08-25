package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

public class SpotifyTrackRepositoryTest {
    static final String trackDir = "src/test/java/spotifybackup/storage/spotify_api_get/track/";
    static private SpotifyTrackRepository spotifyTrackRepository;

    @BeforeAll
    static void setup() {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "false");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            spotifyTrackRepository = new SpotifyTrackRepository(DB_ACCESS);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_track_can_be_persisted() throws IOException {
        // Arrange
        final Track apiTrack = new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "Bio-Engineering.json")))
        );
        assertFalse(spotifyTrackRepository.exists(apiTrack),
                "Track with Spotify ID " + apiTrack.getId() + " shouldn't already exist.");

        // Act
        var persistedTrack = spotifyTrackRepository.persist(apiTrack);

        // Assert
        assertTrue(spotifyTrackRepository.exists(apiTrack.getId()), "Can't find Track by Spotify ID.");
        assertTrue(spotifyTrackRepository.exists(apiTrack), "Can't find Track by apiTrack/Spotify ID.");
        assertTrue(spotifyTrackRepository.exists(persistedTrack), "Can't find Track by Object reference.");
        assertEquals(apiTrack.getArtists().length, persistedTrack.getSpotifyArtists().size());
        assertEquals(apiTrack.getAlbum().getId(), persistedTrack.getSpotifyAlbum().getSpotifyID().getId());
    }


    @Test
    void ensure_multiple_tracks_can_be_persisted() throws IOException {
        // Arrange
        final long oldCount = spotifyTrackRepository.count();
        final Track[] apiTracks = {new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "Can't_Hold_Us_(feat._Ray_Dalton).json")))
        ), new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "Thrift_Shop_(feat._Wanz).json")))
        ),};
        for (var apiTrack : apiTracks) {
            assertFalse(spotifyTrackRepository.exists(apiTrack),
                    "Track with Spotify ID " + apiTrack.getId() + " shouldn't already exist.");
        }

        // Act
        var persistedTracks = spotifyTrackRepository.persistAll(apiTracks);

        // Assert
        for (var apiTrack : apiTracks) {
            var persistedTrack = persistedTracks.stream()
                    .filter(t -> t.getSpotifyID().getId().equals(apiTrack.getId()))
                    .findAny();
            assertTrue(spotifyTrackRepository.exists(apiTrack.getId()), "Can't find Track by Spotify ID.");
            assertTrue(spotifyTrackRepository.exists(apiTrack), "Can't find Track by apiTrack/Spotify ID.");
            assertTrue(persistedTrack.isPresent());
            assertEquals(apiTrack.getArtists().length, persistedTrack.get().getSpotifyArtists().size());
            assertEquals(apiTrack.getAlbum().getId(), persistedTrack.get().getSpotifyAlbum().getSpotifyID().getId());
        }
        assertEquals(oldCount + apiTracks.length, spotifyTrackRepository.count());
    }
}
