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
import java.util.logging.Level;
import java.util.logging.LogManager;

public class SpotifyObjectRepository {
    private static final String URL_DATASOURCE_NAME = "hibernate.hikari.dataSource.url";
    private final EntityManagerFactory emf;

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
            // TODO: create test that checks if this actually works
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

    private <T extends SpotifyObject, A extends AbstractModelObject> T
    persistAbstractModel(A apiObject, BiFunction<EntityManager, A, T> persist) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            var spotifyObject = persist.apply(em, apiObject);
            em.getTransaction().commit();
            return spotifyObject;
        }
    }

    private <T extends SpotifyObject, C extends Collection<T>, A extends AbstractModelObject> C
    persistAbstractModels(A[] apiObjects, C collection, BiFunction<EntityManager, A, T> persist) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            for (var apiObject : apiObjects) {
                collection.add(persist.apply(em, apiObject));
            }
            em.getTransaction().commit();
            return collection;
        }
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
     * @param spotifyID SpotifyID (or Spotify User ID).
     * @return SpotifyTrack, SpotifyAlbum, SpotifyArtist, or SpotifyUser if id matches the spotify_id field in the table
     * and not blank.
     */
    public Optional<? extends SpotifyObject> find(@NonNull SpotifyID spotifyID) {
        return find(spotifyID.getId());
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
     * Find SpotifyUser whose accounts were used to generate the database, identified by countryCode and productType not
     * being null.
     * @return List of SpotifyUser accounts used to generate the database.
     */
    public List<SpotifyUser> getAccountHolders() {
        try (var em = emf.createEntityManager()) {
            return SpotifyUserRepository.getAccountHolders(em);
        }
    }

    /**
     * Get count of type of SpotifyObject in the database.
     * @param type Entity type to perform count on.
     * @return count of SpotifyObject in the database.
     */
    public long count(final SpotifyObject.SubTypes type) {
        try (var em = emf.createEntityManager()) {
            var cb = em.getCriteriaBuilder();
            var query = cb.createQuery(Long.class);
            query.select(cb.count(query.from(type.type)));
            return em.createQuery(query).getSingleResult();
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
     * @return count of SavedTracks belonging to user in the database.
     */
    public long countSavedTracks(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = SpotifySavedTrackRepository.countByUser(em, user);
            return query.getSingleResult();
        }
    }

    /**
     * Get count of a users' saved albums in the database.
     * @param user The Spotify User account to get the count of.
     * @return count of SavedAlbums belonging to user in the database.
     */
    public long countSavedAlbums(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = SpotifySavedAlbumRepository.countByUser(em, user);
            return query.getSingleResult();
        }
    }

    /**
     * Get most recently added saved track in the database.
     * @param user The SpotifyUser account to get SpotifySavedTrack from.
     * @return Optional containing most recently added SpotifySavedTrack if user has SpotifySavedTrack object(s) in the
     * database.
     */
    public Optional<SpotifySavedTrack> getNewestSavedTrack(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = SpotifySavedTrackRepository.findNewestByUser(em, user);
            return query.getResultStream().findFirst();
        }
    }

    /**
     * Get most recently added saved album in the database.
     * @param user The SpotifyUser account to get SpotifySavedAlbum from.
     * @return Optional containing most recently added SpotifySavedAlbum if user has SpotifySavedAlbum object(s) in the
     * database.
     */
    public Optional<SpotifySavedAlbum> getNewestSavedAlbum(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = SpotifySavedAlbumRepository.findNewestByUser(em, user);
            return query.getResultStream().findFirst();
        }
    }

    /**
     * Get a users' saved songs as stored in the database.
     * @param user The SpotifyUser account to get SpotifySavedTrack objects from.
     * @return Set of a users' SpotifySavedTrack objects, may be empty.
     */
    public Set<SpotifySavedTrack> getSavedTracks(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = SpotifySavedTrackRepository.findByUser(em, user);
            return new HashSet<>(query.getResultList());
        }
    }

    /**
     * Get a users' saved albums as stored in the database.
     * @param user The SpotifyUser account to get SpotifySavedAlbum objects from.
     * @return Set of a users' SpotifySavedAlbum objects, may be empty.
     */
    public Set<SpotifySavedAlbum> getSavedAlbums(@NonNull SpotifyUser user) {
        throw new UnsupportedOperationException("to be implemented");
    }

    /**
     * Get a set of a users' formerly saved songs as stored in the database.
     * @param user The SpotifyUser account to get SpotifySavedTrack objects from.
     * @return Set of a users' SpotifySavedTrack objects, may be empty.
     */
    public Set<SpotifySavedTrack> getRemovedSavedTracks(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = SpotifySavedTrackRepository.findRemovedByUser(em, user);
            return new HashSet<>(query.getResultList());
        }
    }

    /**
     * Get a set of a users' formerly saved albums as stored in the database.
     * @param user The SpotifyUser account to get SpotifySavedAlbum objects from.
     * @return Set of a users' SpotifySavedAlbum objects, may be empty.
     */
    public Set<SpotifySavedAlbum> getRemovedSavedAlbums(@NonNull SpotifyUser user) {
        throw new UnsupportedOperationException("to be implemented");
    }

    /**
     * Marks a track as removed from a users' saved songs if it is currently a user's saved song. It is not actually
     * removed from the database, rather it is marked as removed (as well as when this is being done) and won't show up
     * in the return from getSavedTracks() anymore.
     * @param track The SpotifyTrack that has been removed from Liked Songs on Spotify.
     * @param user  The SpotifyUser account to remove track for.
     * @return a SpotifySavedTrack with updated fields if track is one of the users' saved songs, else returns empty.
     */
    public Optional<SpotifySavedTrack> removeSavedTrack(@NonNull SpotifyTrack track, @NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            var removedTrack = SpotifySavedTrackRepository.removeTrackFromLikedSongs(em, track, user);
            em.getTransaction().commit();
            return removedTrack;
        }
    }

    /**
     * Marks an album as removed from a users' saved albums if it is currently a user's saved album. It is not actually
     * removed from the database, rather it is marked as removed (as well as when this is being done) and won't show up
     * in the return from getSavedAlbums() anymore.
     * @param album The SpotifyAlbum that has been removed from Liked Albums on Spotify.
     * @param user  The SpotifyUser account to remove album for.
     * @return a SpotifySavedAlbum with updated fields if album is one of the users' saved albums, else returns empty.
     */
    public Optional<SpotifySavedAlbum> removeSavedAlbum(@NonNull SpotifyAlbum album, @NonNull SpotifyUser user){
        throw new UnsupportedOperationException("to be implemented");
    }

    /**
     * Get Spotify IDs of a users' saved songs as stored in the database.
     * @param user The SpotifyUser account to get SpotifySavedTrack objects from.
     * @return Set of Spotify IDs of a users' SpotifySavedTrack objects, may be empty.
     */
    public Set<String> getSavedTrackIds(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = SpotifySavedTrackRepository.findTrackIdsByUser(em, user);
            return new HashSet<>(query.getResultList());
        }
    }

    /**
     * Get Spotify IDs of a users' saved albums as stored in the database.
     * @param user The SpotifyUser account to get SpotifySavedAlbum objects from.
     * @return Set of Spotify IDs of a users' SpotifySavedAlbum objects, may be empty.
     */
    public Set<String> getSavedAlbumIds(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            var query = SpotifySavedAlbumRepository.findAlbumIdsByUser(em, user);
            return new HashSet<>(query.getResultList());
        }
    }

    /** Get set of playlists a user is following. */
    public Set<SpotifyPlaylist> getFollowedPlaylists(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            return SpotifyUserRepository.getFollowedPlaylists(em, user);
        }
    }

    /** Add playlists a user is following. */
    public void followPlaylists(@NonNull List<SpotifyPlaylist> playlists, @NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SpotifyUserRepository.followPlaylists(em, playlists, user);
            em.getTransaction().commit();
        }
    }

    /** Remove playlists a user is following. */
    public void unfollowPlaylists(@NonNull List<SpotifyPlaylist> playlists, @NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SpotifyUserRepository.unFollowPlaylists(em, playlists, user);
            em.getTransaction().commit();
        }
    }

    /** Get playlists owned by a user. */
    public Set<SpotifyPlaylist> getOwnedPlaylists(@NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            return SpotifyUserRepository.getOwnedPlaylists(em, user);
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
        return persistAbstractModel(image, SpotifyImageRepository::persist);
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
        return persistAbstractModel(apiPlaylist, SpotifyPlaylistRepository::persist);
    }

    /**
     * Attempts to persist a Playlist from the output of the spotify-web-api.
     * @param apiPlaylist Playlist object generated by the spotify-web-api.
     * @return SpotifyPlaylist already in the database with matching Spotify ID or new SpotifyPlaylist if apiPlaylist
     * has a new Spotify ID.
     */
    public SpotifyPlaylist persist(@NonNull PlaylistSimplified apiPlaylist) {
        return persistAbstractModel(apiPlaylist, SpotifyPlaylistRepository::persist);
    }

    /**
     * Attempts to persist a User from the output of the spotify-web-api.
     * @param apiUser User object generated by the spotify-web-api.
     * @return SpotifyUser already in the database with matching Spotify ID or new SpotifyUser if apiUser has a new
     * Spotify ID.
     */
    public SpotifyUser persist(@NonNull User apiUser) {
        return persistAbstractModel(apiUser, SpotifyUserRepository::persist);
    }

    /**
     * Attempts to persist an Artist from the output of the spotify-web-api.
     * @param apiArtist Artist object generated by the spotify-web-api.
     * @return SpotifyArtist already in the database with matching Spotify ID or new SpotifyArtist if apiArtist has a
     * new Spotify ID.
     */
    public SpotifyArtist persist(@NonNull Artist apiArtist) {
        return persistAbstractModel(apiArtist, SpotifyArtistRepository::persist);
    }

    /**
     * Attempts to persist an ArtistSimplified from the output of the spotify-web-api.
     * @param apiArtist ArtistSimplified object generated by the spotify-web-api.
     * @return SpotifyArtist already in the database with matching Spotify ID or new SpotifyArtist if apiArtist has a
     * new Spotify ID.
     */
    public SpotifyArtist persist(@NonNull ArtistSimplified apiArtist) {
        return persistAbstractModel(apiArtist, SpotifyArtistRepository::persist);
    }

    /**
     * Attempts to persist a Track from the output of the spotify-web-api.
     * @param apiTrack Track object generated by the spotify-web-api.
     * @return SpotifyTrack already in the database with matching Spotify ID or new SpotifyTrack if apiTrack has a new
     * Spotify ID.
     */
    public SpotifyTrack persist(@NonNull Track apiTrack) {
        return persistAbstractModel(apiTrack, SpotifyTrackRepository::persist);
    }

    /**
     * Attempts to persist an Album from the output of the spotify-web-api.
     * @param apiAlbum Album object generated by the spotify-web-api.
     * @return SpotifyAlbum already in the database with matching Spotify ID or new SpotifyAlbum if apiAlbum has a new
     * Spotify ID.
     */
    public SpotifyAlbum persist(@NonNull Album apiAlbum) {
        return persistAbstractModel(apiAlbum, SpotifyAlbumRepository::persist);
    }

    /**
     * Attempts to persist an AlbumSimplified from the output of the spotify-web-api.
     * @param apiAlbum AlbumSimplified object generated by the spotify-web-api.
     * @return SpotifyAlbum already in the database with matching Spotify ID or new SpotifyAlbum if apiAlbum has a new
     * Spotify ID.
     */
    public SpotifyAlbum persist(@NonNull AlbumSimplified apiAlbum) {
        return persistAbstractModel(apiAlbum, SpotifyAlbumRepository::persist);
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
        return persistAbstractModels(images, new HashSet<>(), SpotifyImageRepository::persist);
    }

    /**
     * Attempts to persist an array of Artist objects from the output of the spotify-web-api.
     * @param artists An array of Artist objects generated by the spotify-web-api.
     * @return Set of SpotifyArtist objects.
     */
    public Set<SpotifyArtist> persist(@NonNull Artist[] artists) {
        return persistAbstractModels(artists, new HashSet<>(), SpotifyArtistRepository::persist);
    }

    /**
     * Persists array of "Liked Songs"/SavedTrack objects from the output of the spotify-web-api.
     * @param tracks An array of SavedTrack objects generated by the spotify-web-api.
     * @param user   User account to which the "Liked Songs"/SavedTrack belong.
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
     * Persists array of liked albums from the output of the spotify-web-api.
     * @param albums An array of SavedAlbum objects generated by the spotify-web-api.
     * @param user   User account to which the SavedAlbums belong.
     * @return List of SpotifySavedAlbum objects.
     */
    public List<SpotifySavedAlbum> persist(@NonNull SavedAlbum[] albums, @NonNull SpotifyUser user) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            List<SpotifySavedAlbum> persistedAlbums = new ArrayList<>();
            for (var album : albums) {
                persistedAlbums.add(SpotifySavedAlbumRepository.persist(em, album, user));
            }
            em.getTransaction().commit();
            return persistedAlbums;
        }
    }

    /**
     * Attempts to persist an array of Track objects from the output of the spotify-web-api.
     * @param apiTracks An array of Track objects generated by the spotify-web-api.
     * @return List of SpotifyTrack objects.
     */
    public List<SpotifyTrack> persist(@NonNull Track[] apiTracks) {
        return persistAbstractModels(apiTracks, new ArrayList<>(), SpotifyTrackRepository::persist);
    }

    /**
     * Attempts to persist an array of PlaylistSimplified objects from the output of the spotify-web-api.
     * @param apiPlaylists An array of PlaylistSimplified objects generated by the spotify-web-api.
     * @return List of SpotifyPlaylist objects.
     */
    public List<SpotifyPlaylist> persist(@NonNull PlaylistSimplified[] apiPlaylists) {
        return persistAbstractModels(apiPlaylists, new ArrayList<>(), SpotifyPlaylistRepository::persist);
    }
}
