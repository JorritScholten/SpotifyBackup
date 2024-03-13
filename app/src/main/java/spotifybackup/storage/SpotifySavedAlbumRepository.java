package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.specification.SavedAlbum;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;
import static spotifybackup.storage.SpotifyObject.getSingleResultOptionally;

class SpotifySavedAlbumRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifySavedAlbumRepository() {
        throw new ConstructorUsageException();
    }

    static TypedQuery<SpotifySavedAlbum> findNewestByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedAlbum.class) {};
        var root = query.from(SpotifySavedAlbum.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedAlbum_.user), user),
                        query.isFalse(root.get(SpotifySavedAlbum_.isRemoved)))
                .orderBy(query.desc(root.get(SpotifySavedAlbum_.dateAdded)))
        );
    }

    static TypedQuery<Long> countByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedAlbum.class) {};
        var root = query.from(SpotifySavedAlbum.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedAlbum_.user), user),
                        query.isFalse(root.get(SpotifySavedAlbum_.isRemoved)))
                .createCountQuery()
        );
    }

    static TypedQuery<SpotifySavedAlbum> findByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedAlbum.class) {};
        var root = query.from(SpotifySavedAlbum.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedAlbum_.user), user),
                        query.isFalse(root.get(SpotifySavedAlbum_.isRemoved)))
        );
    }

    static TypedQuery<SpotifySavedAlbum> findRemovedByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedAlbum.class) {};
        var root = query.from(SpotifySavedAlbum.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedAlbum_.user), user),
                        query.isTrue(root.get(SpotifySavedAlbum_.isRemoved)))
        );
    }

    static TypedQuery<String> findAlbumIdsByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, String.class) {};
        var root = query.from(SpotifySavedAlbum.class);
        return em.createQuery(query
                .select(root.get(SpotifySavedAlbum_.album).get(SpotifyAlbum_.spotifyID).asString())
                .where(query.equal(root.get(SpotifySavedAlbum_.user), user),
                        query.isFalse(root.get(SpotifySavedAlbum_.isRemoved)))
        );
    }

    static Optional<SpotifySavedAlbum> removeAlbumFromSavedAlbums(EntityManager em, @NonNull SpotifyAlbum album, @NonNull SpotifyUser user) {
        ensureTransactionActive.accept(em);
        var spotifySavedAlbum = find(em, album, user);
        if (spotifySavedAlbum.isPresent()) {
            spotifySavedAlbum.get().setIsRemoved(true);
            spotifySavedAlbum.get().setDateRemoved(ZonedDateTime.now(UTC));
            em.persist(spotifySavedAlbum.get());
        }
        return spotifySavedAlbum;
    }

    static Optional<SpotifySavedAlbum> find(EntityManager em, @NonNull SpotifyAlbum album, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedAlbum.class) {};
        var root = query.from(SpotifySavedAlbum.class);
        query.where(query.equal(root.get(SpotifySavedAlbum_.user), user),
                query.equal(root.get(SpotifySavedAlbum_.album), album));
        return getSingleResultOptionally(em, query);
    }

    static SpotifySavedAlbum persist(EntityManager em, @NonNull SavedAlbum apiAlbum, @NonNull SpotifyUser user) {
        ensureTransactionActive.accept(em);
        var album = SpotifyAlbumRepository.persist(em, apiAlbum.getAlbum(), true, ImageSelection.ALL);
        var spotifySavedAlbum = find(em, album, user);
        if (spotifySavedAlbum.isPresent()) {
            if (spotifySavedAlbum.get().getIsRemoved()) {
                // this only occurs if an album is re-added to liked songs
                spotifySavedAlbum.get().setIsRemoved(false);
                spotifySavedAlbum.get().setDateRemoved(null);
                spotifySavedAlbum.get().setDateAdded(apiAlbum.getAddedAt().toInstant().atZone(UTC));
                em.persist(spotifySavedAlbum.get());
            }
            return spotifySavedAlbum.get();
        } else {
            var newSpotifySavedAlbum = SpotifySavedAlbum.builder()
                    .album(album)
                    .user(user)
                    .dateAdded(apiAlbum.getAddedAt().toInstant().atZone(UTC))
                    .build();
            em.persist(newSpotifySavedAlbum);
            return newSpotifySavedAlbum;
        }
    }
}
