package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Album;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpotifyAlbumRepositoryTest {
    static private SpotifyAlbumRepository spotifyAlbumRepository;
    static final String testDir = "src/test/java/spotifybackup/storage/";

    @BeforeAll
    static void setup() {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "false");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            spotifyAlbumRepository = new SpotifyAlbumRepository(DB_ACCESS);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_album_can_be_persisted() throws IOException {
        // Arrange
        final Album apiAlbum = new Album.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(testDir + "spotify-get-an-album-The_Heist.json")))
        );
        assertFalse(spotifyAlbumRepository.exists(apiAlbum),
                "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertTrue(apiAlbum.getGenres().length > 0, "Album should have 1 or more genres.");

        // Act
        var persistedAlbum = spotifyAlbumRepository.persist(apiAlbum);

        // Assert
        assertTrue(spotifyAlbumRepository.exists(apiAlbum.getId()), "Can't find Album by Spotify ID.");
        assertTrue(spotifyAlbumRepository.exists(apiAlbum), "Can't find Album by apiAlbum/Spotify ID.");
        assertTrue(spotifyAlbumRepository.exists(persistedAlbum), "Can't find Album by Object reference.");
        assertEquals(apiAlbum.getArtists().length, persistedAlbum.getSpotifyArtists().size());
        assertEquals(apiAlbum.getGenres().length, persistedAlbum.getSpotifyGenres().size());
        assertEquals(apiAlbum.getImages().length, persistedAlbum.getSpotifyImages().size());
        assertEquals(apiAlbum.getTracks().getTotal(), persistedAlbum.getSpotifyTracks().size());
    }
}
