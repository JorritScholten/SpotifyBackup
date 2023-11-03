package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.AbstractModelObject;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;

class SpotifyTrackRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyTrackRepository() {
        throw new ConstructorUsageException();
    }

    /**
     * @param markets Array of CountryCode objects.
     * @return Array of Strings containing ISO 3166-1 alpha-2 market codes.
     */
    private static String[] convertMarkets(CountryCode[] markets) {
        Set<String> stringifiedMarkets = new HashSet<>();
        for (var market : markets) {
            stringifiedMarkets.add(market.getAlpha2());
        }
        return stringifiedMarkets.toArray(String[]::new);
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
    static Optional<SpotifyTrack> find(EntityManager entityManager, @NonNull String id) {
        if (id.isBlank() || entityManager.find(SpotifyID.class, id) == null) {
            return Optional.empty();
        }
        var query = entityManager.createNamedQuery("SpotifyTrack.findBySpotifyID", SpotifyTrack.class);
        query.setParameter("spotifyID", entityManager.find(SpotifyID.class, id));
        try {
            return Optional.of(query.getSingleResult());
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
    static SpotifyTrack persist(EntityManager entityManager, @NonNull TrackSimplified apiTrack, @NonNull SpotifyAlbum spotifyAlbum) {
        ensureTransactionActive.accept(entityManager);
        var optionalTrack = find(entityManager, apiTrack);
        if (optionalTrack.isPresent()) {
            return optionalTrack.get();
        } else {
            var newTrackBuilder = SpotifyTrack.builder();
            newTrackBuilder.isSimplified(true);
            newTrackBuilder.spotifyAlbum(spotifyAlbum);
            newTrackBuilder.spotifyID(new SpotifyID(apiTrack.getId()));
            newTrackBuilder.discNumber(apiTrack.getDiscNumber());
            newTrackBuilder.trackNumber(apiTrack.getTrackNumber());
            newTrackBuilder.durationMs(apiTrack.getDurationMs());
            newTrackBuilder.explicit(apiTrack.getIsExplicit());
            newTrackBuilder.name(apiTrack.getName());
            if (apiTrack.getAvailableMarkets().length > 0) {
                newTrackBuilder.availableMarkets(convertMarkets(apiTrack.getAvailableMarkets()));
            }
            var newTrack = newTrackBuilder.build();
            for (var simplifiedApiArtist : apiTrack.getArtists()) {
                newTrack.addArtist(SpotifyArtistRepository.persist(entityManager, simplifiedApiArtist));
            }
            entityManager.persist(newTrack);
            return newTrack;
        }
    }

    static SpotifyTrack persist(EntityManager entityManager, @NonNull AbstractModelObject apiTrack) {
        if(apiTrack instanceof Track t) return persist(entityManager, t);
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
            return optionalTrack.get();
        } else {
            var newTrackBuilder = SpotifyTrack.builder();
            newTrackBuilder.isSimplified(false);
            newTrackBuilder.spotifyAlbum(SpotifyAlbumRepository.persist(entityManager, apiTrack.getAlbum()));
            newTrackBuilder.spotifyID(new SpotifyID(apiTrack.getId()));
            newTrackBuilder.discNumber(apiTrack.getDiscNumber());
            newTrackBuilder.trackNumber(apiTrack.getTrackNumber());
            newTrackBuilder.durationMs(apiTrack.getDurationMs());
            newTrackBuilder.explicit(apiTrack.getIsExplicit());
            if (apiTrack.getExternalIds().getExternalIds().containsKey("isrc")) {
                newTrackBuilder.isrcID(apiTrack.getExternalIds().getExternalIds().get("isrc"));
            }
            if (apiTrack.getExternalIds().getExternalIds().containsKey("ean")) {
                newTrackBuilder.eanID(apiTrack.getExternalIds().getExternalIds().get("ean"));
            }
            if (apiTrack.getExternalIds().getExternalIds().containsKey("upc")) {
                newTrackBuilder.upcID(apiTrack.getExternalIds().getExternalIds().get("upc"));
            }
            newTrackBuilder.name(apiTrack.getName());
            if (apiTrack.getAvailableMarkets().length > 0) {
                newTrackBuilder.availableMarkets(convertMarkets(apiTrack.getAvailableMarkets()));
            }
            var newTrack = newTrackBuilder.build();
            for (var simplifiedApiArtist : apiTrack.getArtists()) {
                newTrack.addArtist(SpotifyArtistRepository.persist(entityManager, simplifiedApiArtist));
            }
            entityManager.persist(newTrack);
            return newTrack;

        }
    }
}
