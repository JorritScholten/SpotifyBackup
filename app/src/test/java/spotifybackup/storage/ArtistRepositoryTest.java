package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArtistRepositoryTest {
    static private ArtistRepository artistRepository;

    @BeforeAll
    static void setup() {
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "false");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            artistRepository = new ArtistRepository(DB_ACCESS);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
    }

    @Test
    void ensure_artist_can_be_persisted() {
        // Arrange
        final Artist apiArtist = new Artist.JsonUtil().createModelObject("""
                {
                    "external_urls": {
                      "spotify": "https://open.spotify.com/artist/4LAz9VRX8Nat9kvIzgkg2v"
                    },
                    "followers": {
                      "href": null,
                      "total": 39002
                    },
                    "genres": [
                      "alternative rock",
                      "rock"
                    ],
                    "href": "https://api.spotify.com/v1/artists/4LAz9VRX8Nat9kvIzgkg2v",
                    "id": "4LAz9VRX8Nat9kvIzgkg2v",
                    "images": [
                      {
                        "url": "https://i.scdn.co/image/ab6761610000e5ebfa16c573e959a8cec07c6441",
                        "height": 640,
                        "width": 640
                      },
                      {
                        "url": "https://i.scdn.co/image/ab67616100005174fa16c573e959a8cec07c6441",
                        "height": 320,
                        "width": 320
                      },
                      {
                        "url": "https://i.scdn.co/image/ab6761610000f178fa16c573e959a8cec07c6441",
                        "height": 160,
                        "width": 160
                      }
                    ],
                    "name": "Rivers Cuomo",
                    "popularity": 54,
                    "type": "artist",
                    "uri": "spotify:artist:4LAz9VRX8Nat9kvIzgkg2v"
                  }
                """);
        assertFalse(artistRepository.exists(apiArtist),
                "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");

        // Act
        var persistedImage = artistRepository.persist(apiArtist);

        // Assert
//        assertTrue(artistRepository.exists(apiArtist.getId()), "Can't find Artist by Spotify ID.");
        assertTrue(artistRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
        assertTrue(persistedImage.isPresent(), "persistedArtist is null");
//        assertTrue(artistRepository.exists(persistedImage.orElseThrow()), "Can't find Artist by Object reference.");
        // add assertions to check that images and genres can be accessed from persistedImage
    }

    @Test
    void ensure_artist_can_be_persisted2() {
        // Arrange
        final Artist apiArtist = new Artist.JsonUtil().createModelObject("""
                {
                    "external_urls": {
                      "spotify": "https://open.spotify.com/artist/4LAz9VRX8Nat9kvIzgkg2v2"
                    },
                    "followers": {
                      "href": null,
                      "total": 39002
                    },
                    "genres": [
                      "alternative rock2",
                      "rock"
                    ],
                    "href": "https://api.spotify.com/v1/artists/4LAz9VRX8Nat9kvIzgkg2v",
                    "id": "4LAz9VRX8Nat9kvIzgkg2v2",
                    "images": [
                      {
                        "url": "https://i.scdn.co/image/ab6761610000e5ebfa16c573e959a8cec07c64412",
                        "height": 640,
                        "width": 640
                      },
                      {
                        "url": "https://i.scdn.co/image/ab67616100005174fa16c573e959a8cec07c64412",
                        "height": 320,
                        "width": 320
                      },
                      {
                        "url": "https://i.scdn.co/image/ab6761610000f178fa16c573e959a8cec07c64412",
                        "height": 160,
                        "width": 160
                      }
                    ],
                    "name": "Rivers Cuomo2",
                    "popularity": 54,
                    "type": "artist",
                    "uri": "spotify:artist:4LAz9VRX8Nat9kvIzgkg2v2"
                  }
                """);
        assertFalse(artistRepository.exists(apiArtist),
                "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");

        // Act
        var persistedImage = artistRepository.persist(apiArtist);

        // Assert
//        assertTrue(artistRepository.exists(apiArtist.getId()), "Can't find Artist by Spotify ID.");
        assertTrue(artistRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
        assertTrue(persistedImage.isPresent(), "persistedArtist is null");
//        assertTrue(artistRepository.exists(persistedImage.orElseThrow()), "Can't find Artist by Object reference.");
        // add assertions to check that images and genres can be accessed from persistedImage
    }
}
