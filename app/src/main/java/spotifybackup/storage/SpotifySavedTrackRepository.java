package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
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

    static Optional<SpotifySavedTrack> find(EntityManager em, @NonNull SpotifyTrack track, @NonNull SpotifyUser user) {
        var query = em.createNamedQuery("SpotifySavedTrack.findByUserAndTrack", SpotifySavedTrack.class);
        query.setParameter("user", user);
        query.setParameter("track", track);
        try {
            return Optional.of(query.getSingleResult());
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
