package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.AbstractModelObject;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static spotifybackup.storage.SpotifyObject.convertMarkets;
import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;

class SpotifyTrackRepository {
    private static final Function<EntityManager, BiConsumer<Track, SpotifyTrack>> setNotSimpleFields =
            entityManager -> (apiTrack, track) -> {
                if (apiTrack.getExternalIds().getExternalIds().containsKey("isrc")) {
                    track.setIsrcID(apiTrack.getExternalIds().getExternalIds().get("isrc"));
                }
                if (apiTrack.getExternalIds().getExternalIds().containsKey("ean")) {
                    track.setEanID(apiTrack.getExternalIds().getExternalIds().get("ean"));
                }
                if (apiTrack.getExternalIds().getExternalIds().containsKey("upc")) {
                    track.setUpcID(apiTrack.getExternalIds().getExternalIds().get("upc"));
                }
            };

    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyTrackRepository() {
        throw new ConstructorUsageException();
    }

    /**
     * Find SpotifyTrack by spotifyID field.
     * @param apiTrack TrackSimplified object generated by the spotify-web-api.
     * @return SpotifyTrack if apiArtist already exists in the database.
     */
    static Optional<SpotifyTrack> find(EntityManager entityManager, @NonNull TrackSimplified apiTrack) {
        return find(entityManager, apiTrack.getId());
    }

    /**
     * Find SpotifyTrack by spotifyID field.
     * @param apiTrack Track object generated by the spotify-web-api.
     * @return SpotifyTrack if apiArtist already exists in the database.
     */
    static Optional<SpotifyTrack> find(EntityManager entityManager, @NonNull Track apiTrack) {
        return find(entityManager, apiTrack.getId());
    }

    /**
     * Find SpotifyTrack by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return SpotifyTrack if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyTrack> find(EntityManager em, @NonNull String id) {
        if (id.isBlank() || em.find(SpotifyID.class, id) == null) return Optional.empty();
        var query = new CriteriaDefinition<>(em, SpotifyTrack.class) {};
        var root = query.from(SpotifyTrack.class);
        query.where(query.equal(root.get(SpotifyTrack_.SPOTIFY_ID).asString(), id));
        try {
            return Optional.of(em.createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to persist a TrackSimplified from the output of the spotify-web-api.
     * @param apiTrack TrackSimplified object generated by the spotify-web-api.
     * @return SpotifyTrack already in the database with matching Spotify ID or new SpotifyTrack if apiTrack has a new
     * Spotify ID.
     */
    static SpotifyTrack persist(EntityManager entityManager, @NonNull TrackSimplified apiTrack,
                                @NonNull SpotifyAlbum spotifyAlbum) {
        ensureTransactionActive.accept(entityManager);
        var optionalTrack = find(entityManager, apiTrack);
        if (optionalTrack.isPresent()) {
            return optionalTrack.get();
        } else {
            var newTrack = SpotifyTrack.builder()
                    .isSimplified(true)
                    .album(spotifyAlbum)
                    .spotifyID(new SpotifyID(apiTrack.getId()))
                    .discNumber(apiTrack.getDiscNumber())
                    .trackNumber(apiTrack.getTrackNumber())
                    .durationMs(apiTrack.getDurationMs())
                    .explicit(apiTrack.getIsExplicit())
                    .name(apiTrack.getName())
                    .availableMarkets(convertMarkets(apiTrack.getAvailableMarkets()))
                    .build();
            for (var simplifiedApiArtist : apiTrack.getArtists()) {
                newTrack.addArtist(SpotifyArtistRepository.persist(entityManager, simplifiedApiArtist));
            }
            entityManager.persist(newTrack);
            return newTrack;
        }
    }

    static SpotifyTrack persist(EntityManager entityManager, @NonNull AbstractModelObject apiTrack) {
        if (apiTrack instanceof Track t) return persist(entityManager, t);
        else throw new IllegalArgumentException("apiTrack should be of type Track here.");
    }

    /**
     * Attempts to persist a Track from the output of the spotify-web-api.
     * @param apiTrack Track object generated by the spotify-web-api.
     * @return SpotifyTrack already in the database with matching Spotify ID or new SpotifyTrack if apiTrack has a new
     * Spotify ID.
     */
    static SpotifyTrack persist(EntityManager entityManager, @NonNull Track apiTrack) {
        ensureTransactionActive.accept(entityManager);
        var optionalTrack = find(entityManager, apiTrack);
        if (optionalTrack.isPresent()) {
            if (!optionalTrack.get().getIsSimplified()) return optionalTrack.get();
            else {
                final var simpleTrack = optionalTrack.get();
                simpleTrack.setIsSimplified(false);
                setNotSimpleFields.apply(entityManager).accept(apiTrack, simpleTrack);
                entityManager.persist(simpleTrack);
                return simpleTrack;
            }
        } else {
            var newTrack = SpotifyTrack.builder()
                    .isSimplified(false)
                    .album(SpotifyAlbumRepository.persist(entityManager, apiTrack.getAlbum()))
                    .spotifyID(new SpotifyID(apiTrack.getId()))
                    .discNumber(apiTrack.getDiscNumber())
                    .trackNumber(apiTrack.getTrackNumber())
                    .durationMs(apiTrack.getDurationMs())
                    .explicit(apiTrack.getIsExplicit())
                    .name(apiTrack.getName())
                    .availableMarkets(convertMarkets(apiTrack.getAvailableMarkets()))
                    .build();
            for (var simplifiedApiArtist : apiTrack.getArtists()) {
                newTrack.addArtist(SpotifyArtistRepository.persist(entityManager, simplifiedApiArtist));
            }
            setNotSimpleFields.apply(entityManager).accept(apiTrack, newTrack);
            entityManager.persist(newTrack);
            return newTrack;

        }
    }
}
