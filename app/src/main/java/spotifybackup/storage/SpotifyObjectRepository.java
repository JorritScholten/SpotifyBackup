package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.NonNull;
import org.hibernate.service.spi.ServiceException;
import se.michaelthelin.spotify.model_objects.AbstractModelObject;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class SpotifyObjectRepository {
    private static final String URL_DATASOURCE_NAME = "hibernate.hikari.dataSource.url";
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

    private SpotifyObjectRepository(@NonNull String persistenceUnitName, @NonNull Properties dbAccess) {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        try {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName, dbAccess);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    /**
     * Factory method to create SpotifyObjectRepository, creates new database file if one does not already exist.
     * @param dbPath File path of database.
     */
    public static SpotifyObjectRepository factory(@NonNull File dbPath) {
        if (!dbPath.exists()) createNewDb(dbPath);
        if (!dbPath.isFile())
            throw new IllegalArgumentException("Supplied filepath to database is unusable: " + dbPath);
        final Properties dbAccess = new Properties();
        dbAccess.put(URL_DATASOURCE_NAME, generateDataSourceUrl(dbPath));
        return new SpotifyObjectRepository("SpotifyObjects", dbAccess);
    }

    /**
     * Factory method to create SpotifyObjectRepository to new database, data is not persisted across multiple runs.
     * @apiNote Should only be used for testing.
     */
    public static SpotifyObjectRepository testFactory(boolean showSql) {
        final Properties dbAccess = new Properties();
        dbAccess.put("hibernate.show_sql", showSql ? "true" : "false");
        dbAccess.put(URL_DATASOURCE_NAME, generateDataSourceUrl(new File("build/spotifyObjectsTest")));
        return new SpotifyObjectRepository("SpotifyObjectsTest", dbAccess);
    }

    private static void createNewDb(File dbPath) {
        final Properties dbAccess = new Properties();
        dbAccess.put("hibernate.hbm2ddl.auto", "create");
        dbAccess.put(URL_DATASOURCE_NAME, generateDataSourceUrl(dbPath));
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        try (var emf = Persistence.createEntityManagerFactory("SpotifyObjects", dbAccess)) {
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    private static String generateDataSourceUrl(File dbPath) {
        var dataSourceUrl = new StringBuilder("jdbc:h2:");
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
                case SpotifyUser u -> em.find(u.getClass(), u.getId());
                case SpotifyPlaylistItem i -> em.find(i.getClass(), i.getId());
                case SpotifyPlaylist p -> em.find(p.getClass(), p.getId());
                default -> throw new IllegalStateException("Unsupported SpotifyObject queried, add implementation for: "
                        + spotifyObject.getClass());
            } != null;
        }
    }

    /**
     * Check if SpotifyImage, SpotifyGenre, SpotifyID or SpotifyUser exists by its url, name, Spotify ID, or Spotify
     * User ID respectively.
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
                case USER -> SpotifyUserRepository.find(em, value).isPresent();
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
                case Image apiImage -> SpotifyImageRepository.find(em, apiImage).isPresent();
                case User apiUser -> SpotifyUserRepository.find(em, apiUser).isPresent();
                case Playlist apiPlaylist -> SpotifyPlaylistRepository.find(em, apiPlaylist).isPresent();
                default ->
                        throw new IllegalStateException("Object type not yet handled by implementation: " + apiObject);
            };
        }
    }

    /**
     * Find SpotifyTrack, SpotifyAlbum, SpotifyArtist, or SpotifyUser by Spotify ID (or Spotify User ID) string value.
     * @param spotifyID String containing a Spotify ID (or Spotify User ID).
     * @return SpotifyTrack, SpotifyAlbum, SpotifyArtist, or SpotifyUser if id matches the spotify_id field in the table
     * and not blank.
     */
    public Optional<? extends SpotifyObject> find(@NonNull String spotifyID) {
        try (var em = emf.createEntityManager()) {
            var userOptional = SpotifyUserRepository.find(em, spotifyID);
            if (userOptional.isPresent()) return userOptional;
            var playlistOptional = SpotifyPlaylistRepository.find(em, spotifyID);
            if (playlistOptional.isPresent()) return playlistOptional;
            var albumOptional = SpotifyAlbumRepository.find(em, spotifyID);
            if (albumOptional.isPresent()) return albumOptional;
            var artistOptional = SpotifyArtistRepository.find(em, spotifyID);
            if (artistOptional.isPresent()) return artistOptional;
            return SpotifyTrackRepository.find(em, spotifyID);
        }
    }

    /**
     * Find SpotifyUser whose account was used to generate the database, identified by countryCode and productType not
     * being null. TODO: rework this to return List<> instead.
     * @return SpotifyUser if only one entry has a non-null countryCode and ProductType.
     */
    public Optional<SpotifyUser> getAccountHolder() {
        try (var em = emf.createEntityManager()) {
            return SpotifyUserRepository.getAccountHolder(em);
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
     * Get count of a users' saved tracks in the database.
     * @param user The Spotify User account to get the count of.
     * @return count of Saved Tracks belonging to user in the database.
     */
    public long countSavedTracks(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = em.createNamedQuery("SpotifySavedTrack.countByUser");
            query.setParameter("user", user);
            return (Long) query.getSingleResult();
        }
    }

    /**
     * Attempts to persist a genre by its name, if it already exists returns already existing SpotifyGenre.
     * @param genreName name of genre as defined by Spotify.
     * @return SpotifyGenre if genreName is not blank.
     * @implNote Can't use persistAbstractModel lambda here because genreName is not a type derived from
     * AbstractModelObject.
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
     * Attempts to persist a Playlist from the output of the spotify-web-api.
     * @param apiPlaylist Playlist object generated by the spotify-web-api.
     * @return SpotifyPlaylist already in the database with matching Spotify ID or new SpotifyPlaylist if apiPlaylist
     * has a new Spotify ID.
     */
    public SpotifyPlaylist persist(@NonNull Playlist apiPlaylist) {
        return (SpotifyPlaylist) persistAbstractModel.apply(apiPlaylist).apply(emf).apply(SpotifyPlaylistRepository::persist);
    }

    /**
     * Attempts to persist a Playlist from the output of the spotify-web-api.
     * @param apiPlaylist Playlist object generated by the spotify-web-api.
     * @return SpotifyPlaylist already in the database with matching Spotify ID or new SpotifyPlaylist if apiPlaylist
     * has a new Spotify ID.
     */
    public SpotifyPlaylist persist(@NonNull PlaylistSimplified apiPlaylist) {
        return (SpotifyPlaylist) persistAbstractModel.apply(apiPlaylist).apply(emf).apply(SpotifyPlaylistRepository::persist);
    }

    /**
     * Attempts to persist a User from the output of the spotify-web-api.
     * @param apiUser User object generated by the spotify-web-api.
     * @return SpotifyUser already in the database with matching Spotify ID or new SpotifyUser if apiUser has a new
     * Spotify ID.
     */
    public SpotifyUser persist(@NonNull User apiUser) {
        return (SpotifyUser) persistAbstractModel.apply(apiUser).apply(emf).apply(SpotifyUserRepository::persist);
    }

    /**
     * Attempts to persist an Artist from the output of the spotify-web-api.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return SpotifyArtist already in the database with matching Spotify ID or new SpotifyArtist if apiArtist has a
     * new Spotify ID.
     */
    public SpotifyArtist persist(@NonNull Artist apiArtist) {
        return (SpotifyArtist) persistAbstractModel.apply(apiArtist).apply(emf).apply(SpotifyArtistRepository::persist);
    }

    /**
     * Attempts to persist an ArtistSimplified from the output of the spotify-web-api.
     * @param apiArtist ArtistSimplified object generated by the spotify-web-api.
     * @return SpotifyArtist already in the database with matching Spotify ID or new SpotifyArtist if apiArtist has a
     * new Spotify ID.
     */
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

    /**
     * Attempts to persist an AlbumSimplified from the output of the spotify-web-api.
     * @param apiAlbum AlbumSimplified object generated by the spotify-web-api.
     * @return SpotifyAlbum already in the database with matching Spotify ID or new SpotifyAlbum if apiAlbum has a new
     * Spotify ID.
     */
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
     * Persists array of "Liked Songs"/SavedTrack objects from the output of the spotify-web-api.
     * @param tracks An array of SavedTrack objects generated by the spotify-web-api.
     * @param user User account to which the "Liked Songs"/SavedTrack belong.
     * @return List of SpotifySavedTrack objects.
     */
    public List<SpotifySavedTrack> persist(@NonNull SavedTrack[] tracks, @NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            List<SpotifySavedTrack> persistedTracks = new ArrayList<>();
            for (var track : tracks) {
                persistedTracks.add(SpotifySavedTrackRepository.persist(em, track, user));
            }
            em.getTransaction().commit();
            return persistedTracks;
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
