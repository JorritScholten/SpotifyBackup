package spotifybackup.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EnableStorageTests", matches = "true")
class SpotifyArtistRepositoryTest {
    static final String artistDir = "src/test/java/spotifybackup/storage/spotify_api_get/artist/";
    private SpotifyObjectRepository spotifyObjectRepository;

    private Artist loadFromPath(String fileName) throws IOException {
        return new Artist.JsonUtil().createModelObject(new String(Files.readAllBytes(Path.of(artistDir + fileName))));
    }

    private void assertArtistForSelectionTestIsSuitable(int w, int h, ImageSelection selection, Artist apiArtist) {
        assertFalse(spotifyObjectRepository.exists(apiArtist),
                "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");
        assertTrue(apiArtist.getImages().length > 1);
        for (var apiImage : apiArtist.getImages()) assertFalse(spotifyObjectRepository.exists(apiImage));
        switch (selection) {
            case ONLY_LARGEST -> {
                assertEquals(w, ImageSelection.findLargest(apiArtist.getImages()).getWidth());
                assertEquals(h, ImageSelection.findLargest(apiArtist.getImages()).getHeight());
            }
            case ONLY_SMALLEST -> {
                assertEquals(w, ImageSelection.findSmallest(apiArtist.getImages()).getWidth());
                assertEquals(h, ImageSelection.findSmallest(apiArtist.getImages()).getHeight());
            }
        }
    }

