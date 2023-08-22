package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

public class SpotifyAlbumRepository {
    static SpotifyAlbum persist(EntityManager entityManager, @NonNull AlbumSimplified apiAlbum) {

    }
}
