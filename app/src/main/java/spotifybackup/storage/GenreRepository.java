package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;

import java.util.*;

public class GenreRepository {
    private final EntityManagerFactory emf; // = Persistence.createEntityManagerFactory("testdb");

    public GenreRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory("testdb", DB_ACCESS);
    }

    /**
     * Find Genre by its id field.
     * @param id id of record in table.
     * @return Genre object matching id.
     * @deprecated replace this method with find(Genre genre) and find(String genreName)
     */
    @Deprecated
    public Genre find(long id) {
        try (var entityManager = emf.createEntityManager()) {
            Genre genre = entityManager.find(Genre.class, id);
            return genre;
        }
    }

    /**
     * Get count of genres in db.
     * @return count of genres in db.
     */
    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("Genre.countBy").getSingleResult();
        }
    }

    /**
     * Check if Genre exists by name.
     * @param genreName name of Genre.
     * @return true if Genre exists in db.
     */
    public boolean exists(@NonNull String genreName) {
        if (genreName.isBlank()) {
            return false;
        }
        genreName = genreName.toLowerCase(Locale.ENGLISH);
        try (var entityManager = emf.createEntityManager()) {
            var query = entityManager.createNamedQuery("Genre.findByName", Genre.class);
            query.setParameter("name", genreName);
            try {
                return query.getSingleResult() != null;
            } catch (NoResultException e) {
                return false;
            }
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
        genreName = genreName.toLowerCase(Locale.ENGLISH);
        try (var entityManager = emf.createEntityManager()) {
            var query = entityManager.createNamedQuery("Genre.findByName", Genre.class);
            query.setParameter("name", genreName);
            try {
                return Optional.of(query.getSingleResult());
            } catch (NoResultException e) {
                var newGenre = new Genre(genreName);
                entityManager.getTransaction().begin();
                entityManager.persist(newGenre);
                entityManager.getTransaction().commit();
                return Optional.of(newGenre);
            }
        }
    }

    /**
     * Attempts to persist a list of genres by name, if a Genre already exists the already existing Genre is used.
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

