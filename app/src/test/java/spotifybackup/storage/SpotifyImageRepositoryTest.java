package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Image;

import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpotifyImageRepositoryTest {
    static private SpotifyImageRepository spotifyImageRepository;

    @BeforeAll
    static void setup() {
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "true");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            spotifyImageRepository = new SpotifyImageRepository(DB_ACCESS);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_image_can_be_persisted() throws URISyntaxException {
        // Arrange
        final Image image = new Image.Builder()
                .setUrl("https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228")
                .setHeight(300)
                .setWidth(300)
                .build();
        assertFalse(spotifyImageRepository.exists(image),
                "Image with url " + image.getUrl() + " shouldn't already exist.");

        // Act
        var persistedImage = spotifyImageRepository.persist(image);

        // Assert
        assertTrue(persistedImage.isPresent());
        assertTrue(spotifyImageRepository.exists(image.getUrl()));
        assertTrue(spotifyImageRepository.exists(persistedImage.orElseThrow()));
    }

    @Test
    void ensure_unsized_image_can_be_persisted() throws URISyntaxException {
        // Arrange
        final Image image = new Image.Builder()
                .setUrl("https://i.scdn.co/image/ab6761610000f178129c7158f9565223cead0dd8")
                .build();
        assertFalse(spotifyImageRepository.exists(image),
                "Image with url " + image.getUrl() + " shouldn't already exist.");

        // Act
        var persistedImage = spotifyImageRepository.persist(image);

        // Assert
        assertTrue(persistedImage.isPresent());
        assertTrue(spotifyImageRepository.exists(image.getUrl()));
        assertTrue(spotifyImageRepository.exists(persistedImage.orElseThrow()));
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
            assertFalse(spotifyImageRepository.exists(image),
                    "Image with url " + image.getUrl() + " shouldn't already exist.");
        }

        // Act
        final var persistedImages = spotifyImageRepository.persistAll(images);

        // Assert
        for (var persistedImage : persistedImages) {
            assertTrue(spotifyImageRepository.exists(persistedImage));
        }
    }
}
