package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyTrackRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String albumDir = testDataDir + "album/";
    static final String trackDir = testDataDir + "track/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_track_can_be_persisted() throws IOException {
        // Arrange
        final Track apiTrack = new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "Bio-Engineering.json")))
        );
        assertFalse(spotifyObjectRepository.exists(apiTrack),
                "Track with Spotify ID " + apiTrack.getId() + " shouldn't already exist.");

        // Act
        var persistedTrack = spotifyObjectRepository.persist(apiTrack);

        // Assert
        assertTrue(spotifyObjectRepository.exists(apiTrack.getId(), SpotifyID.class),
                "Can't find Track by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiTrack), "Can't find Track by apiTrack/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedTrack), "Can't find Track by Object reference.");
        assertTrue(apiTrack.getArtists().length > 0);
        assertEquals(apiTrack.getArtists().length, persistedTrack.getArtists().size());
        assertEquals(apiTrack.getAlbum().getId(), persistedTrack.getAlbum().getSpotifyID().getId());
    }

    @Test
    void ensure_track_market_availability_is_persisted_correctly() throws IOException {
        // Arrange
        final Track apiTrack = new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "Bio-Engineering.json")))
        );
        final var originalCodes = SpotifyObject.convertMarkets(apiTrack.getAvailableMarkets());
        var persistedTrack = spotifyObjectRepository.persist(apiTrack);
        assertNotEquals(0, persistedTrack.getAvailableMarkets().getCodes().size());

        // Act
        final var storedCodes = ((SpotifyTrack) spotifyObjectRepository.find(persistedTrack.getSpotifyID()).orElseThrow()).getAvailableMarkets();

        // Assert
        assertEquals(originalCodes, storedCodes);
    }

    @Test
    void ensure_track_with_no_market_availability_is_persisted_correctly() throws IOException {
        // Arrange
        final Track apiTrack = new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "No_Markets.json")))
        );
        final var originalCodes = SpotifyObject.convertMarkets(apiTrack.getAvailableMarkets());
        var persistedTrack = spotifyObjectRepository.persist(apiTrack);
        assertEquals(0, persistedTrack.getAvailableMarkets().getCodes().size());

        // Act
        final var storedCodes = ((SpotifyTrack) spotifyObjectRepository.find(persistedTrack.getSpotifyID()).orElseThrow()).getAvailableMarkets();

        // Assert
        assertEquals(originalCodes, storedCodes);
        assertEquals(0, storedCodes.getCodes().size());
    }

    @Test
    void ensure_multiple_tracks_can_be_persisted() throws IOException {
        // Arrange
        final long oldCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.TRACK);
        final Track[] apiTracks = {new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "Can't_Hold_Us_(feat._Ray_Dalton).json")))
        ), new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "Thrift_Shop_(feat._Wanz).json")))
        )};
        for (var apiTrack : apiTracks) {
            assertFalse(spotifyObjectRepository.exists(apiTrack),
                    "Track with Spotify ID " + apiTrack.getId() + " shouldn't already exist.");
        }

        // Act
        var persistedTracks = spotifyObjectRepository.persist(apiTracks);

        // Assert
        for (var apiTrack : apiTracks) {
            var persistedTrack = persistedTracks.stream()
                    .filter(t -> t.getSpotifyID().getId().equals(apiTrack.getId()))
                    .findAny();
            assertTrue(spotifyObjectRepository.exists(apiTrack.getId(), SpotifyID.class),
                    "Can't find Track by Spotify ID.");
            assertTrue(spotifyObjectRepository.exists(apiTrack), "Can't find Track by apiTrack/Spotify ID.");
            assertTrue(persistedTrack.isPresent());
            assertTrue(apiTrack.getArtists().length > 0);
            assertEquals(apiTrack.getArtists().length, persistedTrack.get().getArtists().size());
            assertEquals(apiTrack.getAlbum().getId(), persistedTrack.get().getAlbum().getSpotifyID().getId());
        }
        assertEquals(oldCount + apiTracks.length, spotifyObjectRepository.count(SpotifyObject.SubTypes.TRACK));
    }

    @Test
    void ensure_simplified_track_can_be_filled_in_with_unsimplified() throws IOException {
        // Arrange
        final Album apiAlbum = new Album.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(albumDir + "King.json")))
        );
        final var album = spotifyObjectRepository.persist(apiAlbum);
        assertTrue(spotifyObjectRepository.exists(album));
        final var apiTrack = new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "King.json")))
        );
        assertTrue(spotifyObjectRepository.exists(apiTrack));
        assertFalse(apiTrack.getExternalIds().getExternalIds().get("isrc").isBlank(),
                "Track needs to have an external id for this test.");
        final var simpleTrack = (SpotifyTrack) spotifyObjectRepository.find(apiTrack.getId()).orElseThrow();
        assertTrue(simpleTrack.getIsSimplified());
        assertTrue(simpleTrack.getIsrcID().isEmpty());

        // Act
        final var track = spotifyObjectRepository.persist(apiTrack);

        // Assert
        assertEquals(simpleTrack.getId(), track.getId());
        assertEquals(simpleTrack.getSpotifyID(), track.getSpotifyID());
        assertFalse(track.getIsSimplified());
        assertTrue(track.getIsrcID().isPresent());
        assertFalse(track.getIsrcID().orElseThrow().isBlank());
    }

    @Test
    void ensure_simplified_track_can_be_retrieved() throws IOException {
        // Arrange
        final Album apiAlbum = new Album.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(albumDir + "The_Trick_To_Life.json")))
        );
        assertFalse(spotifyObjectRepository.exists(apiAlbum));
        final Track apiTrack = new Track.JsonUtil().createModelObject(
                new String(Files.readAllBytes(Path.of(trackDir + "Worried_About_Ray.json")))
        );
        final TrackSimplified apiTrackSimple = apiAlbum.getTracks().getItems()[0];
        assertEquals(apiTrackSimple.getId(), apiTrack.getId(), "Should be the same Track.");
        spotifyObjectRepository.persist(apiAlbum);
        assertTrue(spotifyObjectRepository.exists(apiAlbum));
        final SpotifyTrack trackSimple = (SpotifyTrack) spotifyObjectRepository.find(apiTrackSimple.getId()).orElseThrow();
        assertTrue(trackSimple.getIsSimplified());

        // Act
        final var oldSimpleTrackIds = spotifyObjectRepository.getSimplifiedTracksSpotifyIDs();

        // Assert
        assertTrue(oldSimpleTrackIds.contains(apiTrackSimple.getId()));

        // Act 2
        spotifyObjectRepository.persist(apiTrack);
        final var newSimpleTrackIds = spotifyObjectRepository.getSimplifiedTracksSpotifyIDs();

        // Assert 2
        assertFalse(newSimpleTrackIds.contains(apiTrackSimple.getId()));
    }
}
