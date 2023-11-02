package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyIDRepositoryTest {
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
    void ensure_id_can_be_persisted() {
        // Arrange
        final String newID = "abc90o328trjrsdffgj";
        assertFalse(spotifyObjectRepository.exists(newID, SpotifyID.class));

        // Act
        var persistedID = spotifyObjectRepository.persistSpotifyID(newID);

        // Assert
        assertTrue(persistedID.isPresent());
        assertTrue(spotifyObjectRepository.exists(persistedID.orElseThrow()));
        assertEquals(newID, persistedID.get().getId());
    }

    @Test
    void ensure_duplicate_ids_arent_persisted() {
        // Arrange
        final String newID = "123";
        final var newIDObject = spotifyObjectRepository.persistSpotifyID(newID).orElseThrow();
        assertTrue(spotifyObjectRepository.exists(newID, SpotifyID.class));

        // Act
        final var duplicateIDObject = spotifyObjectRepository.persistSpotifyID(newID).orElseThrow();

        // Assert
        assertTrue(spotifyObjectRepository.exists(duplicateIDObject));
        assertEquals(newIDObject.getId(), duplicateIDObject.getId());
        assertEquals(newIDObject, duplicateIDObject);
    }

    @Test
    void ensure_blank_ids_arent_persisted() {
        // Arrange
        final String blank = "";

        // Act
        final var optionalSpotifyID = spotifyObjectRepository.persistSpotifyID(blank);

        // Assert
        assertTrue(optionalSpotifyID.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> spotifyObjectRepository.exists(blank, SpotifyID.class));
    }
}
