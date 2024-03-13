package spotifybackup.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyAlbumRepositoryTest {
    static final String albumDir = "src/test/java/spotifybackup/storage/spotify_api_get/album/";
    private SpotifyObjectRepository spotifyObjectRepository;

    static Album loadFromPath(String fileName) throws IOException {
        return new Album.JsonUtil().createModelObject(new String(Files.readAllBytes(Path.of(albumDir + fileName))));
    }

    private void assertAlbumForSelectionTestIsSuitable(int w, int h, ImageSelection selection, Album apiAlbum) {
        assertFalse(spotifyObjectRepository.exists(apiAlbum),
                "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertTrue(apiAlbum.getImages().length > 1);
        for (var apiImage : apiAlbum.getImages()) assertFalse(spotifyObjectRepository.exists(apiImage));
        switch (selection) {
            case ONLY_LARGEST -> {
                assertEquals(w, ImageSelection.findLargest(apiAlbum.getImages()).getWidth());
                assertEquals(h, ImageSelection.findLargest(apiAlbum.getImages()).getHeight());
            }
            case ONLY_SMALLEST -> {
                assertEquals(w, ImageSelection.findSmallest(apiAlbum.getImages()).getWidth());
                assertEquals(h, ImageSelection.findSmallest(apiAlbum.getImages()).getHeight());
            }
        }
    }

    private void assertAlbumWithSelectionPersistedCorrectly(long expectedAlbumImageCount, long expectedTotal, int w,
                                                            int h, ImageSelection selection,
                                                            Album apiAlbum, SpotifyAlbum album) {
        assertTrue(spotifyObjectRepository.exists(apiAlbum));
        assertEquals(apiAlbum.getId(), album.getSpotifyID().getId());
        assertEquals(expectedAlbumImageCount, album.getImages().size());
        assertEquals(expectedTotal, spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE));
        switch (selection) {
            case ONLY_LARGEST, ONLY_SMALLEST -> {
                assertEquals(1, album.getImages().size());
                SpotifyImage image = album.getImages().iterator().next();
                assertEquals(w, image.getWidth().orElseThrow());
                assertEquals(h, image.getHeight().orElseThrow());
                assertEquals(1, Arrays.stream(apiAlbum.getImages())
                        .filter(i -> i.getHeight() == h && i.getWidth() == w).count());
                assertEquals(Arrays.stream(apiAlbum.getImages()).filter(i -> i.getHeight() == h && i.getWidth() == w)
                        .findFirst().orElseThrow().getUrl(), image.getUrl());
            }
            case ALL -> assertEquals(expectedAlbumImageCount, apiAlbum.getImages().length);
        }
    }

    private void assertAlbumPersistedCorrectly(Album apiAlbum, SpotifyAlbum persistedAlbum) {
        assertTrue(spotifyObjectRepository.exists(apiAlbum.getId(), SpotifyID.class),
                "Can't find Album by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiAlbum), "Can't find Album by apiAlbum/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedAlbum), "Can't find Album by Object reference.");
        assertTrue(apiAlbum.getArtists().length > 0, apiAlbum.getName() + " should have artists.");
        assertEquals(apiAlbum.getArtists().length, persistedAlbum.getArtists().size());
        assertTrue(apiAlbum.getGenres().length > 0, apiAlbum.getName() + " should have genres.");
        assertEquals(apiAlbum.getGenres().length, persistedAlbum.getGenres().size());
        assertTrue(apiAlbum.getImages().length > 0, apiAlbum.getName() + " should have images.");
        assertEquals(apiAlbum.getImages().length, persistedAlbum.getImages().size());
        assertEquals(apiAlbum.getTracks().getTotal(), persistedAlbum.getTracks().size());
    }

    @BeforeEach
    void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_album_can_be_persisted() throws IOException {
        // Arrange
        final Album apiAlbum = loadFromPath("The_Heist.json");
        assertFalse(spotifyObjectRepository.exists(apiAlbum),
                "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertTrue(apiAlbum.getGenres().length > 0, "Album should have 1 or more genres.");

        // Act
        var persistedAlbum = spotifyObjectRepository.persist(apiAlbum);

        // Assert
        assertAlbumPersistedCorrectly(apiAlbum, persistedAlbum);
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, value = {
            "total, w,      h,      value",
            "3,     -1,     -1,     ALL",
            "1,     640,    640,    ONLY_LARGEST",
            "1,     64,     64,     ONLY_SMALLEST",
            "0,     -1,     -1,     NONE"
    })
    void ensure_album_can_be_persisted_with_different_image_selections(final long expectedTotal,
                                                                       final int w,
                                                                       final int h,
                                                                       final ImageSelection selection)
            throws IOException {
        // Arrange
        final Album apiAlbum = loadFromPath("The_Heist.json");
        assertAlbumForSelectionTestIsSuitable(w, h, selection, apiAlbum);
        final var oldImageCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE);

        // Act
        final SpotifyAlbum album = spotifyObjectRepository.persist(apiAlbum, selection);

        // Assert
        assertAlbumWithSelectionPersistedCorrectly(expectedTotal, expectedTotal + oldImageCount, w, h,
                selection, apiAlbum, album);
    }

    @Test
    void ensure_singles_album_can_be_persisted() throws IOException {
        // Arrange
        final Album apiAlbum = loadFromPath("Embers_Rise.json");
        assertFalse(spotifyObjectRepository.exists(apiAlbum),
                "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertTrue(apiAlbum.getArtists().length > 0, "Album should have 1 or more artists.");

        // Act
        var persistedAlbum = spotifyObjectRepository.persist(apiAlbum);

        // Assert
        assertAlbumPersistedCorrectly(apiAlbum, persistedAlbum);
    }

    @Test
    void ensure_simplified_album_can_be_filled_in_with_unsimplified() throws IOException {
        // Arrange
        final String albumJson = new String(Files.readAllBytes(Path.of(albumDir + "Ecliptica_(International_Version).json")));
        final AlbumSimplified apiAlbumSimple = new AlbumSimplified.JsonUtil().createModelObject(albumJson);
        final Album apiAlbum = new Album.JsonUtil().createModelObject(albumJson);
        final var albumSimple = spotifyObjectRepository.persist(apiAlbumSimple);
        assertTrue(spotifyObjectRepository.exists(albumSimple));
        assertTrue(albumSimple.getIsSimplified());
        assertNotEquals(0, apiAlbum.getTracks().getTotal());
        assertEquals(0, albumSimple.getTracks().size());

        // Act
        final var album = spotifyObjectRepository.persist(apiAlbum);

        // Assert
        assertEquals(albumSimple.getId(), album.getId());
        assertEquals(albumSimple.getSpotifyID(), album.getSpotifyID());
        assertFalse(album.getIsSimplified());
        assertNotEquals(0, album.getTracks().size());
        assertEquals(apiAlbum.getTracks().getTotal(), album.getTracks().size());
    }

    @Test
    void ensure_simplified_album_can_be_retrieved() throws IOException {
        // Arrange
        final String albumJson = new String(Files.readAllBytes(Path.of(albumDir + "The_Trick_To_Life.json")));
        final AlbumSimplified apiAlbumSimple = new AlbumSimplified.JsonUtil().createModelObject(albumJson);
        final Album apiAlbum = new Album.JsonUtil().createModelObject(albumJson);
        assertFalse(spotifyObjectRepository.exists(apiAlbumSimple));
        final var albumSimple = spotifyObjectRepository.persist(apiAlbumSimple);
        assertTrue(albumSimple.getIsSimplified());

        // Act
        final var oldSimpleAlbumIds = spotifyObjectRepository.getSimplifiedAlbumsSpotifyIDs();

        // Assert
        assertTrue(oldSimpleAlbumIds.contains(apiAlbumSimple.getId()));

        // Act 2
        spotifyObjectRepository.persist(apiAlbum);
        final var newSimpleAlbumIds = spotifyObjectRepository.getSimplifiedAlbumsSpotifyIDs();

        // Assert 2
        assertFalse(newSimpleAlbumIds.contains(apiAlbumSimple.getId()));
    }

    @Test
    void ensure_album_array_can_be_persisted() throws IOException {
        // Arrange
        final Album[] apiAlbums = {
                loadFromPath("The_Heist.json"),
                loadFromPath("The_Trick_To_Life.json"),
                loadFromPath("King.json")
        };
        for (var apiAlbum : apiAlbums)
            assertFalse(spotifyObjectRepository.exists(apiAlbum),
                    "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");

        // Act
        var persistedAlbums = spotifyObjectRepository.persist(apiAlbums);

        // Assert
        for (var persistedAlbum : persistedAlbums) {
            final var apiAlbum = Arrays.stream(apiAlbums)
                    .filter(a -> a.getId().equals(persistedAlbum.getSpotifyID().getId()))
                    .findFirst().orElseThrow();
            assertAlbumPersistedCorrectly(apiAlbum, persistedAlbum);
        }
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, value = {
            "total, w,      h,      value",
            "3,     -1,     -1,     ALL",
            "1,     640,    640,    ONLY_LARGEST",
            "1,     64,     64,     ONLY_SMALLEST",
            "0,     -1,     -1,     NONE"
    })
    void ensure_album_array_can_be_persisted_with_different_image_selections(final long expectedTotal,
                                                                             final int w,
                                                                             final int h,
                                                                             final ImageSelection selection)
            throws IOException {
        // Arrange
        final long oldCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.ALBUM);
        final Album[] apiAlbums = {
                loadFromPath("The_Heist.json"),
                loadFromPath("The_Trick_To_Life.json"),
                loadFromPath("King.json")
        };
        for (var apiAlbum : apiAlbums) assertAlbumForSelectionTestIsSuitable(w, h, selection, apiAlbum);
        final var oldImageCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE);

        // Act
        var albums = spotifyObjectRepository.persist(apiAlbums, selection);

        // Assert
        for (var apiAlbum : apiAlbums) {
            SpotifyAlbum album = albums.stream()
                    .filter(a -> a.getSpotifyID().getId().equals(apiAlbum.getId()))
                    .findAny().orElseThrow();
            assertAlbumWithSelectionPersistedCorrectly(expectedTotal,
                    (expectedTotal * apiAlbums.length) + oldImageCount, w, h, selection, apiAlbum, album);
        }
        assertEquals(oldCount + apiAlbums.length, spotifyObjectRepository.count(SpotifyObject.SubTypes.ALBUM));
    }

    @Test
    void ensure_album_array_can_be_persisted_shallowly() throws IOException {
        // Arrange
        final Album[] apiAlbums = {
                loadFromPath("The_Heist.json"),
                loadFromPath("The_Trick_To_Life.json"),
                loadFromPath("King.json")
        };
        for (var apiAlbum : apiAlbums)
            assertFalse(spotifyObjectRepository.exists(apiAlbum),
                    "Album with Spotify ID " + apiAlbum.getId() + " shouldn't already exist.");
        assertEquals(0, spotifyObjectRepository.count(SpotifyObject.SubTypes.TRACK));

        // Act
        var persistedAlbums = spotifyObjectRepository.persistWithoutTracks(apiAlbums);

        // Assert
        assertEquals(apiAlbums.length, spotifyObjectRepository.count(SpotifyObject.SubTypes.ALBUM));
        assertEquals(0, spotifyObjectRepository.count(SpotifyObject.SubTypes.TRACK));
        for (var album : persistedAlbums) {
            assertTrue(album.getTracks().isEmpty());
        }
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, value = {
            "total, w,      h,      value",
            "3,     -1,     -1,     ALL",
            "1,     640,    640,    ONLY_LARGEST",
            "1,     64,     64,     ONLY_SMALLEST",
            "0,     -1,     -1,     NONE"
    })
    void ensure_album_array_can_be_persisted_shallowly_with_different_image_selections(final long expectedTotal,
                                                                                       final int w,
                                                                                       final int h,
                                                                                       final ImageSelection selection)
            throws IOException {
        // Arrange
        final Album[] apiAlbums = {
                loadFromPath("The_Heist.json"),
                loadFromPath("The_Trick_To_Life.json"),
                loadFromPath("King.json")
        };
        for (var apiAlbum : apiAlbums) assertAlbumForSelectionTestIsSuitable(w, h, selection, apiAlbum);
        assertEquals(0, spotifyObjectRepository.count(SpotifyObject.SubTypes.TRACK));

        // Act
        var persistedAlbums = spotifyObjectRepository.persistWithoutTracks(apiAlbums, selection);

        // Assert
        assertEquals(apiAlbums.length, spotifyObjectRepository.count(SpotifyObject.SubTypes.ALBUM));
        assertEquals(0, spotifyObjectRepository.count(SpotifyObject.SubTypes.TRACK));
        for (var album : persistedAlbums) {
            assertTrue(album.getTracks().isEmpty());
        }
    }
}
