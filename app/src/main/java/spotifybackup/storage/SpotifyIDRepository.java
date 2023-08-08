package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.NonNull;

import java.util.Optional;
import java.util.Properties;

public class SpotifyIDRepository {
    private final EntityManagerFactory emf;

    public SpotifyIDRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Check if SpotifyID exists in persistence context by Spotify ID.
     * @param id Spotify ID to check.
     * @return true if Spotify ID exists in persistence context.
     */
    public boolean exists(@NonNull String id) {
        if (id.isBlank()) {
            return false;
        }
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(SpotifyID.class, id) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if SpotifyID exists in persistence context.
     * @param spotifyID SpotifyID to check.
     * @return true if spotifyID exists in persistence context.
     */
    public boolean exists(@NonNull SpotifyID spotifyID) {
        return exists(spotifyID.getId());
    }

    /**
     * Attempts to persist a Spotify ID by its string representation, if it already exists returns already existing
     * SpotifyID.
     * @param id Spotify ID to persist (base-62 identifier as defined by Spotify).
     * @return SpotifyID if id is not blank.
     */
    public Optional<SpotifyID> persist(@NonNull String id) {
        if (id.isBlank()) {
            return Optional.empty();
        }
        try (var entityManager = emf.createEntityManager()) {
            try {
                return Optional.of(entityManager.find(SpotifyID.class, id));
            } catch (IllegalArgumentException | NullPointerException e) {
                var newID = new SpotifyID(id);
                entityManager.getTransaction().begin();
                entityManager.persist(newID);
                entityManager.getTransaction().commit();
                return Optional.of(newID);
            }
        }
    }
}
