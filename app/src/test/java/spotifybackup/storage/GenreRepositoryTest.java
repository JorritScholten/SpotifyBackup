package spotifybackup.storage;

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
        genreRepository = new GenreRepository(DB_ACCESS);
    }

    @Test
    void ensure_genre_can_be_persisted() {
        // Arrange
        long oldCount = genreRepository.count();
        final String genreName = "rock";

        // Act
        genreRepository.persist(genreName);

        // Assert
        assertEquals(oldCount + 1, genreRepository.count());
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
}
