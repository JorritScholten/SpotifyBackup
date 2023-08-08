package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class GenreRepositoryTest {
    static private GenreRepository genreRepository;

    @BeforeAll
    static void setup() {
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "true");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            genreRepository = new GenreRepository(DB_ACCESS);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_genre_can_be_persisted() {
        // Arrange
        long oldCount = genreRepository.count();
        final String genreName = "rock";
        assertFalse(genreRepository.exists(genreName),
                "Genre with name " + genreName + " shouldn't already exist.");

        // Act
        var persistedGenre = genreRepository.persist(genreName);

        // Assert
        assertEquals(oldCount + 1, genreRepository.count());
        assertTrue(genreRepository.exists(persistedGenre.orElseThrow()));
    }

    @Test
    void ensure_genres_arent_duplicated() {
        // Arrange
        final String genreName = "new genre";
        genreRepository.persist(genreName);
        long oldCount = genreRepository.count();

        // Act
        genreRepository.persist(genreName);

        // Assert
        assertEquals(oldCount, genreRepository.count());
    }

    @Test
    void ensure_persisted_genre_exists_in_db() {
        // Arrange
        final String genreName = "jazz";
        final var usedToExist = genreRepository.exists(genreName);

        // Act
        genreRepository.persist(genreName);

        // Assert
        assertFalse(usedToExist, "Genre with name " + genreName + " shouldn't already exist.");
        assertTrue(genreRepository.exists(genreName));
    }

    @Test
    void persist_multiple_new_genres() {
        // Arrange
        final String[] genreNames = {"pop", "grunge-rock", "ska", "trance"};
        for (var genreName : genreNames) {
            assertFalse(genreRepository.exists(genreName),
                    "Genre with name " + genreName + " shouldn't already exist.");
        }

        // Act
        final var persistedGenres = genreRepository.persistAll(genreNames);

        // Assert
        for (var persistedGenre : persistedGenres) {
            assertTrue(genreRepository.exists(persistedGenre));
        }
    }

    @Test
    void persist_some_new_genres() {
        // Arrange
        final String[] genreNames = {"pop", "grunge-rock", "hip-hop", "house"};
        assertTrue(genreRepository.exists(genreRepository.persist(genreNames[0]).orElseThrow()));
        assertTrue(genreRepository.exists(genreRepository.persist(genreNames[1]).orElseThrow()));
        assertFalse(genreRepository.exists(genreNames[2]),
                "Genre with name " + genreNames[2] + " shouldn't already exist.");
        assertFalse(genreRepository.exists(genreNames[3]),
                "Genre with name " + genreNames[3] + " shouldn't already exist.");

        // Act
        final var persistedGenres = genreRepository.persistAll(genreNames);

        // Assert
        for (var persistedGenre : persistedGenres) {
            assertTrue(genreRepository.exists(persistedGenre));
        }
    }
}
