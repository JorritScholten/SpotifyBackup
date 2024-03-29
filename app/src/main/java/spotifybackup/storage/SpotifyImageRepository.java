package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.specification.Image;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.*;
import java.util.stream.Collectors;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;
import static spotifybackup.storage.SpotifyObject.getSingleResultOptionally;

class SpotifyImageRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyImageRepository() {
        throw new ConstructorUsageException();
    }

    /**
     * Converts Image[] generated by spotify-web-api to Set<SpotifyImage>.
     * @deprecated Use method with integrated selection method.
     */
    @Deprecated
    static Set<SpotifyImage> imageSetFactory(EntityManager entityManager, Image[] images) {
        if (images == null) return Collections.emptySet();
        ensureTransactionActive.accept(entityManager);
        Set<SpotifyImage> imageSet = new HashSet<>();
        for (var image : images) {
            imageSet.add(persist(entityManager, image));
        }
        return imageSet;
    }

    /**
     * Converts Image[] generated by spotify-web-api to Set<SpotifyImage>.
     * @param selection Limit which images are saved to selection.
     */
    static Set<SpotifyImage> imageSetFactory(EntityManager entityManager, Image[] images, ImageSelection selection) {
        if (images == null || images.length == 0) return Collections.emptySet();
        ensureTransactionActive.accept(entityManager);
        return switch (selection) {
            case ALL -> Arrays.stream(images).map(i -> persist(entityManager, i)).collect(Collectors.toSet());
            case NONE -> Collections.emptySet();
            case ONLY_LARGEST -> Set.of(persist(entityManager, ImageSelection.findLargest(images)));
            case ONLY_SMALLEST -> Set.of(persist(entityManager, ImageSelection.findSmallest(images)));
        };
    }

    /**
     * Find SpotifyImage by its url, width and height fields.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if image matches a record in the database.
     */
    static Optional<SpotifyImage> find(EntityManager em, @NonNull Image image) {
        if (image.getHeight() == null || image.getWidth() == null) return find(em, image.getUrl());
        var query = new CriteriaDefinition<>(em, SpotifyImage.class) {};
        var root = query.from(SpotifyImage.class);
        query.where(query.equal(root.get(SpotifyImage_.url), image.getUrl()),
                query.equal(root.get(SpotifyImage_.width), image.getWidth()),
                query.equal(root.get(SpotifyImage_.height), image.getHeight()));
        return getSingleResultOptionally(em, query);
    }

    /**
     * Find SpotifyImage by its url field.
     * @param url source URL of the image.
     * @return SpotifyImage if url matches the url field in the table and not blank.
     */
    static Optional<SpotifyImage> find(EntityManager em, @NonNull String url) {
        if (url.isBlank()) return Optional.empty();
        var query = new CriteriaDefinition<>(em, SpotifyImage.class) {};
        var root = query.from(SpotifyImage.class);
        query.where(query.equal(root.get(SpotifyImage_.url), url));
        return getSingleResultOptionally(em, query);
    }

    /**
     * Attempts to persist an Image, if it already exists returns already existing SpotifyImage.
     * @param apiImage Image object generated by spotify-web-api.
     * @return SpotifyImage if already in the database or images' url is not too long or empty.
     */
    static SpotifyImage persist(EntityManager entityManager, @NonNull Image apiImage) {
        ensureTransactionActive.accept(entityManager);
        var optionalImage = find(entityManager, apiImage);
        if (optionalImage.isPresent()) {
            return optionalImage.get();
        } else if (apiImage.getUrl().isBlank() || apiImage.getUrl().length() > 255) {
            throw new IllegalArgumentException("URL is too long or blank");
        } else {
            var newSpotifyImage = SpotifyImage.builder()
                    .url(apiImage.getUrl())
                    .width(apiImage.getWidth())
                    .height(apiImage.getHeight())
                    .build();
            entityManager.persist(newSpotifyImage);
            return newSpotifyImage;
        }
    }
}
