package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

public class SpotifyIDRepositoryTest {
    static private SpotifyIDRepository spotifyIDRepository;

    @BeforeAll
    static void setup() {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "true");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            spotifyIDRepository = new SpotifyIDRepository(DB_ACCESS);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_id_can_be_persisted() {
        // Arrange
        final String newID = "abc90o328trjrsdffgj";
        assertFalse(spotifyIDRepository.exists(newID));

        // Act
        var persistedID = spotifyIDRepository.persist(newID);

        // Assert
        assertTrue(persistedID.isPresent());
        assertTrue(spotifyIDRepository.exists(persistedID.orElseThrow()));
        assertEquals(newID, persistedID.get().getId());
    }

    @Test
    void ensure_duplicate_ids_arent_persisted() {
        // Arrange
        final String newID = "123";
        final var newIDObject = spotifyIDRepository.persist(newID).orElseThrow();
        assertTrue(spotifyIDRepository.exists(newID));

        // Act
        final var duplicateIDObject = spotifyIDRepository.persist(newID).orElseThrow();

        // Assert
        assertTrue(spotifyIDRepository.exists(duplicateIDObject));
        assertEquals(newIDObject.getId(), duplicateIDObject.getId());
        assert (newIDObject.equals(duplicateIDObject));
    }

    @Test
    void ensure_blank_ids_arent_persisted() {
        // Arrange
        final String blank = "";

        // Act
        final var optionalSpotifyID = spotifyIDRepository.persist(blank);

        // Assert
        assertTrue(optionalSpotifyID.isEmpty());
        assertFalse(spotifyIDRepository.exists(blank));
    }
}
