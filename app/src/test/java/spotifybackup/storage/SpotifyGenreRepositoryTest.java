package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyGenreRepositoryTest {
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        try {
            spotifyObjectRepository = SpotifyObjectRepository.testFactory(true);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_genre_can_be_persisted() {
        // Arrange
        long oldCount = spotifyObjectRepository.countGenres();
        final String genreName = "rock";
        assertFalse(spotifyObjectRepository.genreExists(genreName),
                "Genre with name " + genreName + " shouldn't already exist.");

        // Act
        var persistedGenre = spotifyObjectRepository.persistGenre(genreName);

        // Assert
        assertEquals(oldCount + 1, spotifyObjectRepository.countGenres());
        assertTrue(spotifyObjectRepository.exists(persistedGenre.orElseThrow()));
    }

    @Test
    void ensure_genres_arent_duplicated() {
        // Arrange
        final String genreName = "new genre";
        spotifyObjectRepository.persistGenre(genreName);
        long oldCount = spotifyObjectRepository.countGenres();

        // Act
        spotifyObjectRepository.persistGenre(genreName);

        // Assert
        assertEquals(oldCount, spotifyObjectRepository.countGenres());
    }

    @Test
    void ensure_persisted_genre_exists_in_db() {
        // Arrange
        final String genreName = "jazz";
        final var usedToExist = spotifyObjectRepository.genreExists(genreName);

        // Act
        spotifyObjectRepository.persistGenre(genreName);

        // Assert
        assertFalse(usedToExist, "Genre with name " + genreName + " shouldn't already exist.");
        assertTrue(spotifyObjectRepository.genreExists(genreName));
    }

    @Test
    void persist_multiple_new_genres() {
        // Arrange
        final String[] genreNames = {"pop", "grunge-rock", "ska", "trance"};
        for (var genreName : genreNames) {
            assertFalse(spotifyObjectRepository.genreExists(genreName),
                    "Genre with name " + genreName + " shouldn't already exist.");
        }

        // Act
        final var persistedGenres = spotifyObjectRepository.persistGenres(genreNames);

        // Assert
        for (var persistedGenre : persistedGenres) {
            assertTrue(spotifyObjectRepository.exists(persistedGenre));
        }
    }

    @Test
    void persist_some_new_genres() {
        // Arrange
        final String[] genreNames = {"pop", "grunge-rock", "hip-hop", "house"};
        assertTrue(spotifyObjectRepository.exists(spotifyObjectRepository.persistGenre(genreNames[0]).orElseThrow()));
        assertTrue(spotifyObjectRepository.exists(spotifyObjectRepository.persistGenre(genreNames[1]).orElseThrow()));
        assertFalse(spotifyObjectRepository.genreExists(genreNames[2]),
                "Genre with name " + genreNames[2] + " shouldn't already exist.");
        assertFalse(spotifyObjectRepository.genreExists(genreNames[3]),
                "Genre with name " + genreNames[3] + " shouldn't already exist.");

        // Act
        final var persistedGenres = spotifyObjectRepository.persistGenres(genreNames);

        // Assert
        for (var persistedGenre : persistedGenres) {
            assertTrue(spotifyObjectRepository.exists(persistedGenre));
        }
    }
}
