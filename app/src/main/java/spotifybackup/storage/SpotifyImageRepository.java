package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Image;

import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class SpotifyImageRepository {
    private final EntityManagerFactory emf;

    public SpotifyImageRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Find SpotifyImage by its url field.
     * @param url source URL of the image.
     * @return SpotifyImage if url matches the url field in the table and not blank.
     */
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
     * Find SpotifyImage by its url field.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if image matches a record in the database.
     */
    public Optional<SpotifyImage> find(@NonNull Image image) {
        try (var entityManager = emf.createEntityManager()) {
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
    }

    /**
     * Check if SpotifyImage exists by URL.
     * @param url URL to check.
     * @return true if url is present in the database.
     */
    public boolean exists(@NonNull String url) {
        return find(url).isPresent();
    }

    /**
     * Check if SpotifyImage exists by Image.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if image matches a record in the database.
     */
    public boolean exists(@NonNull Image image) {
        return find(image).isPresent();
    }

    /**
     * Check if SpotifyImage exists in the database.
     * @param spotifyImage SpotifyImage to check.
     * @return true if spotifyImage exists in the database.
     */
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
     */
    public Optional<SpotifyImage> persist(@NonNull Image image) {
        try (var entityManager = emf.createEntityManager()) {
            var optionalImage = find(image);
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
                entityManager.getTransaction().begin();
                entityManager.persist(newSpotifyImage);
                entityManager.getTransaction().commit();
                return Optional.of(newSpotifyImage);
            }
        }
    }

    /**
     * Attempts to persist an array of Image objects, if an Image is already stored the already existing SpotifyImage
     * is used.
     * @param images Array of Image objects generated by spotify-web-api.
     * @return Set of SpotifyImage objects.
     */
    public Set<SpotifyImage> persistAll(@NonNull Image[] images) {
        Set<SpotifyImage> imageSet = new HashSet<>();
        for (var image : images) {
            var optionalSpotifyImage = persist(image);
            optionalSpotifyImage.ifPresent(imageSet::add);
        }
        return imageSet;
    }
}