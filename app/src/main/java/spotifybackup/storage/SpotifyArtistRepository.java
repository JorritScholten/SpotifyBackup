package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Image;

import java.util.*;

public class SpotifyArtistRepository {
    private final EntityManagerFactory emf;
    private final SpotifyIDRepository spotifyIDRepository;

    public SpotifyArtistRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
        spotifyIDRepository = new SpotifyIDRepository(DB_ACCESS);
    }

    /**
     * Get count of artists in the database.
     * @return count of genres in the database.
     */
    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("SpotifyArtist.countBy").getSingleResult();
        }
    }

    /**
     * Find Artist by spotifyID field.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return Artist if apiArtist already exists in the database.
     */
    static Optional<SpotifyArtist> find(EntityManager entityManager, @NonNull ArtistSimplified apiArtist) {
        return find(entityManager, apiArtist.getId());
    }

    /**
     * Find Artist by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return Artist if id matches the spotify_id field in the table and not blank.
     */
    static Optional<SpotifyArtist> find(EntityManager entityManager, @NonNull String id) {
        var query = entityManager.createNamedQuery("SpotifyArtist.findBySpotifyID", SpotifyArtist.class);
        query.setParameter("spotifyID", id);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Find Artist by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return Artist if id matches the spotify_id field in the table and not blank.
     */
    public Optional<SpotifyArtist> find(@NonNull String id) {
        try (var entityManager = emf.createEntityManager()) {
            var query = entityManager.createNamedQuery("SpotifyArtist.findBySpotifyID", SpotifyArtist.class);
            final var optionalSpotifyID = spotifyIDRepository.find(id);
            if (optionalSpotifyID.isEmpty()) {
                return Optional.empty();
            } else {
                query.setParameter("spotifyID", optionalSpotifyID.get());
                try {
                    return Optional.of(query.getSingleResult());
                } catch (NoResultException e) {
                    return Optional.empty();
                }
            }
        }
    }

    static SpotifyArtist persist(EntityManager entityManager, @NonNull ArtistSimplified apiArtist) {
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
     * Check if Artist exists in the database.
     * @param spotifyArtist Artist to check
     * @return true if artist exists in the database.
     */
    public boolean exists(@NonNull SpotifyArtist spotifyArtist) {
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(SpotifyArtist.class, spotifyArtist.getId()) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Find Artist by spotifyID field.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return Artist if apiArtist already exists in the database.
     */
    public Optional<SpotifyArtist> find(@NonNull Artist apiArtist) {
        return find(apiArtist.getId());
    }

    /**
     * Check if Artist exists in persistence context by Spotify ID string value.
     * @param id String containing a Spotify ID.
     * @return true if Artist specified by id exists in the database.
     */
    public boolean exists(@NonNull String id) {
        return find(id).isPresent();
    }

    /**
     * Converts Image[] generated by spotify-web-api to Set<SpotifyImage>.
     */
    private Set<SpotifyImage> imageSetFactory(@NonNull Image[] images) {
        Set<SpotifyImage> imageSet = new HashSet<>();
        for (var image : images) {
            var newSpotifyImage = SpotifyImage.builder()
                    .url(image.getUrl())
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .build();
            imageSet.add(newSpotifyImage);
        }
        return imageSet;
    }

    /**
     * Converts genreNames[] generated by spotify-web-api to Set<Genre> and updates link table for ManyToMany mapping.
     */
    private Set<SpotifyGenre> genreSetFactory(EntityManager entityManager, @NonNull String[] genreNames) {
        if (!entityManager.getTransaction().isActive()) {
            throw new RuntimeException("Method will only work from within an active transaction.");
        }
        Set<SpotifyGenre> spotifyGenreSet = new HashSet<>();
        for (var genreName : genreNames) {
            var query = entityManager.createNamedQuery("SpotifyGenre.findByName", SpotifyGenre.class);
            query.setParameter("name", genreName);
            try {
                spotifyGenreSet.add(query.getSingleResult());
            } catch (NoResultException e) {
                spotifyGenreSet.add(new SpotifyGenre(genreName.toLowerCase(Locale.ENGLISH)));
            }
        }
        for (var genre : spotifyGenreSet) {
            entityManager.persist(genre);
        }
        return spotifyGenreSet;
    }

    /**
     * Checks if Artist object generated by the spotify-web-api exists in the database.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return true if apiArtist exists in the database.
     */
    public boolean exists(@NonNull Artist apiArtist) {
        return find(apiArtist).isPresent();
    }

    /**
     * Attempts to persist an Artist from the output of the spotify-web-api.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return Artist already in the database with matching Spotify ID or new Artist if apiArtist has a new Spotify ID.
     */
    public SpotifyArtist persist(@NonNull Artist apiArtist) {
        var optionalArtist = find(apiArtist);
        if (optionalArtist.isPresent()) {
            return optionalArtist.get();
        } else {
            var newArtist = SpotifyArtist.builder()
                    .name(apiArtist.getName())
                    .spotifyID(new SpotifyID(apiArtist.getId()))
                    .isSimplified(false)
                    .build();
            try (var entityManager = emf.createEntityManager()) {
                entityManager.getTransaction().begin();
                newArtist.addImages(imageSetFactory(apiArtist.getImages()));
                newArtist.addGenres(genreSetFactory(entityManager, apiArtist.getGenres()));
                entityManager.persist(newArtist);
                entityManager.getTransaction().commit();
                return newArtist;
            }
        }
    }

    /**
     * Attempts to persist an array of Artist objects from the output of the spotify-web-api.
     * @param apiArtists An array of Artist objects generated by the spotify-web-api.
     * @return Set of Artist objects.
     */
    public Set<SpotifyArtist> persistAll(@NonNull Artist[] apiArtists) {
        Set<SpotifyArtist> spotifyArtistSet = new HashSet<>();
        for (var apiArtist : apiArtists) {
            spotifyArtistSet.add(persist(apiArtist));
        }
        return spotifyArtistSet;
    }
}
