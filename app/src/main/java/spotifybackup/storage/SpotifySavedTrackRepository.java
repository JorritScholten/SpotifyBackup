package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.List;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;

class SpotifySavedTrackRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifySavedTrackRepository() {
        throw new ConstructorUsageException();
    }

    static List<SpotifySavedTrack> persist(EntityManager em, @NonNull SavedTrack[] tracks, @NonNull SpotifyUser user) {
        ensureTransactionActive.accept(em);
        return null;
    }
}
