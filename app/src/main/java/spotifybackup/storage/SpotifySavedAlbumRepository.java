package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.specification.SavedAlbum;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;
import static spotifybackup.storage.SpotifyObject.getSingleResultOptionally;

class SpotifySavedAlbumRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifySavedAlbumRepository() {
        throw new ConstructorUsageException();
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
        var album = SpotifyAlbumRepository.persist(em, apiAlbum.getAlbum());
        var spotifySavedAlbum = find(em, album, user);
        if (spotifySavedAlbum.isPresent()) {
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
