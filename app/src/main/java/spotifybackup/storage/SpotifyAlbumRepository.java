package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class SpotifyAlbumRepository {
    private final EntityManagerFactory emf;

    /**
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public SpotifyAlbumRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Find SpotifyAlbum by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return SpotifyAlbum if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyAlbum> find(EntityManager entityManager, @NonNull String id) {
        if (id.isBlank() || entityManager.find(SpotifyID.class, id) == null) {
            return Optional.empty();
        }
        var query = entityManager.createNamedQuery("SpotifyAlbum.findBySpotifyID", SpotifyAlbum.class);
        query.setParameter("spotifyID", entityManager.find(SpotifyID.class, id));
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Find SpotifyAlbum by spotifyID field.
     * @param apiAlbum AlbumSimplified object generated by the spotify-web-api.
     * @return SpotifyAlbum if apiAlbum already exists in the database.
     */
    static Optional<SpotifyAlbum> find(EntityManager entityManager, @NonNull AlbumSimplified apiAlbum) {
        return find(entityManager, apiAlbum.getId());
    }

    /**
     * Find SpotifyAlbum by spotifyID field.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return SpotifyAlbum if apiAlbum already exists in the database.
     */
    static Optional<SpotifyAlbum> find(EntityManager entityManager, @NonNull Album apiAlbum) {
        return find(entityManager, apiAlbum.getId());
    }

    /**
     * Attempts to persist an AlbumSimplified from the output of the spotify-web-api.
     * @param apiAlbum AlbumSimplified object generated by the spotify-web-api.
     * @return SpotifyAlbum already in the database with matching Spotify ID or new SpotifyAlbum if apiAlbum has a new
     * Spotify ID.
     */
    static SpotifyAlbum persist(EntityManager entityManager, @NonNull AlbumSimplified apiAlbum) {
        if (!entityManager.getTransaction().isActive()) {
            throw new RuntimeException("Method will only work from within an active transaction.");
        }
        var optionalAlbum = find(entityManager, apiAlbum);
        if (optionalAlbum.isPresent()) {
            return optionalAlbum.get();
        } else {
            var newAlbum = SpotifyAlbum.builder()
                    .isSimplified(true)
                    .spotifyID(new SpotifyID(apiAlbum.getId()))
                    .spotifyAlbumType(apiAlbum.getAlbumType())
                    .name(apiAlbum.getName())
                    .availableMarkets(convertMarkets(apiAlbum.getAvailableMarkets()))
                    .releaseDate(SpotifyObject.convertDate(apiAlbum.getReleaseDate(), apiAlbum.getReleaseDatePrecision()))
                    .releaseDatePrecision(apiAlbum.getReleaseDatePrecision())
                    .build();
            for (var apiArtist : apiAlbum.getArtists()) {
                newAlbum.addArtist(SpotifyArtistRepository.persist(entityManager, apiArtist));
            }
            entityManager.persist(newAlbum);
            return newAlbum;
        }
    }

    /**
     * Attempts to persist an Album from the output of the spotify-web-api.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return SpotifyAlbum already in the database with matching Spotify ID or new SpotifyAlbum if apiAlbum has a new
     * Spotify ID.
     */
    static SpotifyAlbum persist(EntityManager entityManager, @NonNull Album apiAlbum) {
        if (!entityManager.getTransaction().isActive()) {
            throw new RuntimeException("Method will only work from within an active transaction.");
        }
        var optionalAlbum = find(entityManager, apiAlbum);
        if (optionalAlbum.isPresent()) {
            return optionalAlbum.get();
        } else {
            var newAlbumBuilder = SpotifyAlbum.builder();
            newAlbumBuilder.isSimplified(false);
            newAlbumBuilder.spotifyID(new SpotifyID(apiAlbum.getId()));
            newAlbumBuilder.name(apiAlbum.getName());
            newAlbumBuilder.spotifyAlbumType(apiAlbum.getAlbumType());
            newAlbumBuilder.releaseDate(SpotifyObject.convertDate(apiAlbum.getReleaseDate(), apiAlbum.getReleaseDatePrecision()));
            newAlbumBuilder.releaseDatePrecision(apiAlbum.getReleaseDatePrecision());
            if (apiAlbum.getAvailableMarkets().length > 0) {
                newAlbumBuilder.availableMarkets(convertMarkets(apiAlbum.getAvailableMarkets()));
            }
            if (apiAlbum.getExternalIds().getExternalIds().containsKey("isrc")) {
                newAlbumBuilder.isrcID(apiAlbum.getExternalIds().getExternalIds().get("isrc"));
            }
            if (apiAlbum.getExternalIds().getExternalIds().containsKey("ean")) {
                newAlbumBuilder.eanID(apiAlbum.getExternalIds().getExternalIds().get("ean"));
            }
            if (apiAlbum.getExternalIds().getExternalIds().containsKey("upc")) {
                newAlbumBuilder.upcID(apiAlbum.getExternalIds().getExternalIds().get("upc"));
            }
            var newAlbum = newAlbumBuilder.build();
            for (var simplifiedApiArtist : apiAlbum.getArtists()) {
                newAlbum.addArtist(SpotifyArtistRepository.persist(entityManager, simplifiedApiArtist));
            }
            for (var simplifiedApiTrack : apiAlbum.getTracks().getItems()) {
                newAlbum.addTrack(SpotifyTrackRepository.persist(entityManager, simplifiedApiTrack, newAlbum));
            }
            newAlbum.addImages(SpotifyImageRepository.imageSetFactory(entityManager, apiAlbum.getImages()));
            newAlbum.addGenres(SpotifyGenreRepository.genreSetFactory(entityManager, apiAlbum.getGenres()));
            entityManager.persist(newAlbum);
            return newAlbum;
        }
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
     * Get count of albums in the database.
     * @return count of albums in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("SpotifyAlbum.countBy").getSingleResult();
        }
    }

    /**
     * Find SpotifyAlbum by spotifyID field.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return SpotifyAlbum if apiAlbum already exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public Optional<SpotifyAlbum> find(@NonNull Album apiAlbum) {
        try (var entityManager = emf.createEntityManager()) {
            return find(entityManager, apiAlbum.getId());
        }
    }

    /**
     * Checks if Album object generated by the spotify-web-api exists in the database.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return true if apiAlbum exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public boolean exists(@NonNull Album apiAlbum) {
        try (var entityManager = emf.createEntityManager()) {
            return find(entityManager, apiAlbum.getId()).isPresent();
        }
    }

    /**
     * Checks if Album exists in the database by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return true if SpotifyAlbum specified by id exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public boolean exists(@NonNull String id) {
        try (var entityManager = emf.createEntityManager()) {
            return find(entityManager, id).isPresent();
        }
    }

    /**
     * Check if Album exists in the database.
     * @param spotifyAlbum SpotifyAlbum to check
     * @return true if album exists in the database.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public boolean exists(@NonNull SpotifyAlbum spotifyAlbum) {
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(SpotifyAlbum.class, spotifyAlbum.getId()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Attempts to persist an Album from the output of the spotify-web-api.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return SpotifyAlbum already in the database with matching Spotify ID or new SpotifyAlbum if apiAlbum has a new
     * Spotify ID.
     * @deprecated Use SpotifyObjectRepository instead.
     */
    @Deprecated(forRemoval = true)
    public SpotifyAlbum persist(@NonNull Album apiAlbum) {
        try (var entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            var spotifyAlbum = persist(entityManager, apiAlbum);
            entityManager.getTransaction().commit();
            return spotifyAlbum;
        }
    }
}
