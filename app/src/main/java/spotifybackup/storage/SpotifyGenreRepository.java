package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;

import java.util.*;

public class SpotifyGenreRepository {
    private final EntityManagerFactory emf;

    public SpotifyGenreRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Find SpotifyGenre by its name field.
     * @param genreName name of SpotifyGenre.
     * @return SpotifyGenre if genreName is not blank and in the table.
     */
    public Optional<SpotifyGenre> find(@NonNull String genreName) {
        if (genreName.isBlank()) {
            return Optional.empty();
        }
        genreName = genreName.toLowerCase(Locale.ENGLISH);
        try (var entityManager = emf.createEntityManager()) {
            var query = entityManager.createNamedQuery("SpotifyGenre.findByName", SpotifyGenre.class);
            query.setParameter("name", genreName);
            try {
                return Optional.of(query.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        }
    }

    /**
     * Get count of genres in the database.
     * @return count of genres in the database.
     */
    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("SpotifyGenre.countBy").getSingleResult();
        }
    }

    /**
     * Check if SpotifyGenre exists by name.
     * @param genreName name of Genre.
     * @return true if genreName exists as a SpotifyGenre in the database.
     */
    public boolean exists(@NonNull String genreName) {
        return find(genreName).isPresent();
    }

    /**
     * Check if SpotifyGenre exists in the database.
     * @param spotifyGenre SpotifyGenre to check.
     * @return true if spotifyGenre exists in the database.
     */
    public boolean exists(@NonNull SpotifyGenre spotifyGenre) {
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(SpotifyGenre.class, spotifyGenre.getId()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Attempts to persist a genre by its name, if it already exists returns already existing SpotifyGenre.
     * @param genreName name of genre as defined by Spotify.
     * @return SpotifyGenre if genreName is not blank.
     */
    public Optional<SpotifyGenre> persist(@NonNull String genreName) {
        if (genreName.isBlank()) {
            return Optional.empty();
        }
        var optionalGenre = find(genreName);
        if (optionalGenre.isPresent()) {
            return optionalGenre;
        } else {
            try (var entityManager = emf.createEntityManager()) {
                var newGenre = new SpotifyGenre(genreName);
                entityManager.getTransaction().begin();
                entityManager.persist(newGenre);
                entityManager.getTransaction().commit();
                return Optional.of(newGenre);
            }
        }
    }

    /**
     * Attempts to persist an array of genres by name, if a Genre already exists the already existing SpotifyGenre is
     * used.
     * @param genreNames an array of genre names as defined by Spotify.
     * @return Set of SpotifyGenre objects.
     */
    public Set<SpotifyGenre> persistAll(@NonNull String[] genreNames) {
        Set<SpotifyGenre> spotifyGenreSet = new HashSet<>();
        for (var genreName : genreNames) {
            var genre = persist(genreName);
            genre.ifPresent(spotifyGenreSet::add);
        }
        return spotifyGenreSet;
    }
}

