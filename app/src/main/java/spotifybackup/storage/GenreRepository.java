package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class GenreRepository {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("testdb");

    public Genre find(long id) {
        try (var entityManager = emf.createEntityManager()) {
            Genre genre = entityManager.find(Genre.class, id);
            return genre;
        }
    }

    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("Genre.countBy").getSingleResult();
        }
    }
}
