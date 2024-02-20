package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.model_objects.AbstractModelObject;
import se.michaelthelin.spotify.model_objects.specification.User;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.util.List;
import java.util.Optional;

import static spotifybackup.storage.SpotifyObject.ensureTransactionActive;
import static spotifybackup.storage.SpotifyObject.getSingleResultOptionally;

class SpotifyUserRepository {
    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private SpotifyUserRepository() {
        throw new ConstructorUsageException();
    }

    /**
     * Find SpotifyUser whose accounts were used to generate the database, identified by countryCode and productType not
     * being null.
     * @return List of SpotifyUser accounts used to generate the database.
     */
    static List<SpotifyUser> getAccountHolders(EntityManager em) {
        var query = new CriteriaDefinition<>(em, SpotifyUser.class) {};
        var root = query.from(SpotifyUser.class);
        query.where(query.isNotNull(root.get(SpotifyUser_.countryCode)),
                query.isNotNull(root.get(SpotifyUser_.productType)));
        return em.createQuery(query).getResultList();
    }

    /**
     * Find SpotifyUser by spotifyID field.
     * @param apiUser User object generated by the spotify-web-api.
     * @return SpotifyUser if apiUser already exists in the database.
     */
    static Optional<SpotifyUser> find(EntityManager entityManager, @NonNull User apiUser) {
        return find(entityManager, apiUser.getId());
    }

    /**
     * Find SpotifyUser by Spotify User ID string value.
     * @param id String containing a Spotify User ID.
     * @return SpotifyUser if id matches the spotify_user_id field in the table and not blank.
     */
    static Optional<SpotifyUser> find(EntityManager em, @NonNull String id) {
        if (id.isBlank()) return Optional.empty();
        var query = new CriteriaDefinition<>(em, SpotifyUser.class) {};
        var root = query.from(SpotifyUser.class);
        query.where(query.equal(root.get(SpotifyUser_.spotifyUserID), id));
        return getSingleResultOptionally(em, query);
    }

    static SpotifyUser persist(EntityManager entityManager, @NonNull AbstractModelObject apiUser) {
        if (apiUser instanceof User u) return persist(entityManager, u);
        else throw new IllegalArgumentException("apiUser should be of type User here.");
    }

    /**
     * Attempts to persist a User from the output of the spotify-web-api.
     * @param apiUser User object generated by the spotify-web-api.
     * @return SpotifyUser already in the database with matching Spotify ID or new SpotifyUser if apiUser has a new
     * Spotify ID.
     */
    static SpotifyUser persist(EntityManager entityManager, @NonNull User apiUser) {
        ensureTransactionActive.accept(entityManager);
        var optionalUser = find(entityManager, apiUser);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            if (apiUser.getId().isBlank()) throw new IllegalArgumentException("Spotify User ID can not be blank.");
            var newUser = SpotifyUser.builder()
                    .spotifyUserID(apiUser.getId())
                    .displayName(apiUser.getDisplayName())
                    .countryCode(apiUser.getCountry() == null ? null : apiUser.getCountry().getAlpha2())
                    .productType(apiUser.getProduct())
                    .build();
            newUser.addImages(SpotifyImageRepository.imageSetFactory(entityManager, apiUser.getImages()));
            entityManager.persist(newUser);
            return newUser;
        }
    }
}
