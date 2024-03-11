package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.List;
import java.util.Optional;

import static spotifybackup.storage.SpotifyObject.*;

class SpotifyAlbumRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyAlbumRepository() {
        throw new ConstructorUsageException();
    }

    private static void setNotSimpleFields(EntityManager em, boolean storeTracks, Album apiAlbum, SpotifyAlbum album) {
        if (apiAlbum.getExternalIds().getExternalIds().containsKey("isrc")) {
            album.setIsrcID(apiAlbum.getExternalIds().getExternalIds().get("isrc"));
        }
        if (apiAlbum.getExternalIds().getExternalIds().containsKey("ean")) {
            album.setEanID(apiAlbum.getExternalIds().getExternalIds().get("ean"));
        }
        if (apiAlbum.getExternalIds().getExternalIds().containsKey("upc")) {
            album.setUpcID(apiAlbum.getExternalIds().getExternalIds().get("upc"));
        }
        for (var simplifiedApiArtist : apiAlbum.getArtists()) {
            album.addArtist(SpotifyArtistRepository.persist(em, simplifiedApiArtist));
        }
        if (storeTracks) {
            for (var simplifiedApiTrack : apiAlbum.getTracks().getItems()) {
                album.addTrack(SpotifyTrackRepository.persist(em, simplifiedApiTrack, album));
            }
        }
        album.addImages(SpotifyImageRepository.imageSetFactory(em, apiAlbum.getImages()));
        album.addGenres(SpotifyGenreRepository.genreSetFactory(em, apiAlbum.getGenres()));
    }

    static List<String> findAllSpotifyIdsOfSimplified(EntityManager em) {
        var query = new CriteriaDefinition<>(em, String.class) {};
        var root = query.from(SpotifyAlbum.class);
        query.select(root.get(SpotifyAlbum_.spotifyID).asString())
                .where(query.isTrue(root.get(SpotifyAlbum_.isSimplified)));
        return em.createQuery(query).getResultList();
    }

    /**
     * Find SpotifyAlbum by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return SpotifyAlbum if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyAlbum> find(EntityManager em, @NonNull String id) {
        if (id.isBlank() || em.find(SpotifyID.class, id) == null) return Optional.empty();
        var query = new CriteriaDefinition<>(em, SpotifyAlbum.class) {};
        var root = query.from(SpotifyAlbum.class);
        query.where(query.equal(root.get(SpotifyAlbum_.spotifyID).asString(), id));
        return getSingleResultOptionally(em, query);
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
        ensureTransactionActive.accept(entityManager);
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
                    .releaseDate(convertDate(apiAlbum.getReleaseDate(), apiAlbum.getReleaseDatePrecision()))
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
        return persist(entityManager, apiAlbum, true);
    }

    static SpotifyAlbum persistWithoutTracks(EntityManager entityManager, @NonNull Album apiAlbum) {
        return persist(entityManager, apiAlbum, false);
    }

    private static SpotifyAlbum persist(EntityManager entityManager, @NonNull Album apiAlbum, boolean storeTracks) {
        ensureTransactionActive.accept(entityManager);
        var optionalAlbum = find(entityManager, apiAlbum);
        if (optionalAlbum.isPresent()) {
            if (!optionalAlbum.get().getIsSimplified()) return optionalAlbum.get();
            else {
                final var simpleAlbum = optionalAlbum.get();
                simpleAlbum.setIsSimplified(false);
                setNotSimpleFields(entityManager, storeTracks, apiAlbum, simpleAlbum);
                if (simpleAlbum.getAvailableMarkets().getCodes().isEmpty()) {
                    simpleAlbum.getAvailableMarkets().addCodes(apiAlbum.getAvailableMarkets());
                }
                entityManager.persist(simpleAlbum);
                return simpleAlbum;
            }
        } else {
            var newAlbum = SpotifyAlbum.builder()
                    .isSimplified(false)
                    .spotifyID(new SpotifyID(apiAlbum.getId()))
                    .name(apiAlbum.getName())
                    .spotifyAlbumType(apiAlbum.getAlbumType())
                    .releaseDate(convertDate(apiAlbum.getReleaseDate(), apiAlbum.getReleaseDatePrecision()))
                    .releaseDatePrecision(apiAlbum.getReleaseDatePrecision())
                    .availableMarkets(convertMarkets(apiAlbum.getAvailableMarkets()))
                    .build();
            entityManager.persist(newAlbum);
            setNotSimpleFields(entityManager, storeTracks, apiAlbum, newAlbum);
            entityManager.persist(newAlbum);
            return newAlbum;
        }
    }
}
