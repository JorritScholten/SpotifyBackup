package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class SpotifyArtistRepository {
    private final EntityManagerFactory emf;

    /**
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public SpotifyArtistRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Find SpotifyArtist by spotifyID field.
     * @param apiArtist ArtistSimplified object generated by the spotify-web-api.
     * @return SpotifyArtist if apiArtist already exists in the database.
     */
    static Optional<SpotifyArtist> find(EntityManager entityManager, @NonNull ArtistSimplified apiArtist) {
        return find(entityManager, apiArtist.getId());
    }

    /**
     * Find SpotifyArtist by spotifyID field.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return SpotifyArtist if apiArtist already exists in the database.
     */
    static Optional<SpotifyArtist> find(EntityManager entityManager, @NonNull Artist apiArtist) {
        return find(entityManager, apiArtist.getId());
    }

    /**
     * Find SpotifyArtist by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return SpotifyArtist if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyArtist> find(EntityManager entityManager, @NonNull String id) {
        if (id.isBlank() || entityManager.find(SpotifyID.class, id) == null) {
            return Optional.empty();
        }
        var query = entityManager.createNamedQuery("SpotifyArtist.findBySpotifyID", SpotifyArtist.class);
        query.setParameter("spotifyID", entityManager.find(SpotifyID.class, id));
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to persist an ArtistSimplified from the output of the spotify-web-api.
     * @param apiArtist ArtistSimplified object generated by the spotify-web-api.
     * @return SpotifyArtist already in the database with matching Spotify ID or new SpotifyArtist if apiArtist has a
     * new Spotify ID.
     */
    static SpotifyArtist persist(EntityManager entityManager, @NonNull ArtistSimplified apiArtist) {
        var optionalArtist = find(entityManager, apiArtist);
        if (optionalArtist.isPresent()) {
            return optionalArtist.get();
        } else {
            var newArtist = SpotifyArtist.builder()
                    .name(apiArtist.getName())
                    .spotifyID(new SpotifyID(apiArtist.getId()))
                    .isSimplified(true)
                    .build();
            entityManager.persist(newArtist);
            return newArtist;
        }
    }

    /**
     * Attempts to persist an Artist from the output of the spotify-web-api.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return Artist already in the database with matching Spotify ID or new Artist if apiArtist has a new Spotify ID.
     */
    static SpotifyArtist persist(EntityManager entityManager, @NonNull Artist apiArtist) {
        var optionalArtist = find(entityManager, apiArtist);
        if (optionalArtist.isPresent()) {
            return optionalArtist.get();
        } else {
            var newArtist = SpotifyArtist.builder()
                    .name(apiArtist.getName())
                    .spotifyID(new SpotifyID(apiArtist.getId()))
                    .isSimplified(false)
                    .build();
            newArtist.addImages(SpotifyImageRepository.imageSetFactory(entityManager, apiArtist.getImages()));
            newArtist.addGenres(SpotifyGenreRepository.genreSetFactory(entityManager, apiArtist.getGenres()));
            entityManager.persist(newArtist);
            return newArtist;
        }
    }

    /**
     * Get count of artists in the database.
     * @return count of genres in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("SpotifyArtist.countBy").getSingleResult();
        }
    }

    /**
     * Find Artist by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return Artist if id matches the spotify_id field in the table and not blank.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public Optional<SpotifyArtist> find(@NonNull String id) {
        try (var entityManager = emf.createEntityManager()) {
            return find(entityManager, id);
        }
    }

    /**
     * Check if Artist exists in the database.
     * @param spotifyArtist SpotifyArtist to check
     * @return true if artist exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public boolean exists(@NonNull SpotifyArtist spotifyArtist) {
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(SpotifyArtist.class, spotifyArtist.getId()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Find Artist by spotifyID field.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return Artist if apiArtist already exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public Optional<SpotifyArtist> find(@NonNull Artist apiArtist) {
        return find(apiArtist.getId());
    }

    /**
     * Check if Artist exists in the database by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return true if SpotifyArtist specified by id exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public boolean exists(@NonNull String id) {
        return find(id).isPresent();
    }

    /**
     * Checks if Artist object generated by the spotify-web-api exists in the database.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return true if apiArtist exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public boolean exists(@NonNull Artist apiArtist) {
        return find(apiArtist).isPresent();
    }

    /**
     * Attempts to persist an Artist from the output of the spotify-web-api.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return Artist already in the database with matching Spotify ID or new Artist if apiArtist has a new Spotify ID.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public SpotifyArtist persist(@NonNull Artist apiArtist) {
        try (var entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            var spotifyArtist = persist(entityManager, apiArtist);
            entityManager.getTransaction().commit();
            return spotifyArtist;
        }
    }

    /**
     * Attempts to persist an array of Artist objects from the output of the spotify-web-api.
     * @param apiArtists An array of Artist objects generated by the spotify-web-api.
     * @return Set of SpotifyArtist objects.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    public Set<SpotifyArtist> persistAll(@NonNull Artist[] apiArtists) {
        Set<SpotifyArtist> spotifyArtistSet = new HashSet<>();
        for (var apiArtist : apiArtists) {
            spotifyArtistSet.add(persist(apiArtist));
        }
        return spotifyArtistSet;
    }
}
