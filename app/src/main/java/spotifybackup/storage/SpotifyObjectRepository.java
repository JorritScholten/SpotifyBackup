package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Image;

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
                return em.find(SpotifyGenre.class, g.getId()) != null;
            } else if (spotifyObject instanceof SpotifyImage i) {
                return em.find(SpotifyImage.class, i.getId()) != null;
            } else if (spotifyObject instanceof SpotifyID spotifyID) {
                return em.find(SpotifyID.class, spotifyID.getId()) != null;
            } else if (spotifyObject instanceof SpotifyArtist a) {
                return em.find(SpotifyArtist.class, a.getId()) != null;
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
            em.getTransaction().begin();
            var persistedGenre = SpotifyGenreRepository.persist(em, genreName);
            em.getTransaction().commit();
            return persistedGenre;
        }
    }

    /**
     * Attempts to persist an array of genres by name, if a Genre already exists the already existing SpotifyGenre is
     * used.
     * @param genreNames an array of genre names as defined by Spotify.
     * @return Set of SpotifyGenre objects.
     */
    public Set<SpotifyGenre> persistGenres(@NonNull String[] genreNames) {
        try (var em = emf.createEntityManager()) {
            Set<SpotifyGenre> spotifyGenreSet = new HashSet<>();
            em.getTransaction().begin();
            for (var genreName : genreNames) {
                var genre = SpotifyGenreRepository.persist(em, genreName);
                genre.ifPresent(spotifyGenreSet::add);
            }
            em.getTransaction().commit();
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
    public boolean genreExists(@NonNull String genreName) {
        try (var em = emf.createEntityManager()) {
            return SpotifyGenreRepository.find(em, genreName).isPresent();
        }
    }

    /**
     * Check if SpotifyImage exists by Image.
     * @param image Image object generated by spotify-web-api.
     * @return true if image matches a record in the database.
     */
    public boolean imageExists(@NonNull Image image) {
        try (var em = emf.createEntityManager()) {
            if (image.getHeight() != null && image.getWidth() != null) {
                return SpotifyImageRepository.find(em, image).isPresent();
            } else {
                return SpotifyImageRepository.find(em, image.getUrl()).isPresent();
            }
        }
    }

    /**
     * Find SpotifyImage by its url field.
     * @param url source URL of the image.
     * @return SpotifyImage if url matches the url field in the table and not blank.
     */
    public boolean imageExists(@NonNull String url) {
        try (var em = emf.createEntityManager()) {
            return SpotifyImageRepository.find(em, url).isPresent();
        }
    }

    /**
     * Attempts to persist an Image, if it already exists returns already existing SpotifyImage.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if already in the database or images' url is not too long or empty.
     */
    public Optional<SpotifyImage> persistImage(@NonNull Image image) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            var persistedImage = SpotifyImageRepository.persist(em, image);
            em.getTransaction().commit();
            return persistedImage;
        }
    }

    /**
     * Attempts to persist an array of Image objects, if an Image is already stored the already existing SpotifyImage
     * is used.
     * @param images Array of Image objects generated by spotify-web-api.
     * @return Set of SpotifyImage objects.
     */
    public Set<SpotifyImage> persistImages(@NonNull Image[] images) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Set<SpotifyImage> imageSet = new HashSet<>();
            for (var image : images) {
                var optionalSpotifyImage = SpotifyImageRepository.persist(em, image);
                optionalSpotifyImage.ifPresent(imageSet::add);
            }
            em.getTransaction().commit();
            return imageSet;
        }
    }

    /**
     * Check if SpotifyID exists in persistence context by Spotify ID.
     * @param id Spotify ID to check.
     * @return true if Spotify ID exists in the database.
     */
    public boolean spotifyIDExists(@NonNull String id) {
        if (id.isBlank()) {
            return false;
        }
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(SpotifyID.class, id) != null;
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Attempts to persist a Spotify ID by its string representation, if it already exists returns already existing
     * SpotifyID.
     * @param id Spotify ID to persist (base-62 identifier as defined by Spotify).
     * @return SpotifyID if id is not blank.
     */
    public Optional<SpotifyID> persistSpotifyID(@NonNull String id) {
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
