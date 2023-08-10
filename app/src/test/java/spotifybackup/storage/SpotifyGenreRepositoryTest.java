package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

public class SpotifyGenreRepositoryTest {
    static private SpotifyGenreRepository spotifyGenreRepository;

    @BeforeAll
    static void setup() {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "true");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            spotifyGenreRepository = new SpotifyGenreRepository(DB_ACCESS);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_genre_can_be_persisted() {
        // Arrange
        long oldCount = spotifyGenreRepository.count();
        final String genreName = "rock";
        assertFalse(spotifyGenreRepository.exists(genreName),
                "Genre with name " + genreName + " shouldn't already exist.");

        // Act
        var persistedGenre = spotifyGenreRepository.persist(genreName);

        // Assert
        assertEquals(oldCount + 1, spotifyGenreRepository.count());
        assertTrue(spotifyGenreRepository.exists(persistedGenre.orElseThrow()));
    }

    @Test
    void ensure_genres_arent_duplicated() {
        // Arrange
        final String genreName = "new genre";
        spotifyGenreRepository.persist(genreName);
        long oldCount = spotifyGenreRepository.count();

        // Act
        spotifyGenreRepository.persist(genreName);

        // Assert
        assertEquals(oldCount, spotifyGenreRepository.count());
    }

    @Test
    void ensure_persisted_genre_exists_in_db() {
        // Arrange
        final String genreName = "jazz";
        final var usedToExist = spotifyGenreRepository.exists(genreName);

        // Act
        spotifyGenreRepository.persist(genreName);

        // Assert
        assertFalse(usedToExist, "Genre with name " + genreName + " shouldn't already exist.");
        assertTrue(spotifyGenreRepository.exists(genreName));
    }

    @Test
    void persist_multiple_new_genres() {
        // Arrange
        final String[] genreNames = {"pop", "grunge-rock", "ska", "trance"};
        for (var genreName : genreNames) {
            assertFalse(spotifyGenreRepository.exists(genreName),
                    "Genre with name " + genreName + " shouldn't already exist.");
        }

        // Act
        final var persistedGenres = spotifyGenreRepository.persistAll(genreNames);

        // Assert
        for (var persistedGenre : persistedGenres) {
            assertTrue(spotifyGenreRepository.exists(persistedGenre));
        }
    }

    @Test
    void persist_some_new_genres() {
        // Arrange
        final String[] genreNames = {"pop", "grunge-rock", "hip-hop", "house"};
        assertTrue(spotifyGenreRepository.exists(spotifyGenreRepository.persist(genreNames[0]).orElseThrow()));
        assertTrue(spotifyGenreRepository.exists(spotifyGenreRepository.persist(genreNames[1]).orElseThrow()));
        assertFalse(spotifyGenreRepository.exists(genreNames[2]),
                "Genre with name " + genreNames[2] + " shouldn't already exist.");
        assertFalse(spotifyGenreRepository.exists(genreNames[3]),
                "Genre with name " + genreNames[3] + " shouldn't already exist.");

        // Act
        final var persistedGenres = spotifyGenreRepository.persistAll(genreNames);

        // Assert
        for (var persistedGenre : persistedGenres) {
            assertTrue(spotifyGenreRepository.exists(persistedGenre));
        }
    }
}