    private void assertArtistWithSelectionPersistedCorrectly(long expectedAlbumImageCount, long expectedTotal, int w,
                                                             int h, ImageSelection selection,
                                                             Artist apiArtist, SpotifyArtist artist) {
        assertTrue(spotifyObjectRepository.exists(apiArtist));
        assertEquals(apiArtist.getId(), artist.getSpotifyID().getId());
        assertEquals(expectedAlbumImageCount, artist.getImages().size());
        assertEquals(expectedTotal, spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE));
        switch (selection) {
            case ONLY_LARGEST, ONLY_SMALLEST -> {
                assertEquals(1, artist.getImages().size());
                SpotifyImage image = artist.getImages().iterator().next();
                assertEquals(w, image.getWidth().orElseThrow());
                assertEquals(h, image.getHeight().orElseThrow());
                assertEquals(1, Arrays.stream(apiArtist.getImages())
                        .filter(i -> i.getHeight() == h && i.getWidth() == w).count());
                assertEquals(Arrays.stream(apiArtist.getImages()).filter(i -> i.getHeight() == h && i.getWidth() == w)
                        .findFirst().orElseThrow().getUrl(), image.getUrl());
            }
            case ALL -> assertEquals(expectedAlbumImageCount, apiArtist.getImages().length);
        }
    }

    @BeforeEach
    void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Rivers_Cuomo.json", "Macklemore_&_Ryan_Lewis.json", "Ryan_Lewis.json", "Texas.json"})
    void ensure_artist_can_be_persisted(final String fileName) throws IOException {
        // Arrange
        final Artist apiArtist = loadFromPath(fileName);
        assertFalse(spotifyObjectRepository.exists(apiArtist),
                "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");

        // Act
        var persistedArtist = spotifyObjectRepository.persist(apiArtist);

        // Assert
        assertTrue(spotifyObjectRepository.exists(apiArtist.getId(), SpotifyID.class),
                "Can't find Artist by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(persistedArtist), "Can't find Artist by Object reference.");
        assertTrue(apiArtist.getGenres().length > 0);
        assertEquals(apiArtist.getGenres().length, persistedArtist.getGenres().size());
        assertTrue(apiArtist.getImages().length > 0);
        assertEquals(apiArtist.getImages().length, persistedArtist.getImages().size());
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, value = {
            "total, w,      h,      value",
            "3,     -1,     -1,     ALL",
            "1,     640,    640,    ONLY_LARGEST",
            "1,     160,    160,    ONLY_SMALLEST",
            "0,     -1,     -1,     NONE"
    })
    void ensure_artist_can_be_persisted_with_different_image_selections(final int expectedTotal, final int w,
                                                                        final int h, final ImageSelection selection)
            throws IOException {
        // Arrange
        final Artist apiArtist = loadFromPath("Texas.json");
        assertArtistForSelectionTestIsSuitable(w, h, selection, apiArtist);
        final var oldImageCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE);

        // Act
        final SpotifyArtist artist = spotifyObjectRepository.persist(apiArtist, selection);

        // Assert
        assertArtistWithSelectionPersistedCorrectly(expectedTotal, expectedTotal + oldImageCount, w, h,
                selection, apiArtist, artist);
    }

    @ParameterizedTest
    @EnumSource(ImageSelection.class)
    void ensure_artist_without_images_can_be_persisted_with_different_image_selections(final ImageSelection selection)
            throws IOException {
        // Arrange
        final Artist apiArtist = loadFromPath("Texas_wo_images.json");
        assertEquals(0, apiArtist.getImages().length);
        final var oldImageCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE);

        // Act
        final SpotifyArtist artist = spotifyObjectRepository.persist(apiArtist, selection);

        // Assert
        assertTrue(spotifyObjectRepository.exists(apiArtist.getId(), SpotifyID.class),
                "Can't find Artist by Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
        assertTrue(spotifyObjectRepository.exists(artist), "Can't find Artist by Object reference.");
        assertEquals(0, artist.getImages().size());
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, value = {
            "total, w,      h,      value",
            "3,     -1,     -1,     ALL",
            "1,     640,    640,    ONLY_LARGEST",
            "1,     160,    160,    ONLY_SMALLEST",
            "0,     -1,     -1,     NONE"
    })
    void ensure_simplified_artist_can_be_persisted_with_different_image_selections(final int expectedTotal,
                                                                                   final int w,
                                                                                   final int h,
                                                                                   final ImageSelection selection)
            throws IOException {
        // Arrange
        final String artistJson = new String(Files.readAllBytes(Path.of(artistDir + "Texas.json")));
        final var apiArtistSimple = new ArtistSimplified.JsonUtil().createModelObject(artistJson);
        final var apiArtist = new Artist.JsonUtil().createModelObject(artistJson);
        assertArtistForSelectionTestIsSuitable(w, h, selection, apiArtist);
        final var oldImageCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE);

        // Act
        spotifyObjectRepository.persist(apiArtistSimple);
        final SpotifyArtist artist = spotifyObjectRepository.persist(apiArtist, selection);

        // Assert
        assertArtistWithSelectionPersistedCorrectly(expectedTotal, expectedTotal + oldImageCount, w, h,
                selection, apiArtist, artist);
    }

    @Test
    void ensure_multiple_artists_can_be_persisted() throws IOException {
        // Arrange
        final long oldCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.ARTIST);
        final Artist[] apiArtists = {
                loadFromPath("Macklemore_&_Ryan_Lewis.json"),
                loadFromPath("Macklemore.json"),
                loadFromPath("Ryan_Lewis.json")
        };
        for (var apiArtist : apiArtists) {
            assertFalse(spotifyObjectRepository.exists(apiArtist),
                    "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");
        }

        // Act
        var persistedArtists = spotifyObjectRepository.persist(apiArtists);

        // Assert
        for (var apiArtist : apiArtists) {
            var persistedArtist = persistedArtists.stream()
                    .filter(a -> a.getSpotifyID().getId().equals(apiArtist.getId()))
                    .findAny();
            assertTrue(spotifyObjectRepository.exists(apiArtist.getId(), SpotifyID.class),
                    "Can't find Artist by Spotify ID.");
            assertTrue(spotifyObjectRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
            assertTrue(persistedArtist.isPresent());
            assertTrue(spotifyObjectRepository.exists(persistedArtist.get()),
                    "Can't find Artist by Object reference.");
            assertTrue(apiArtist.getGenres().length > 0);
            assertEquals(apiArtist.getGenres().length, persistedArtist.get().getGenres().size());
            assertTrue(apiArtist.getImages().length > 0);
            assertEquals(apiArtist.getImages().length, persistedArtist.get().getImages().size());
        }
        assertEquals(oldCount + apiArtists.length, spotifyObjectRepository.count(SpotifyObject.SubTypes.ARTIST));
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, value = {
            "total, w,      h,      value",
            "3,     -1,     -1,     ALL",
            "1,     640,    640,    ONLY_LARGEST",
            "1,     160,    160,    ONLY_SMALLEST",
            "0,     -1,     -1,     NONE"
    })
    void ensure_multiple_artists_can_be_persisted_with_different_image_selections(final long expectedTotal,
                                                                                  final int w,
                                                                                  final int h,
                                                                                  final ImageSelection selection)
            throws IOException {
        // Arrange
        final long oldCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.ARTIST);
        final Artist[] apiArtists = {
                loadFromPath("Macklemore_&_Ryan_Lewis.json"),
                loadFromPath("Macklemore.json"),
                loadFromPath("Ryan_Lewis.json")
        };
        for (var apiArtist : apiArtists) assertArtistForSelectionTestIsSuitable(w, h, selection, apiArtist);
        final var oldImageCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.IMAGE);

        // Act
        List<SpotifyArtist> persistedArtists = spotifyObjectRepository.persist(apiArtists, selection);

        // Assert
        for (var apiArtist : apiArtists) {
            SpotifyArtist artist = persistedArtists.stream()
                    .filter(a -> a.getSpotifyID().getId().equals(apiArtist.getId()))
                    .findAny().orElseThrow();
            assertArtistWithSelectionPersistedCorrectly(expectedTotal,
                    (expectedTotal * apiArtists.length) + oldImageCount, w, h, selection, apiArtist, artist);
        }
        assertEquals(oldCount + apiArtists.length, spotifyObjectRepository.count(SpotifyObject.SubTypes.ARTIST));
    }

    @Test
    void ensure_simplified_artist_can_be_filled_in_with_unsimplified() throws IOException {
        // Arrange
        final String artistJson = new String(Files.readAllBytes(Path.of(artistDir + "Texas.json")));
        final var apiArtistSimple = new ArtistSimplified.JsonUtil().createModelObject(artistJson);
        final var apiArtist = new Artist.JsonUtil().createModelObject(artistJson);
        assertNotEquals(0, apiArtist.getGenres().length);
        final var artistSimple = spotifyObjectRepository.persist(apiArtistSimple);
        assertTrue(spotifyObjectRepository.exists(artistSimple));
        assertTrue(artistSimple.getIsSimplified());
        assertEquals(0, artistSimple.getGenres().size());

        // Act
        final var artist = spotifyObjectRepository.persist(apiArtist);

        // Assert
        assertEquals(artistSimple.getId(), artist.getId());
        assertEquals(artistSimple.getSpotifyID(), artist.getSpotifyID());
        assertFalse(artist.getIsSimplified());
        assertNotEquals(0, artist.getGenres().size());
        assertEquals(apiArtist.getGenres().length, artist.getGenres().size());
    }

    @Test
    void ensure_simplified_artist_can_be_retrieved() throws IOException {
        // Arrange
        final String artistJson = new String(Files.readAllBytes(Path.of(artistDir + "Jake_Chudnow.json")));
        final var apiArtistSimple = new ArtistSimplified.JsonUtil().createModelObject(artistJson);
        final var apiArtist = new Artist.JsonUtil().createModelObject(artistJson);
        assertFalse(spotifyObjectRepository.exists(apiArtistSimple));
        final var artistSimple = spotifyObjectRepository.persist(apiArtistSimple);
        assertTrue(artistSimple.getIsSimplified());

        // Act
        final var oldSimpleArtistIds = spotifyObjectRepository.getSimplifiedArtistsSpotifyIDs();

        // Assert
        assertTrue(oldSimpleArtistIds.contains(apiArtistSimple.getId()));

        // Act 2
        spotifyObjectRepository.persist(apiArtist);
        final var newSimpleArtistIds = spotifyObjectRepository.getSimplifiedArtistsSpotifyIDs();

        // Assert 2
        assertFalse(newSimpleArtistIds.contains(apiArtistSimple.getId()));
    }
}
