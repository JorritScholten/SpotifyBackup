package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Image;

import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;

public class SpotifyImageRepository {
    private final EntityManagerFactory emf;

    /**
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public SpotifyImageRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Converts Image[] generated by spotify-web-api to Set<SpotifyImage>.
     */
    static Set<SpotifyImage> imageSetFactory(EntityManager entityManager, @NonNull Image[] images) {
        ensureTransactionActive.accept(entityManager);
        Set<SpotifyImage> imageSet = new HashSet<>();
        for (var image : images) {
            var optionalImage = persist(entityManager, image);
            optionalImage.ifPresent(imageSet::add);
        }
        return imageSet;
    }

    /**
     * Find SpotifyImage by its url, width and height fields.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if image matches a record in the database.
     */
    static Optional<SpotifyImage> find(EntityManager entityManager, @NonNull Image image) {
        var query = entityManager.createNamedQuery("SpotifyImage.findByUrlWH", SpotifyImage.class);
        query.setParameter("url", image.getUrl());
        query.setParameter("width", image.getWidth());
        query.setParameter("height", image.getHeight());
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Find SpotifyImage by its url field.
     * @param url source URL of the image.
     * @return SpotifyImage if url matches the url field in the table and not blank.
     */
    static Optional<SpotifyImage> find(EntityManager entityManager, @NonNull String url) {
        if (url.isBlank()) {
            return Optional.empty();
        }
        var query = entityManager.createNamedQuery("SpotifyImage.findByUrl", SpotifyImage.class);
        query.setParameter("url", url);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to persist an Image, if it already exists returns already existing SpotifyImage.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if already in the database or images' url is not too long or empty.
     */
    static Optional<SpotifyImage> persist(EntityManager entityManager, @NonNull Image image) {
        ensureTransactionActive.accept(entityManager);
        var optionalImage = find(entityManager, image);
        if (optionalImage.isPresent()) {
            return optionalImage;
        } else if (image.getUrl().isBlank() || image.getUrl().length() > 255) {
            // fail silently if url too long or empty
            return Optional.empty();
        } else {
            var newSpotifyImage = SpotifyImage.builder()
                    .url(image.getUrl())
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .build();
            entityManager.persist(newSpotifyImage);
            return Optional.of(newSpotifyImage);
        }
    }

    /**
     * Find SpotifyImage by its url field.
     * @param url source URL of the image.
     * @return SpotifyImage if url matches the url field in the table and not blank.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public Optional<SpotifyImage> find(@NonNull String url) {
        if (url.isBlank()) {
            return Optional.empty();
        }
        try (var entityManager = emf.createEntityManager()) {
            var query = entityManager.createNamedQuery("SpotifyImage.findByUrl", SpotifyImage.class);
            query.setParameter("url", url);
            try {
                return Optional.of(query.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        }
    }

    /**
     * Find SpotifyImage by its url, width and height fields.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if image matches a record in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public Optional<SpotifyImage> find(@NonNull Image image) {
        try (var entityManager = emf.createEntityManager()) {
            return find(entityManager, image);
        }
    }

    /**
     * Check if SpotifyImage exists by URL.
     * @param url URL to check.
     * @return true if url is present in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public boolean exists(@NonNull String url) {
        return find(url).isPresent();
    }

    /**
     * Check if SpotifyImage exists by Image.
     * @param image Image object generated by spotify-web-api.
     * @return true if image matches a record in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public boolean exists(@NonNull Image image) {
        return find(image).isPresent();
    }

    /**
     * Check if SpotifyImage exists in the database.
     * @param spotifyImage SpotifyImage to check.
     * @return true if spotifyImage exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public boolean exists(@NonNull SpotifyImage spotifyImage) {
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(SpotifyImage.class, spotifyImage.getId()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Attempts to persist an Image, if it already exists returns already existing SpotifyImage.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if already in the database or images' url is not too long or empty.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public Optional<SpotifyImage> persist(@NonNull Image image) {
        try (var entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            var spotifyImage = persist(entityManager, image);
            entityManager.getTransaction().commit();
            return spotifyImage;
        }
    }

    /**
     * Attempts to persist an array of Image objects, if an Image is already stored the already existing SpotifyImage is
     * used.
     * @param images Array of Image objects generated by spotify-web-api.
     * @return Set of SpotifyImage objects.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public Set<SpotifyImage> persistAll(@NonNull Image[] images) {
        Set<SpotifyImage> imageSet = new HashSet<>();
        for (var image : images) {
            var optionalSpotifyImage = persist(image);
            optionalSpotifyImage.ifPresent(imageSet::add);
        }
        return imageSet;
    }
}
