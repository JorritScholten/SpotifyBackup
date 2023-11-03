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
            spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_genre_can_be_persisted() {
        // Arrange
        long oldCount = spotifyObjectRepository.count(SpotifyGenre.class);
        final String genreName = "rock";
        assertFalse(spotifyObjectRepository.exists(genreName, SpotifyGenre.class),
                "Genre with name " + genreName + " shouldn't already exist.");

        // Act
        var persistedGenre = spotifyObjectRepository.persist(genreName);

        // Assert
        assertEquals(oldCount + 1, spotifyObjectRepository.count(SpotifyGenre.class));
        assertTrue(spotifyObjectRepository.exists(persistedGenre));
    }

    @Test
    void ensure_genres_arent_duplicated() {
        // Arrange
        final String genreName = "new genre";
        spotifyObjectRepository.persist(genreName);
        long oldCount = spotifyObjectRepository.count(SpotifyGenre.class);

        // Act
        spotifyObjectRepository.persist(genreName);

        // Assert
        assertEquals(oldCount, spotifyObjectRepository.count(SpotifyGenre.class));
    }

    @Test
    void ensure_persisted_genre_exists_in_db() {
        // Arrange
        final String genreName = "jazz";
        final var usedToExist = spotifyObjectRepository.exists(genreName, SpotifyGenre.class);

        // Act
        spotifyObjectRepository.persist(genreName);

        // Assert
        assertFalse(usedToExist, "Genre with name " + genreName + " shouldn't already exist.");
        assertTrue(spotifyObjectRepository.exists(genreName, SpotifyGenre.class));
    }

    @Test
    void persist_multiple_new_genres() {
        // Arrange
        final String[] genreNames = {"pop", "grunge-rock", "ska", "trance"};
        for (var genreName : genreNames) {
            assertFalse(spotifyObjectRepository.exists(genreName, SpotifyGenre.class),
                    "Genre with name " + genreName + " shouldn't already exist.");
        }

        // Act
        final var persistedGenres = spotifyObjectRepository.persist(genreNames);

        // Assert
        for (var persistedGenre : persistedGenres) {
            assertTrue(spotifyObjectRepository.exists(persistedGenre));
        }
    }

    @Test
    void persist_some_new_genres() {
        // Arrange
        final String[] genreNames = {"pop", "grunge-rock", "hip-hop", "house"};
        assertTrue(spotifyObjectRepository.exists(spotifyObjectRepository.persist(genreNames[0])));
        assertTrue(spotifyObjectRepository.exists(spotifyObjectRepository.persist(genreNames[1])));
        assertFalse(spotifyObjectRepository.exists(genreNames[2], SpotifyGenre.class),
                "Genre with name " + genreNames[2] + " shouldn't already exist.");
        assertFalse(spotifyObjectRepository.exists(genreNames[3], SpotifyGenre.class),
                "Genre with name " + genreNames[3] + " shouldn't already exist.");

        // Act
        final var persistedGenres = spotifyObjectRepository.persist(genreNames);

        // Assert
        for (var persistedGenre : persistedGenres) {
            assertTrue(spotifyObjectRepository.exists(persistedGenre));
        }
    }
}
