package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.AbstractModelObject;
import se.michaelthelin.spotify.model_objects.specification.Image;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;

class SpotifyImageRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyImageRepository() {
        throw new ConstructorUsageException();
    }

    /**
     * Converts Image[] generated by spotify-web-api to Set<SpotifyImage>.
     */
    static Set<SpotifyImage> imageSetFactory(EntityManager entityManager, @NonNull Image[] images) {
        ensureTransactionActive.accept(entityManager);
        Set<SpotifyImage> imageSet = new HashSet<>();
        for (var image : images) {
            imageSet.add(persist(entityManager, image));
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

    static SpotifyImage persist(EntityManager entityManager, @NonNull AbstractModelObject image) {
        if(image instanceof Image i) return persist(entityManager, i);
        else throw new IllegalArgumentException("image should be of type Image here.");
    }

    /**
     * Attempts to persist an Image, if it already exists returns already existing SpotifyImage.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if already in the database or images' url is not too long or empty.
     */
    static SpotifyImage persist(EntityManager entityManager, @NonNull Image image) {
        ensureTransactionActive.accept(entityManager);
        var optionalImage = find(entityManager, image);
        if (optionalImage.isPresent()) {
            return optionalImage.get();
        } else if (image.getUrl().isBlank() || image.getUrl().length() > 255) {
            throw new IllegalArgumentException("URL is too long or blank");
        } else {
            var newSpotifyImage = SpotifyImage.builder()
                    .url(image.getUrl())
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .build();
            entityManager.persist(newSpotifyImage);
            return newSpotifyImage;
        }
    }
}
