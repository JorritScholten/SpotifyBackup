package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;
import static spotifybackup.storage.SpotifyObject.getSingleResultOptionally;

class SpotifyPlaylistRepository {
    private static final Function<EntityManager, BiConsumer<Playlist, SpotifyPlaylist>> setNotSimpleFields =
            entityManager -> (apiPlaylist, playlist) -> {
                if (apiPlaylist.getTracks().getItems() != null) {
                    for (var item : apiPlaylist.getTracks().getItems()) {
                        playlist.addPlaylistItem(SpotifyPlaylistItemRepository.persist(entityManager, item, playlist));
                    }
                }
            };

    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyPlaylistRepository() {
        throw new ConstructorUsageException();
    }

    static List<String> findAllSpotifyIdsOfSimplified(EntityManager em) {
        var query = new CriteriaDefinition<>(em, String.class) {};
        var root = query.from(SpotifyPlaylist.class);
        query.select(root.get(SpotifyPlaylist_.spotifyID).asString())
                .where(query.isTrue(root.get(SpotifyPlaylist_.isSimplified)));
        return em.createQuery(query).getResultList();
    }

    static List<SpotifyPlaylist> findAll(EntityManager em) {
        var query = new CriteriaDefinition<>(em, SpotifyPlaylist.class) {};
        query.from(SpotifyPlaylist.class);
        return em.createQuery(query).getResultList();
    }

    /**
     * Find SpotifyPlaylist by spotifyID field.
     * @param apiPlaylist Playlist object generated by the spotify-web-api.
     * @return SpotifyPlaylist if apiPlaylist already exists in the database.
     */
    static Optional<SpotifyPlaylist> find(EntityManager entityManager, @NonNull Playlist apiPlaylist) {
        return find(entityManager, apiPlaylist.getId());
    }

    /**
     * Find SpotifyPlaylist by spotifyID field.
     * @param apiPlaylist PlaylistSimplified object generated by the spotify-web-api.
     * @return SpotifyPlaylist if apiPlaylist already exists in the database.
     */
    static Optional<SpotifyPlaylist> find(EntityManager entityManager, @NonNull PlaylistSimplified apiPlaylist) {
        return find(entityManager, apiPlaylist.getId());
    }

    /**
     * Find SpotifyPlaylist by Spotify ID value.
     * @param id SpotifyID of playlist.
     * @return SpotifyPlaylist if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyPlaylist> find(EntityManager em, @NonNull SpotifyID id) {
        return find(em, id.getId());
    }

    /**
     * Find SpotifyPlaylist by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return SpotifyPlaylist if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyPlaylist> find(EntityManager em, @NonNull String id) {
        if (id.isBlank() || em.find(SpotifyID.class, id) == null) return Optional.empty();
        var query = new CriteriaDefinition<>(em, SpotifyPlaylist.class) {};
        var root = query.from(SpotifyPlaylist.class);
        query.where(query.equal(root.get(SpotifyPlaylist_.spotifyID).asString(), id));
        return getSingleResultOptionally(em, query);
    }

    /**
     * Attempts to persist a PlaylistSimplified from the output of the spotify-web-api.
     * @param apiPlaylist PlaylistSimplified object generated by the spotify-web-api.
     * @return SpotifyPlaylist already in the database with matching Spotify ID or new SpotifyPlaylist if apiPlaylist
     * has a new Spotify ID.
     */
    static SpotifyPlaylist persist(EntityManager entityManager, @NonNull PlaylistSimplified apiPlaylist) {
        ensureTransactionActive.accept(entityManager);
        var optionalPlaylist = find(entityManager, apiPlaylist);
        if (optionalPlaylist.isPresent()) {
            return optionalPlaylist.get();
        } else {
            var newPlaylist = SpotifyPlaylist.builder()
                    .isSimplified(true)
                    .spotifyID(new SpotifyID(apiPlaylist.getId()))
                    .owner(SpotifyUserRepository.persist(entityManager, apiPlaylist.getOwner()))
                    .name(apiPlaylist.getName())
                    .isCollaborative(apiPlaylist.getIsCollaborative())
                    .isPublic(apiPlaylist.getIsPublicAccess())
                    .snapshotId(apiPlaylist.getSnapshotId())
                    .build();
            entityManager.persist(newPlaylist);
            return newPlaylist;
        }
    }

    /**
     * Attempts to persist a Playlist from the output of the spotify-web-api.
     * @param apiPlaylist Playlist object generated by the spotify-web-api.
     * @return SpotifyPlaylist already in the database with matching Spotify ID or new SpotifyPlaylist if apiPlaylist
     * has a new Spotify ID.
     */
    static SpotifyPlaylist persist(EntityManager entityManager, @NonNull Playlist apiPlaylist) {
        ensureTransactionActive.accept(entityManager);
        var optionalPlaylist = find(entityManager, apiPlaylist);
        if (optionalPlaylist.isPresent()) {
            if (!optionalPlaylist.get().getIsSimplified()) return optionalPlaylist.get();
            else {
                var simplePlaylist = optionalPlaylist.get();
                simplePlaylist.setIsSimplified(false);
                simplePlaylist.setDescription(apiPlaylist.getDescription());
                setNotSimpleFields.apply(entityManager).accept(apiPlaylist, simplePlaylist);
                entityManager.persist(simplePlaylist);
                return simplePlaylist;
            }
        } else {
            var newPlaylist = SpotifyPlaylist.builder()
                    .isSimplified(false)
                    .spotifyID(new SpotifyID(apiPlaylist.getId()))
                    .owner(SpotifyUserRepository.persist(entityManager, apiPlaylist.getOwner()))
                    .name(apiPlaylist.getName())
                    .description(apiPlaylist.getDescription())
                    .isCollaborative(apiPlaylist.getIsCollaborative())
                    .isPublic(apiPlaylist.getIsPublicAccess())
                    .snapshotId(apiPlaylist.getSnapshotId())
                    .build();
            entityManager.persist(newPlaylist);
            setNotSimpleFields.apply(entityManager).accept(apiPlaylist, newPlaylist);
            entityManager.persist(newPlaylist);
            return newPlaylist;
        }
    }

    static Optional<SpotifyPlaylist> update(EntityManager em, Playlist apiPlaylist) {
        ensureTransactionActive.accept(em);
        var optionalPlaylist = find(em, apiPlaylist);
        if (optionalPlaylist.isEmpty()) return optionalPlaylist;
        else {
            var playlist = optionalPlaylist.get();
            playlist.setName(apiPlaylist.getName());
            playlist.setDescription(apiPlaylist.getDescription());
            playlist.setIsCollaborative(apiPlaylist.getIsCollaborative());
            playlist.setIsPublic(apiPlaylist.getIsPublicAccess());
            playlist.setSnapshotId(apiPlaylist.getSnapshotId());
            em.persist(playlist);
            return Optional.of(playlist);
        }
    }
}
