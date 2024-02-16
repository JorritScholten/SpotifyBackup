package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.time.ZoneOffset;
import java.util.Optional;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;

class SpotifySavedTrackRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifySavedTrackRepository() {
        throw new ConstructorUsageException();
    }

    static TypedQuery<SpotifySavedTrack> findNewestByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedTrack.class) {};
        var root = query.from(SpotifySavedTrack.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedTrack_.user), user))
                .orderBy(query.desc(root.get(SpotifySavedTrack_.dateAdded)))
        );
    }

    static TypedQuery<Long> countByUser(EntityManager em, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedTrack.class) {};
        var root = query.from(SpotifySavedTrack.class);
        return em.createQuery(query
                .where(query.equal(root.get(SpotifySavedTrack_.user), user))
                .createCountQuery()
        );
    }

    static TypedQuery<SpotifySavedTrack> findByUser(EntityManager em, @NonNull SpotifyUser user) {
        return null;
    }

    static TypedQuery<String> findTrackIdsByUser(EntityManager em, @NonNull SpotifyUser user) {
        return null;
    }

    static Optional<SpotifySavedTrack> find(EntityManager em, @NonNull SpotifyTrack track, @NonNull SpotifyUser user) {
        var query = new CriteriaDefinition<>(em, SpotifySavedTrack.class) {};
        var root = query.from(SpotifySavedTrack.class);
        query.where(query.equal(root.get(SpotifySavedTrack_.user), user),
                query.equal(root.get(SpotifySavedTrack_.track), track));
        try {
            return Optional.of(em.createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    static SpotifySavedTrack persist(EntityManager em, @NonNull SavedTrack apiTrack, @NonNull SpotifyUser user) {
        ensureTransactionActive.accept(em);
        var track = SpotifyTrackRepository.persist(em, apiTrack.getTrack());
        var optionalSavedTrack = find(em, track, user);
        if (optionalSavedTrack.isPresent()) {
            return optionalSavedTrack.get();
        } else {
            var newSpotifySavedTrack = SpotifySavedTrack.builder()
                    .track(track)
                    .user(user)
                    .dateAdded(apiTrack.getAddedAt().toInstant().atZone(ZoneOffset.UTC))
                    .build();
            em.persist(newSpotifySavedTrack);
            return newSpotifySavedTrack;
        }
    }
}
