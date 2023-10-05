package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.NonNull;

import java.util.Optional;
import java.util.Properties;

public class SpotifyIDRepository {
    private final EntityManagerFactory emf;

    /**
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public SpotifyIDRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Find SpotifyID by string value.
     * @param id String containing a Spotify ID.
     * @return SpotifyID if id matches the id field in the table and not blank.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public Optional<SpotifyID> find(@NonNull String id) {
        if (id.isBlank()) {
            return Optional.empty();
        }
        try (var entityManager = emf.createEntityManager()) {
            return Optional.of(entityManager.find(SpotifyID.class, id));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    /**
     * Check if SpotifyID exists in persistence context by Spotify ID.
     * @param id Spotify ID to check.
     * @return true if Spotify ID exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public boolean exists(@NonNull String id) {
        return find(id).isPresent();
    }

    /**
     * Check if SpotifyID exists in persistence context.
     * @param spotifyID SpotifyID to check.
     * @return true if spotifyID exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public boolean exists(@NonNull SpotifyID spotifyID) {
        return exists(spotifyID.getId());
    }

    /**
     * Attempts to persist a Spotify ID by its string representation, if it already exists returns already existing
     * SpotifyID.
     * @param id Spotify ID to persist (base-62 identifier as defined by Spotify).
     * @return SpotifyID if id is not blank.
     * @deprecated Use SpotifyObjectRepository instead.
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
