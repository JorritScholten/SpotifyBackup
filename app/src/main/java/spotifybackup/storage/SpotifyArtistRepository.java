package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.List;
import java.util.Optional;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;
import static spotifybackup.storage.SpotifyObject.getSingleResultOptionally;

class SpotifyArtistRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyArtistRepository() {
        throw new ConstructorUsageException();
    }

    static List<String> findAllSpotifyIdsOfSimplified(EntityManager em) {
        var query = new CriteriaDefinition<>(em, String.class) {};
        var root = query.from(SpotifyArtist.class);
        query.select(root.get(SpotifyArtist_.spotifyID).asString())
                .where(query.isTrue(root.get(SpotifyArtist_.isSimplified)));
        return em.createQuery(query).getResultList();
    }

    /**
     * Find SpotifyArtist by spotifyID field.
     * @param apiArtist ArtistSimplified object generated by the spotify-web-api.
     * @return SpotifyArtist if apiArtist already exists in the database.
     */
    static Optional<SpotifyArtist> find(EntityManager entityManager, @NonNull ArtistSimplified apiArtist) {
        return find(entityManager, apiArtist.getId());
    }

    /**
     * Find SpotifyArtist by spotifyID field.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return SpotifyArtist if apiArtist already exists in the database.
     */
    static Optional<SpotifyArtist> find(EntityManager entityManager, @NonNull Artist apiArtist) {
        return find(entityManager, apiArtist.getId());
    }

    /**
     * Find SpotifyArtist by spotifyID value.
     * @param id SpotifyID of artist.
     * @return SpotifyArtist if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyArtist> find(EntityManager entityManager, @NonNull SpotifyID id) {
        return find(entityManager, id.getId());
    }

    /**
     * Find SpotifyArtist by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return SpotifyArtist if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyArtist> find(EntityManager em, @NonNull String id) {
        if (id.isBlank() || em.find(SpotifyID.class, id) == null) return Optional.empty();
        var query = new CriteriaDefinition<>(em, SpotifyArtist.class) {};
        var root = query.from(SpotifyArtist.class);
        query.where(query.equal(root.get(SpotifyArtist_.spotifyID).asString(), id));
        return getSingleResultOptionally(em, query);
    }

    /**
     * Attempts to persist an ArtistSimplified from the output of the spotify-web-api.
     * @param apiArtist ArtistSimplified object generated by the spotify-web-api.
     * @return SpotifyArtist already in the database with matching Spotify ID or new SpotifyArtist if apiArtist has a
     * new Spotify ID.
     */
    static SpotifyArtist persist(EntityManager entityManager, @NonNull ArtistSimplified apiArtist) {
        ensureTransactionActive.accept(entityManager);
        var optionalArtist = find(entityManager, apiArtist);
        if (optionalArtist.isPresent()) {
            return optionalArtist.get();
        } else {
            var newArtist = SpotifyArtist.builder()
                    .name(apiArtist.getName())
                    .spotifyID(new SpotifyID(apiArtist.getId()))
                    .isSimplified(true)
                    .build();
            entityManager.persist(newArtist);
            return newArtist;
        }
    }

    /**
     * Attempts to persist an Artist from the output of the spotify-web-api.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return Artist already in the database with matching Spotify ID or new Artist if apiArtist has a new Spotify ID.
     */
    static SpotifyArtist persist(EntityManager entityManager, @NonNull Artist apiArtist) {
        ensureTransactionActive.accept(entityManager);
        var optionalArtist = find(entityManager, apiArtist);
        if (optionalArtist.isPresent()) {
            if (!optionalArtist.get().getIsSimplified()) return optionalArtist.get();
            else {
                final var simpleArtist = optionalArtist.get();
                simpleArtist.setIsSimplified(false);
                simpleArtist.addImages(SpotifyImageRepository.imageSetFactory(entityManager, apiArtist.getImages()));
                simpleArtist.addGenres(SpotifyGenreRepository.genreSetFactory(entityManager, apiArtist.getGenres()));
                entityManager.persist(simpleArtist);
                return simpleArtist;
            }
        } else {
            var newArtist = SpotifyArtist.builder()
                    .name(apiArtist.getName())
                    .spotifyID(new SpotifyID(apiArtist.getId()))
                    .isSimplified(false)
                    .build();
            newArtist.addImages(SpotifyImageRepository.imageSetFactory(entityManager, apiArtist.getImages()));
            newArtist.addGenres(SpotifyGenreRepository.genreSetFactory(entityManager, apiArtist.getGenres()));
            entityManager.persist(newArtist);
            return newArtist;
        }
    }
}
