package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.*;

public class SpotifyTrackRepository {
    private final EntityManagerFactory emf;

    /**
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public SpotifyTrackRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
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
        if (!entityManager.getTransaction().isActive()) {
            throw new RuntimeException("Method will only work from within an active transaction.");
        }
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
            newTrackBuilder.duration_ms(apiTrack.getDurationMs());
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

    /**
     * Attempts to persist a Track from the output of the spotify-web-api.
     * @param apiTrack Track object generated by the spotify-web-api.
     * @return SpotifyTrack already in the database with matching Spotify ID or new SpotifyTrack if apiTrack has a new
     * Spotify ID.
     */
    static SpotifyTrack persist(EntityManager entityManager, @NonNull Track apiTrack) {
        if (!entityManager.getTransaction().isActive()) {
            throw new RuntimeException("Method will only work from within an active transaction.");
        }
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
            newTrackBuilder.duration_ms(apiTrack.getDurationMs());
            newTrackBuilder.explicit(apiTrack.getIsExplicit());
            if (apiTrack.getExternalIds().getExternalIds().containsKey("isrc")) {
                newTrackBuilder.isrcID(apiTrack.getExternalIds().getExternalIds().get("isrc"));
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

    /**
     * Get count of tracks in the database.
     * @return count of tracks in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("SpotifyTrack.countBy").getSingleResult();
        }
    }

    /**
     * Find Artist by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return Artist if id matches the spotify_id field in the table and not blank.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public Optional<SpotifyTrack> find(@NonNull String id) {
        try (var entityManager = emf.createEntityManager()) {
            return find(entityManager, id);
        }
    }

    /**
     * Find SpotifyTrack by spotifyID field.
     * @param apiTrack Track object generated by the spotify-web-api.
     * @return SpotifyTrack if apiTrack already exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public Optional<SpotifyTrack> find(@NonNull Track apiTrack) {
        return find(apiTrack.getId());
    }

    /**
     * Checks if Track object generated by the spotify-web-api exists in the database.
     * @param apiTrack Track object generated by the spotify-web-api.
     * @return true if apiTrack exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public boolean exists(@NonNull Track apiTrack) {
        return find(apiTrack).isPresent();
    }

    /**
     * Checks if Track exists in the database by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return true if SpotifyTrack specified by id exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public boolean exists(@NonNull String id) {
        return find(id).isPresent();
    }

    /**
     * Checks if SpotifyTrack exists in the database.
     * @param track SpotifyTrack to check.
     * @return true if track exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public boolean exists(@NonNull SpotifyTrack track) {
        return find(track.getSpotifyID().getId()).isPresent();
    }

    /**
     * Attempts to persist a Track from the output of the spotify-web-api.
     * @param apiTrack Track object generated by the spotify-web-api.
     * @return SpotifyTrack already in the database with matching Spotify ID or new SpotifyTrack if apiTrack has a new
     * Spotify ID.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public SpotifyTrack persist(@NonNull Track apiTrack) {
        try (var entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            var spotifyTrack = persist(entityManager, apiTrack);
            entityManager.getTransaction().commit();
            return spotifyTrack;
        }
    }

    /**
     * Attempts to persist an array of Track objects from the output of the spotify-web-api.
     * @param apiTracks An array of Track objects generated by the spotify-web-api.
     * @return List of SpotifyTrack objects.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated
    public List<SpotifyTrack> persistAll(@NonNull Track[] apiTracks) {
        List<SpotifyTrack> spotifyTracks = new ArrayList<>();
        for (var apiTrack : apiTracks) {
            spotifyTracks.add(persist(apiTrack));
        }
        return spotifyTracks;
    }
}
