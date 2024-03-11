package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaDelete;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;

class SpotifyPlaylistItemRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyPlaylistItemRepository() {
        throw new ConstructorUsageException();
    }

    public static List<SpotifyPlaylistItem> findByPlaylist(EntityManager em, SpotifyPlaylist playlist) {
        var query = new CriteriaDefinition<>(em, SpotifyPlaylistItem.class) {};
        var root = query.from(SpotifyPlaylistItem.class);
        query.where(query.equal(root.get(SpotifyPlaylistItem_.playlist), playlist));
        return em.createQuery(query).getResultList();
    }

    /**
     * Find SpotifyPlaylistItems by track, playlist and user it was added by.
     * @param apiPlaylistItem PlaylistTrack object generated by the spotify-web-api.
     * @return List of SpotifyPlaylistItems matching apiPlaylistItem.
     */
    static List<SpotifyPlaylistItem> find(EntityManager entityManager, @NonNull PlaylistTrack apiPlaylistItem,
                                          @NonNull SpotifyPlaylist playlist) {
        if (apiPlaylistItem.getTrack().getType() != ModelObjectType.TRACK) throw new IllegalArgumentException(
                apiPlaylistItem.getTrack().getType() + " storage not implemented yet."
        );
        var optionalTrack = SpotifyTrackRepository.find(entityManager, apiPlaylistItem.getTrack().getId());
        if (optionalTrack.isEmpty()) {
            return Collections.emptyList();
        } else {
            if (apiPlaylistItem.getAddedBy() == null)
                return find(entityManager, optionalTrack.get(), playlist);
            else
                return find(entityManager, optionalTrack.get(), playlist,
                        SpotifyUserRepository.persist(entityManager, apiPlaylistItem.getAddedBy()));
        }
    }

    /**
     * Find SpotifyPlaylistItems by matching SpotifyTrack and SpotifyPlaylist.
     * @return List of SpotifyPlaylistItems matching parameters.
     */
    static List<SpotifyPlaylistItem> find(EntityManager em, @NonNull SpotifyTrack track,
                                          @NonNull SpotifyPlaylist playlist) {
        var query = new CriteriaDefinition<>(em, SpotifyPlaylistItem.class) {};
        var root = query.from(SpotifyPlaylistItem.class);
        query.where(query.equal(root.get(SpotifyPlaylistItem_.playlist), playlist),
                query.equal(root.get(SpotifyPlaylistItem_.track), track));
        return em.createQuery(query).getResultList();
    }

    /**
     * Find SpotifyPlaylistItems by matching SpotifyTrack, SpotifyPlaylist and which SpotifyUser added the track.
     * @return List of SpotifyPlaylistItems matching parameters.
     */
    static List<SpotifyPlaylistItem> find(EntityManager em, @NonNull SpotifyTrack track,
                                          @NonNull SpotifyPlaylist playlist, @NonNull SpotifyUser addedBy) {
        var query = new CriteriaDefinition<>(em, SpotifyPlaylistItem.class) {};
        var root = query.from(SpotifyPlaylistItem.class);
        query.where(query.equal(root.get(SpotifyPlaylistItem_.playlist), playlist),
                query.equal(root.get(SpotifyPlaylistItem_.track), track),
                query.equal(root.get(SpotifyPlaylistItem_.addedBy), addedBy));
        return em.createQuery(query).getResultList();
    }

    /**
     * Attempts to persist a PlaylistTrack from the output of the spotify-web-api.
     * @param apiPlaylistItem PlaylistTrack object generated by the spotify-web-api.
     * @param playlist        SpotifyPlaylist to which this item belongs.
     * @return SpotifyPlaylistItem already in the database with matching Spotify ID or new SpotifyPlaylistItem if
     * apiPlaylistItem has a new Spotify ID.
     */
    static SpotifyPlaylistItem persist(EntityManager entityManager, @NonNull PlaylistTrack apiPlaylistItem,
                                       @NonNull SpotifyPlaylist playlist) {
        ensureTransactionActive.accept(entityManager);
        var playlistItemList = find(entityManager, apiPlaylistItem, playlist);
        if (playlistItemList.size() == 1) {
            return playlistItemList.getFirst();
        } else if (!playlistItemList.isEmpty()) {
            throw new IllegalArgumentException("Duplicate playlist item handling not implemented yet.");
        } else {
            if (apiPlaylistItem.getTrack().getType() != ModelObjectType.TRACK) throw new IllegalArgumentException(
                    apiPlaylistItem.getTrack().getType() + " storage not implemented yet."
            );
            var newItem = SpotifyPlaylistItem.builder()
                    .track(SpotifyTrackRepository.persist(entityManager, (Track) apiPlaylistItem.getTrack()))
                    .playlist(playlist)
                    .addedBy(apiPlaylistItem.getAddedBy() == null || apiPlaylistItem.getAddedBy().getId().isBlank() ?
                            null : SpotifyUserRepository.persist(entityManager, apiPlaylistItem.getAddedBy()))
                    .dateAdded(apiPlaylistItem.getAddedAt() == null ? null :
                            ZonedDateTime.ofInstant(apiPlaylistItem.getAddedAt().toInstant(), ZoneOffset.UTC))
                    .build();
            entityManager.persist(newItem);
            return newItem;
        }
    }

    public static void deleteByPlaylist(EntityManager em, SpotifyPlaylist playlist) {
        ensureTransactionActive.accept(em);
        var cb = em.getCriteriaBuilder();
        var delete = cb.createCriteriaDelete(SpotifyPlaylistItem.class);
        var root = delete.from(SpotifyPlaylistItem.class);
        delete.where(cb.equal(root.get(SpotifyPlaylistItem_.playlist), playlist));
        em.createQuery(delete).executeUpdate();
    }
}
