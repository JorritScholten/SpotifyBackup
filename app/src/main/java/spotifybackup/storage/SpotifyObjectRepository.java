package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import se.michaelthelin.spotify.model_objects.AbstractModelObject;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class SpotifyObjectRepository {
    private static final String PERSISTENCE_UNIT_NAME = "persistenceUnitName";
    private final EntityManagerFactory emf;
    private final Function<AbstractModelObject, Function<EntityManagerFactory, Function<
            BiFunction<EntityManager, AbstractModelObject, ? extends SpotifyObject>,
            ? extends SpotifyObject>>> persistAbstractModel = apiObject -> factory -> persist -> {
        try (var em = factory.createEntityManager()) {
            em.getTransaction().begin();
            var spotifyObject = persist.apply(em, apiObject);
            em.getTransaction().commit();
            return spotifyObject;
        }
    };

    private SpotifyObjectRepository(Properties dbAccess) {
        emf = Persistence.createEntityManagerFactory(dbAccess.getProperty(PERSISTENCE_UNIT_NAME), dbAccess);
    }

    /**
     * Factory method to create SpotifyObjectRepository.
     * @param dbPath File path of database.
     */
    public static SpotifyObjectRepository factory(@NonNull File dbPath) {
        if (!(dbPath.isFile() && dbPath.exists() && dbPath.canRead() && dbPath.canWrite())) {
            throw new IllegalArgumentException("Supplied filepath to database is unusable: " + dbPath);
        }
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties dbAccess = new Properties();
        dbAccess.put("hibernate.hbm2ddl.auto", "validate");
        dbAccess.put("hibernate.show_sql", "false");
        dbAccess.put(PERSISTENCE_UNIT_NAME, "SpotifyObjects");
        dbAccess.put("hibernate.hikari.dataSource.url", generateDataSourceUrl(dbPath));
        return new SpotifyObjectRepository(dbAccess);
    }

    /**
     * Factory method to create SpotifyObjectRepository to new database, data is not persisted across multiple runs.
     * @apiNote Should only be used for testing.
     */
    public static SpotifyObjectRepository testFactory(boolean showSql) {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties dbAccess = new Properties();
        dbAccess.put("hibernate.hbm2ddl.auto", "create");
        dbAccess.put("hibernate.show_sql", showSql ? "true" : "false");
        dbAccess.put(PERSISTENCE_UNIT_NAME, "SpotifyObjectsTest");
        dbAccess.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/spotifyObjectsTest;DB_CLOSE_DELAY=-1");
        return new SpotifyObjectRepository(dbAccess);
    }

    private static String generateDataSourceUrl(File dbPath) {
        var dataSourceUrl = new StringBuilder("jdbc:h2:");
        dataSourceUrl.append(dbPath.getAbsolutePath());
        if (dbPath.getAbsolutePath().endsWith(".mv.db")) {
            dataSourceUrl.append(dbPath.getAbsolutePath(), 0, dbPath.getAbsolutePath().length() - 6);
        } else if (dbPath.getAbsolutePath().endsWith(".db")) {
            dataSourceUrl.append(dbPath.getAbsolutePath(), 0, dbPath.getAbsolutePath().length() - 3);
        } else {
            dataSourceUrl.append(dbPath.getAbsolutePath());
        }
        dataSourceUrl.append(";DB_CLOSE_DELAY=-1");
        return dataSourceUrl.toString();
    }

    /**
     * Checks if SpotifyObject exists in the database.
     * @param spotifyObject entity object to check.
     * @return true if spotifyObject exists.
     */
    public boolean exists(@NonNull SpotifyObject spotifyObject) {
        try (var em = emf.createEntityManager()) {
            return switch (spotifyObject) {
                case SpotifyGenre g -> em.find(g.getClass(), g.getId());
                case SpotifyImage i -> em.find(i.getClass(), i.getId());
                case SpotifyID i -> em.find(i.getClass(), i.getId());
                case SpotifyArtist a -> em.find(a.getClass(), a.getId());
                case SpotifyAlbum a -> em.find(a.getClass(), a.getId());
                case SpotifyTrack t -> em.find(t.getClass(), t.getId());
                default -> throw new IllegalStateException("Unsupported SpotifyObject queried, add implementation for: "
                        + spotifyObject.getClass());
            } != null;
        }
    }

    /**
     * Check if SpotifyImage, SpotifyGenre, or SpotifyID exists by its url, name or Spotify ID respectively.
     * @param value String to check.
     * @param type  Entity type to check.
     * @return true if value matches field in the database.
     * @throws IllegalArgumentException if value is blank.
     */
    public boolean exists(@NonNull String value, Class<? extends SpotifyObject> type) {
        if (value.isBlank()) throw new IllegalArgumentException("Value should not be blank.");
        try (var em = emf.createEntityManager()) {
            return switch (SpotifyObject.accessSubTypeByClass.apply(type)) {
                case GENRE -> SpotifyGenreRepository.find(em, value).isPresent();
                case IMAGE -> SpotifyImageRepository.find(em, value).isPresent();
                case ID -> em.find(SpotifyID.class, value) != null;
                default -> throw new IllegalArgumentException(SpotifyObject.accessSubTypeByClass.apply(type).name +
                        " not a valid type for this method.");
            };
        }
    }

    /**
     * Checks if AbstractModelObject object generated by the spotify-web-api exists in the database.
     * @param apiObject AbstractModelObject object generated by the spotify-web-api.
     * @return true if apiObject exists in the database.
     */
    public boolean exists(@NonNull AbstractModelObject apiObject) {
        try (var em = emf.createEntityManager()) {
            return switch (apiObject) {
                case Track apiTrack -> SpotifyTrackRepository.find(em, apiTrack.getId()).isPresent();
                case Album apiAlbum -> SpotifyAlbumRepository.find(em, apiAlbum.getId()).isPresent();
                case Artist apiArtist -> SpotifyArtistRepository.find(em, apiArtist).isPresent();
                case Image image -> {
                    if (image.getHeight() != null && image.getWidth() != null)
                        yield SpotifyImageRepository.find(em, image).isPresent();
                    else
                        yield SpotifyImageRepository.find(em, image.getUrl()).isPresent();
                }
                default ->
                        throw new IllegalStateException("Object type not yet handled by implementation: " + apiObject);
            };
        }
    }

    /**
     * Find SpotifyTrack, SpotifyAlbum or SpotifyArtist by Spotify ID string value.
     * @param spotifyID String containing a Spotify ID.
     * @return SpotifyTrack, SpotifyAlbum or SpotifyArtist if id matches the spotify_id field in the table and not blank.
     */
    public Optional<? extends SpotifyObject> find(@NonNull String spotifyID) {
        try (var em = emf.createEntityManager()) {
//            var userOptional = SpotifyUserRepository.find(em, spotifyID);
//            if (userOptional.isPresent()) return userOptional;
//            var playlistOptional = SpotifyPlaylistRepository.find(em, spotifyID);
//            if (playlistOptional.isPresent()) return playlistOptional;
            var albumOptional = SpotifyAlbumRepository.find(em, spotifyID);
            if (albumOptional.isPresent()) return albumOptional;
            var artistOptional = SpotifyArtistRepository.find(em, spotifyID);
            if (artistOptional.isPresent()) return artistOptional;
            return SpotifyTrackRepository.find(em, spotifyID);
        }
    }

    /**
     * Get count of type of SpotifyObject in the database.
     * @param type Entity type to perform count on.
     * @return count of SpotifyObject in the database.
     */
    public long count(SpotifyObject.SubTypes type) {
        try (var em = emf.createEntityManager()) {
            return (Long) em.createNamedQuery(type.name + ".countBy").getSingleResult();
        }
    }

    /**
     * Get count of type of SpotifyObject in the database.
     * @param type Entity type to perform count on.
     * @return count of SpotifyObject in the database.
     */
    public long count(Class<? extends SpotifyObject> type) {
        return count(SpotifyObject.accessSubTypeByClass.apply(type));
    }

    /**
     * Attempts to persist a genre by its name, if it already exists returns already existing SpotifyGenre.
     * @param genreName name of genre as defined by Spotify.
     * @return SpotifyGenre if genreName is not blank.
     */
    public SpotifyGenre persist(@NonNull String genreName) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            var persistedGenre = SpotifyGenreRepository.persist(em, genreName);
            em.getTransaction().commit();
            return persistedGenre;
        }
    }

    /**
     * Attempts to persist an Image, if it already exists returns already existing SpotifyImage.
     * @param image Image object generated by spotify-web-api.
     * @return SpotifyImage if already in the database or images' url is not too long or empty.
     */
    public SpotifyImage persist(@NonNull Image image) {
        return (SpotifyImage) persistAbstractModel.apply(image).apply(emf).apply(SpotifyImageRepository::persist);
    }

    /**
     * Attempts to persist a Spotify ID by its string representation, if it already exists returns already existing
     * SpotifyID.
     * @param newID Spotify ID to persist (base-62 identifier as defined by Spotify).
     * @return SpotifyID if id is not blank.
     * @throws IllegalArgumentException if id value in SpotifyID is blank.
     */
    public SpotifyID persist(@NonNull SpotifyID newID) {
        if (newID.getId().isBlank()) throw new IllegalArgumentException("ID value in SpotifyID should not be blank.");
        try (var em = emf.createEntityManager()) {
            try {
                var foundId = em.find(SpotifyID.class, newID.getId());
                if (foundId == null) throw new NullPointerException();
                else return foundId;
            } catch (IllegalArgumentException | NullPointerException e) {
                em.getTransaction().begin();
                em.persist(newID);
                em.getTransaction().commit();
                return newID;
            }
        }
    }

    /**
     * Attempts to persist an Artist from the output of the spotify-web-api.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return Artist already in the database with matching Spotify ID or new Artist if apiArtist has a new Spotify ID.
     */
    public SpotifyArtist persist(@NonNull Artist apiArtist) {
        return (SpotifyArtist) persistAbstractModel.apply(apiArtist).apply(emf).apply(SpotifyArtistRepository::persist);
    }

    public SpotifyArtist persist(@NonNull ArtistSimplified apiArtist) {
        return (SpotifyArtist) persistAbstractModel.apply(apiArtist).apply(emf).apply(SpotifyArtistRepository::persist);
    }

    /**
     * Attempts to persist a Track from the output of the spotify-web-api.
     * @param apiTrack Track object generated by the spotify-web-api.
     * @return SpotifyTrack already in the database with matching Spotify ID or new SpotifyTrack if apiTrack has a new
     * Spotify ID.
     */
    public SpotifyTrack persist(@NonNull Track apiTrack) {
        return (SpotifyTrack) persistAbstractModel.apply(apiTrack).apply(emf).apply(SpotifyTrackRepository::persist);
    }

    /**
     * Attempts to persist an Album from the output of the spotify-web-api.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return SpotifyAlbum already in the database with matching Spotify ID or new SpotifyAlbum if apiAlbum has a new
     * Spotify ID.
     */
    public SpotifyAlbum persist(@NonNull Album apiAlbum) {
        return (SpotifyAlbum) persistAbstractModel.apply(apiAlbum).apply(emf).apply(SpotifyAlbumRepository::persist);
    }

    public SpotifyAlbum persist(@NonNull AlbumSimplified apiAlbum) {
        return (SpotifyAlbum) persistAbstractModel.apply(apiAlbum).apply(emf).apply(SpotifyAlbumRepository::persist);
    }

    /**
     * Attempts to persist an array of genres by name, if a Genre already exists the already existing SpotifyGenre is
     * used.
     * @param genreNames an array of genre names as defined by Spotify.
     * @return Set of SpotifyGenre objects.
     */
    public Set<SpotifyGenre> persist(@NonNull String[] genreNames) {
        try (var em = emf.createEntityManager()) {
            Set<SpotifyGenre> spotifyGenreSet = new HashSet<>();
            em.getTransaction().begin();
            for (var genreName : genreNames) {
                spotifyGenreSet.add(SpotifyGenreRepository.persist(em, genreName));
            }
            em.getTransaction().commit();
            return spotifyGenreSet;
        }
    }

    /**
     * Attempts to persist an array of Image objects, if an Image is already stored the already existing SpotifyImage is
     * used.
     * @param images Array of Image objects generated by spotify-web-api.
     * @return Set of SpotifyImage objects.
     */
    public Set<SpotifyImage> persist(@NonNull Image[] images) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Set<SpotifyImage> imageSet = new HashSet<>();
            for (var image : images) {
                imageSet.add(SpotifyImageRepository.persist(em, image));
            }
            em.getTransaction().commit();
            return imageSet;
        }
    }

    /**
     * Attempts to persist an array of Artist objects from the output of the spotify-web-api.
     * @param apiArtists An array of Artist objects generated by the spotify-web-api.
     * @return Set of SpotifyArtist objects.
     */
    public Set<SpotifyArtist> persist(@NonNull Artist[] apiArtists) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Set<SpotifyArtist> spotifyArtistSet = new HashSet<>();
            for (var apiArtist : apiArtists) {
                spotifyArtistSet.add(SpotifyArtistRepository.persist(em, apiArtist));
            }
            em.getTransaction().commit();
            return spotifyArtistSet;
        }
    }

    /**
     * Attempts to persist an array of Track objects from the output of the spotify-web-api.
     * @param apiTracks An array of Track objects generated by the spotify-web-api.
     * @return List of SpotifyTrack objects.
     */
    public List<SpotifyTrack> persist(@NonNull Track[] apiTracks) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            List<SpotifyTrack> spotifyTracks = new ArrayList<>();
            for (var apiTrack : apiTracks) {
                spotifyTracks.add(SpotifyTrackRepository.persist(em, apiTrack));
            }
            em.getTransaction().commit();
            return spotifyTracks;
        }
    }
}
