package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyIDRepositoryTest {
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_id_can_be_persisted() {
        // Arrange
        final String newID = "abc90o328trjrsdffgj";
        assertFalse(spotifyObjectRepository.exists(newID, SpotifyID.class));

        // Act
        var persistedID = spotifyObjectRepository.persist(new SpotifyID(newID));

        // Assert
        assertTrue(spotifyObjectRepository.exists(persistedID));
        assertEquals(newID, persistedID.getId());
    }

    @Test
    void ensure_duplicate_ids_arent_persisted() {
        // Arrange
        final String newID = "123";
        final var newIDObject = spotifyObjectRepository.persist(new SpotifyID(newID));
        assertTrue(spotifyObjectRepository.exists(newID, SpotifyID.class));

        // Act
        final var duplicateIDObject = spotifyObjectRepository.persist(new SpotifyID(newID));

        // Assert
        assertTrue(spotifyObjectRepository.exists(duplicateIDObject));
        assertEquals(newIDObject.getId(), duplicateIDObject.getId());
        assertEquals(newIDObject, duplicateIDObject);
    }

    @Test
    void ensure_blank_ids_arent_persisted() {
        // Arrange
        final String blank = "";
        final var blankID = new SpotifyID(blank);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> spotifyObjectRepository.persist(blankID));
    }
}
