package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyTrackRepositoryTest {
    static final String testDataDir = "src/test/java/spotifybackup/storage/spotify_api_get/";
    static final String albumDir = testDataDir + "album/";
    static final String trackDir = testDataDir + "track/";
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        try {
            spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
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
        assertEquals(apiTrack.getArtists().length, persistedTrack.getSpotifyArtists().size());
        assertEquals(apiTrack.getAlbum().getId(), persistedTrack.getSpotifyAlbum().getSpotifyID().getId());
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
            assertEquals(apiTrack.getArtists().length, persistedTrack.get().getSpotifyArtists().size());
            assertEquals(apiTrack.getAlbum().getId(), persistedTrack.get().getSpotifyAlbum().getSpotifyID().getId());
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
}
