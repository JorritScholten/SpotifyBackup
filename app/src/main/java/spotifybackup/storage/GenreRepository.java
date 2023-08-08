package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;

import java.util.*;

public class GenreRepository {
    private final EntityManagerFactory emf;

    public GenreRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Find Genre by its name field.
     * @param genreName name of Genre.
     * @return Genre if genreName is not blank and in the table.
     */
    public Optional<Genre> find(@NonNull String genreName) {
        if (genreName.isBlank()) {
            return Optional.empty();
        }
        genreName = genreName.toLowerCase(Locale.ENGLISH);
        try (var entityManager = emf.createEntityManager()) {
            var query = entityManager.createNamedQuery("Genre.findByName", Genre.class);
            query.setParameter("name", genreName);
            try {
                return Optional.of(query.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        }
    }

    /**
     * Get count of genres in database.
     * @return count of genres in database.
     */
    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("Genre.countBy").getSingleResult();
        }
    }

    /**
     * Check if Genre exists by name.
     * @param genreName name of Genre.
     * @return true if Genre exists in database.
     */
    public boolean exists(@NonNull String genreName) {
        return find(genreName).isPresent();
    }

    /**
     * Check if Genre exists in database.
     * @param genre Genre to check.
     * @return true if genre exists in database.
     */
    public boolean exists(@NonNull Genre genre) {
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(Genre.class, genre.getId()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Attempts to persist a genre by its name, if it already exists returns already existing Genre.
     * @param genreName name of genre as defined by Spotify.
     * @return Genre if genreName is not blank.
     */
    public Optional<Genre> persist(@NonNull String genreName) {
        if (genreName.isBlank()) {
            return Optional.empty();
        }
        var optionalGenre = find(genreName);
        if (optionalGenre.isPresent()) {
            return optionalGenre;
        } else {
            try (var entityManager = emf.createEntityManager()) {
                var newGenre = new Genre(genreName);
                entityManager.getTransaction().begin();
                entityManager.persist(newGenre);
                entityManager.getTransaction().commit();
                return Optional.of(newGenre);
            }
        }
    }

    /**
     * Attempts to persist an array of genres by name, if a Genre already exists the already existing Genre is used.
     * @param genreNames an array of genre names as define by Spotify.
     * @return Set of Genre objects.
     */
    public Set<Genre> persistAll(@NonNull String[] genreNames) {
        Set<Genre> genreSet = new HashSet<>();
        for (var genreName : genreNames) {
            var genre = persist(genreName);
            genre.ifPresent(genreSet::add);
        }
        return genreSet;
    }
}

