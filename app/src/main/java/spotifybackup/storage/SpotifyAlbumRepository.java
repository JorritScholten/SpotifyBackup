package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class SpotifyAlbumRepository {
    private final EntityManagerFactory emf;
    private final SpotifyIDRepository spotifyIDRepository;

    public SpotifyAlbumRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
        spotifyIDRepository = new SpotifyIDRepository(DB_ACCESS);
    }

    /**
     * Get count of albums in the database.
     * @return count of albums in the database.
     */
    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("SpotifyAlbum.countBy").getSingleResult();
        }
    }

    /**
     * Find SpotifyAlbum by spotifyID field.
     * @param apiAlbum Track object generated by the spotify-web-api.
     * @return SpotifyAlbum if apiAlbum already exists in the database.
     */
    public Optional<SpotifyAlbum> find(@NonNull Album apiAlbum) {
        try (var entityManager = emf.createEntityManager()) {
            return find(entityManager, apiAlbum.getId());
        }
    }

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

    static Optional<SpotifyAlbum> find(EntityManager entityManager, @NonNull AlbumSimplified apiAlbum) {
        return find(entityManager, apiAlbum.getId());
    }

    /**
     * Checks if Album object generated by the spotify-web-api exists in the database.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return true if apiAlbum exists in the database.
     */
    public boolean exists(@NonNull Album apiAlbum) {
        try (var entityManager = emf.createEntityManager()) {
            return find(entityManager, apiAlbum.getId()).isPresent();
        }
    }


    /**
     * Attempts to persist a Album from the output of the spotify-web-api.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return SpotifyAlbum already in the database with matching Spotify ID or new SpotifyAlbum if apiAlbum has a new
     * Spotify ID.
     */
    public SpotifyAlbum persist(@NonNull Album apiAlbum) {
        var optionalAlbum = find(apiAlbum);
        if (optionalAlbum.isPresent()) {
            return optionalAlbum.get();
        } else {
            try (var entityManager = emf.createEntityManager()) {
                entityManager.getTransaction().begin();
                var newAlbumBuilder = SpotifyAlbum.builder();
                newAlbumBuilder.isSimplified(false);
                newAlbumBuilder.spotifyID(new SpotifyID(apiAlbum.getId()));
                newAlbumBuilder.name(apiAlbum.getName());
                newAlbumBuilder.spotifyAlbumType(apiAlbum.getAlbumType());
                newAlbumBuilder.releaseDate(apiAlbum.getReleaseDate());
                newAlbumBuilder.releaseDatePrecision(apiAlbum.getReleaseDatePrecision());
                if (apiAlbum.getAvailableMarkets().length > 0) {
                    newAlbumBuilder.availableMarkets(convertMarkets(apiAlbum.getAvailableMarkets()));
                }
                if (apiAlbum.getExternalIds().getExternalIds().containsKey("isrc")) {
                    newAlbumBuilder.isrcID(apiAlbum.getExternalIds().getExternalIds().get("isrc"));
                }
                var newAlbum = newAlbumBuilder.build();
                for (var simplifiedApiArtist : apiAlbum.getArtists()) {
                    newAlbum.addArtist(SpotifyArtistRepository.persist(entityManager, simplifiedApiArtist));
                }
                for (var simplifiedApiTrack : apiAlbum.getTracks().getItems()){
                    newAlbum.addTrack(SpotifyTrackRepository.persist(entityManager, simplifiedApiTrack));
                }
                // handle genres as well
                entityManager.persist(newAlbum);
                entityManager.getTransaction().commit();
                return newAlbum;
            }
        }
    }

    static SpotifyAlbum persist(EntityManager entityManager, @NonNull AlbumSimplified apiAlbum) {
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
                    .releaseDate(apiAlbum.getReleaseDate())
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
}