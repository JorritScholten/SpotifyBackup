package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SpotifyAlbumRepository {
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
