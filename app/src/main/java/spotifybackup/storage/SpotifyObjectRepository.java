package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.NonNull;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class SpotifyObjectRepository {
    private final EntityManagerFactory emf;

    private SpotifyObjectRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Factory method to create SpotifyObjectRepository.
     * @param dbPath File path of database.
     */
    public static SpotifyObjectRepository factory(@NonNull File dbPath) {
        if (!(dbPath.isFile() && dbPath.exists() && dbPath.canRead() && dbPath.canWrite())) {
            throw new RuntimeException("Supplied filepath to database is unusable: " + dbPath);
        }
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "validate");
        DB_ACCESS.put("hibernate.show_sql", "false");
        DB_ACCESS.put("persistenceUnitName", "SpotifyObjects");
        DB_ACCESS.put("hibernate.hikari.dataSource.url", generateDataSourceUrl(dbPath));
        return new SpotifyObjectRepository(DB_ACCESS);
    }

    /**
     * Factory method to create SpotifyObjectRepository to new database, data is not persisted across multiple runs.
     * @apiNote Should only be used for testing.
     */
    public static SpotifyObjectRepository testFactory(boolean show_sql) {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", show_sql ? "true" : "false");
        DB_ACCESS.put("persistenceUnitName", "SpotifyObjectsTest");
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/spotifyObjectsTest;DB_CLOSE_DELAY=-1");
        return new SpotifyObjectRepository(DB_ACCESS);
    }

    private static String generateDataSourceUrl(File dbPath) {
        var dataSourceUrl = new StringBuilder("jdbc:h2:");
        dataSourceUrl.append(dbPath.getAbsolutePath());
        if (dbPath.getAbsolutePath().endsWith(".mv.db")) {
            dataSourceUrl.append(dbPath.getAbsolutePath(), 0, dbPath.getAbsolutePath().length() - 6);
        } else if (dbPath.getAbsolutePath().endsWith(".db")) {
            dataSourceUrl.append(dbPath.getAbsolutePath(), 0, dbPath.getAbsolutePath().length() - 3);
        } else {
            dataSourceUrl.append(dbPath.getAbsolutePath());
        }
        dataSourceUrl.append(";DB_CLOSE_DELAY=-1");
        return dataSourceUrl.toString();
    }

    public List<SpotifyGenre> findAllGenres() {
        try (var em = emf.createEntityManager()) {
            return SpotifyGenreRepository.findAll(em);
        }
    }

    /**
     * Checks if SpotifyObject exists in the database.
     * @param spotifyObject entity object to check.
     * @return true if spotifyObject exists.
     */
    public boolean exists(@NonNull SpotifyObject spotifyObject) {
        try (var em = emf.createEntityManager()) {
            if (spotifyObject instanceof SpotifyGenre g) {
                return em.contains(g);
            } else if (spotifyObject instanceof SpotifyImage i) {
                return em.contains(i);
            } else {
                return false;
            }
        }
    }

    /**
     * Attempts to persist a genre by its name, if it already exists returns already existing SpotifyGenre.
     * @param genreName name of genre as defined by Spotify.
     * @return SpotifyGenre if genreName is not blank.
     */
    public Optional<SpotifyGenre> persistGenre(@NonNull String genreName) {
        try (var em = emf.createEntityManager()) {
            return SpotifyGenreRepository.persist(em, genreName);
        }
    }

    /**
     * Attempts to persist an array of genres by name, if a Genre already exists the already existing SpotifyGenre is
     * used.
     * @param genreNames an array of genre names as defined by Spotify.
     * @return Set of SpotifyGenre objects.
     */
    public Set<SpotifyGenre> persistAllGenres(@NonNull String[] genreNames) {
        try (var em = emf.createEntityManager()) {
            Set<SpotifyGenre> spotifyGenreSet = new HashSet<>();
            for (var genreName : genreNames) {
                var genre = SpotifyGenreRepository.persist(em, genreName);
                genre.ifPresent(spotifyGenreSet::add);
            }
            return spotifyGenreSet;
        }
    }

    /**
     * Get count of genres in the database.
     * @return count of genres in the database.
     */
    public long countGenres() {
        try (var em = emf.createEntityManager()) {
            return (Long) em.createNamedQuery("SpotifyGenre.countBy").getSingleResult();
        }
    }

    /**
     * Check if SpotifyGenre exists by name.
     * @param genreName name of Genre.
     * @return true if genreName exists as a SpotifyGenre in the database.
     */
    public boolean genreExists(String genreName) {
        try (var em = emf.createEntityManager()) {
            return SpotifyGenreRepository.find(em, genreName).isPresent();
        }
    }
}
