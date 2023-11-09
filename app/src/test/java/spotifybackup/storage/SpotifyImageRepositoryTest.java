package spotifybackup.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Image;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpotifyImageRepositoryTest {
    static private SpotifyObjectRepository spotifyObjectRepository;

    @BeforeAll
    static void setup() {
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
    }

    @Test
    void ensure_image_can_be_persisted() {
        // Arrange
        final Image image = new Image.Builder()
                .setUrl("https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228")
                .setHeight(300)
                .setWidth(300)
                .build();
        assertFalse(spotifyObjectRepository.exists(image),
                "Image with url " + image.getUrl() + " shouldn't already exist.");

        // Act
        var persistedImage = spotifyObjectRepository.persist(image);

        // Assert
        assertTrue(spotifyObjectRepository.exists(image));
        assertTrue(spotifyObjectRepository.exists(persistedImage));
    }

    @Test
    void ensure_unsized_image_can_be_persisted() {
        // Arrange
        final Image image = new Image.Builder()
                .setUrl("https://i.scdn.co/image/ab6761610000f178129c7158f9565223cead0dd8")
                .build();
        assertFalse(spotifyObjectRepository.exists(image) ||
                        spotifyObjectRepository.exists(image.getUrl(), SpotifyImage.class),
                "Image with url " + image.getUrl() + " shouldn't already exist."
        );

        // Act
        var persistedImage = spotifyObjectRepository.persist(image);

        // Assert
        assertTrue(spotifyObjectRepository.exists(image.getUrl(), SpotifyImage.class));
        assertTrue(spotifyObjectRepository.exists(image));
        assertTrue(spotifyObjectRepository.exists(persistedImage));
    }

    @Test
    void persist_multiple_new_images() {
        // Arrange
        final Image[] images = {
                new Image.Builder()
                        .setUrl("https://i.scdn.co/image/ab6761610000e5ebfa16c573e959a8cec07c6441")
                        .setHeight(640)
                        .setWidth(640)
                        .build(),
                new Image.Builder()
                        .setUrl("https://i.scdn.co/image/ab67616100005174fa16c573e959a8cec07c6441")
                        .setHeight(320)
                        .setWidth(320)
                        .build(),
                new Image.Builder()
                        .setUrl("https://i.scdn.co/image/ab6761610000f178fa16c573e959a8cec07c6441")
                        .setHeight(160)
                        .setWidth(160)
                        .build()
        };
        for (var image : images) {
            assertFalse(spotifyObjectRepository.exists(image),
                    "Image with url " + image.getUrl() + " shouldn't already exist.");
        }

        // Act
        final var persistedImages = spotifyObjectRepository.persist(images);

        // Assert
        for (var persistedImage : persistedImages) {
            assertTrue(spotifyObjectRepository.exists(persistedImage));
        }
    }
}
