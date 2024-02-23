package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;
import static spotifybackup.storage.SpotifyObject.getSingleResultOptionally;

class SpotifySavedTrackRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifySavedTrackRepository() {
        throw new ConstructorUsageException();
    }

    static TypedQuery<SpotifySavedTrack> findNewestByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedTrack.class) {};
        var root = query.from(SpotifySavedTrack.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedTrack_.user), user),
                        query.isFalse(root.get(SpotifySavedTrack_.isRemoved)))
                .orderBy(query.desc(root.get(SpotifySavedTrack_.dateAdded)))
        );
    }

    static TypedQuery<Long> countByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedTrack.class) {};
        var root = query.from(SpotifySavedTrack.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedTrack_.user), user),
                        query.isFalse(root.get(SpotifySavedTrack_.isRemoved)))
                .createCountQuery()
        );
    }

    static TypedQuery<SpotifySavedTrack> findByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedTrack.class) {};
        var root = query.from(SpotifySavedTrack.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedTrack_.user), user),
                        query.isFalse(root.get(SpotifySavedTrack_.isRemoved)))
        );
    }

    static TypedQuery<SpotifySavedTrack> findRemovedByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedTrack.class) {};
        var root = query.from(SpotifySavedTrack.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedTrack_.user), user),
                        query.isTrue(root.get(SpotifySavedTrack_.isRemoved)))
        );
    }

    static TypedQuery<String> findTrackIdsByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, String.class) {};
        var root = query.from(SpotifySavedTrack.class);
        return em.createQuery(query
                .select(root.get(SpotifySavedTrack_.track).get(SpotifyTrack_.spotifyID).asString())
                .where(query.equal(root.get(SpotifySavedTrack_.user), user),
                        query.isFalse(root.get(SpotifySavedTrack_.isRemoved)))
        );
    }

    static Optional<SpotifySavedTrack> removeTrackFromLikedSongs(EntityManager em, @NonNull SpotifyTrack track, @NonNull SpotifyUser user) {
        ensureTransactionActive.accept(em);
        var optionalSavedTrack = find(em, track, user);
        if (optionalSavedTrack.isPresent()) {
            optionalSavedTrack.get().setIsRemoved(true);
            optionalSavedTrack.get().setDateRemoved(ZonedDateTime.now(UTC));
            em.persist(optionalSavedTrack.get());
        }
        return optionalSavedTrack;
    }

    static Optional<SpotifySavedTrack> find(EntityManager em, @NonNull SpotifyTrack track, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedTrack.class) {
        };
        var root = query.from(SpotifySavedTrack.class);
        query.where(query.equal(root.get(SpotifySavedTrack_.user), user),
                query.equal(root.get(SpotifySavedTrack_.track), track));
        return getSingleResultOptionally(em, query);
    }

    static SpotifySavedTrack persist(EntityManager em, @NonNull SavedTrack apiTrack, @NonNull SpotifyUser user) {
        ensureTransactionActive.accept(em);
        var track = SpotifyTrackRepository.persist(em, apiTrack.getTrack());
        var optionalSavedTrack = find(em, track, user);
        if (optionalSavedTrack.isPresent()) {
            if(optionalSavedTrack.get().getIsRemoved()){
                optionalSavedTrack.get().setIsRemoved(false);
                optionalSavedTrack.get().setDateRemoved(null);
                optionalSavedTrack.get().setDateAdded(apiTrack.getAddedAt().toInstant().atZone(UTC));
                em.persist(optionalSavedTrack.get());
            }
            return optionalSavedTrack.get();
        } else {
            var newSpotifySavedTrack = SpotifySavedTrack.builder()
                    .track(track)
                    .user(user)
                    .dateAdded(apiTrack.getAddedAt().toInstant().atZone(UTC))
                    .build();
            em.persist(newSpotifySavedTrack);
            return newSpotifySavedTrack;
        }
    }
}
