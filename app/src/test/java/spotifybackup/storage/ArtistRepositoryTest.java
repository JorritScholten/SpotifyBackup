package spotifybackup.storage;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

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
        var persistedArtist = artistRepository.persist(apiArtist);

        // Assert
        assertTrue(artistRepository.exists(apiArtist.getId()), "Can't find Artist by Spotify ID.");
        assertTrue(artistRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
        assertTrue(persistedArtist.isPresent(), "persistedArtist is null");
        assertTrue(artistRepository.exists(persistedArtist.orElseThrow()), "Can't find Artist by Object reference.");
        assertEquals(apiArtist.getGenres().length, persistedArtist.get().getGenres().size());
        assertEquals(apiArtist.getImages().length, persistedArtist.get().getImages().size());
    }

    @Test
    void ensure_multiple_artists_can_be_persisted() {
        // Arrange
        final long oldCount = artistRepository.count();
        final Artist[] apiArtists = {new Artist.JsonUtil().createModelObject("""
                {
                  "external_urls": {
                    "spotify": "https://open.spotify.com/artist/5BcAKTbp20cv7tC5VqPFoC"
                  },
                  "followers": {
                    "href": null,
                    "total": 2642025
                  },
                  "genres": [
                    "pop rap",
                    "seattle hip hop"
                  ],
                  "href": "https://api.spotify.com/v1/artists/5BcAKTbp20cv7tC5VqPFoC",
                  "id": "5BcAKTbp20cv7tC5VqPFoC",
                  "images": [
                    {
                      "height": 640,
                      "url": "https://i.scdn.co/image/ab6761610000e5eb3c2f1ba4c1a3de141e63d775",
                      "width": 640
                    },
                    {
                      "height": 320,
                      "url": "https://i.scdn.co/image/ab676161000051743c2f1ba4c1a3de141e63d775",
                      "width": 320
                    },
                    {
                      "height": 160,
                      "url": "https://i.scdn.co/image/ab6761610000f1783c2f1ba4c1a3de141e63d775",
                      "width": 160
                    }
                  ],
                  "name": "Macklemore & Ryan Lewis",
                  "popularity": 72,
                  "type": "artist",
                  "uri": "spotify:artist:5BcAKTbp20cv7tC5VqPFoC"
                }
                """), new Artist.JsonUtil().createModelObject("""
                {
                  "external_urls": {
                    "spotify": "https://open.spotify.com/artist/3JhNCzhSMTxs9WLGJJxWOY"
                  },
                  "followers": {
                    "href": null,
                    "total": 2103751
                  },
                  "genres": [
                    "pop rap",
                    "seattle hip hop"
                  ],
                  "href": "https://api.spotify.com/v1/artists/3JhNCzhSMTxs9WLGJJxWOY",
                  "id": "3JhNCzhSMTxs9WLGJJxWOY",
                  "images": [
                    {
                      "height": 640,
                      "url": "https://i.scdn.co/image/ab6761610000e5eb488b3b242f97163a7f8e17b9",
                      "width": 640
                    },
                    {
                      "height": 320,
                      "url": "https://i.scdn.co/image/ab67616100005174488b3b242f97163a7f8e17b9",
                      "width": 320
                    },
                    {
                      "height": 160,
                      "url": "https://i.scdn.co/image/ab6761610000f178488b3b242f97163a7f8e17b9",
                      "width": 160
                    }
                  ],
                  "name": "Macklemore",
                  "popularity": 75,
                  "type": "artist",
                  "uri": "spotify:artist:3JhNCzhSMTxs9WLGJJxWOY"
                }
                """), new Artist.JsonUtil().createModelObject("""
                {
                  "external_urls": {
                    "spotify": "https://open.spotify.com/artist/4myTppRgh0rojLxx8RycOp"
                  },
                  "followers": {
                    "href": null,
                    "total": 19410
                  },
                  "genres": [
                    "pop rap"
                  ],
                  "href": "https://api.spotify.com/v1/artists/4myTppRgh0rojLxx8RycOp",
                  "id": "4myTppRgh0rojLxx8RycOp",
                  "images": [
                    {
                      "height": 640,
                      "url": "https://i.scdn.co/image/ab6761610000e5eb22a807b9b7c5d50042db2657",
                      "width": 640
                    },
                    {
                      "height": 320,
                      "url": "https://i.scdn.co/image/ab6761610000517422a807b9b7c5d50042db2657",
                      "width": 320
                    },
                    {
                      "height": 160,
                      "url": "https://i.scdn.co/image/ab6761610000f17822a807b9b7c5d50042db2657",
                      "width": 160
                    }
                  ],
                  "name": "Ryan Lewis",
                  "popularity": 72,
                  "type": "artist",
                  "uri": "spotify:artist:4myTppRgh0rojLxx8RycOp"
                }
                """),
        };
        for (var apiArtist : apiArtists) {
            assertFalse(artistRepository.exists(apiArtist),
                    "Artist with Spotify ID " + apiArtist.getId() + " shouldn't already exist.");
        }

        // Act
        var persistedArtists = artistRepository.persistAll(apiArtists);

        // Assert
        for (var apiArtist : apiArtists) {
            var persistedArtist = persistedArtists.stream()
                    .filter(a -> a.getSpotifyID().getId().equals(apiArtist.getId()))
                    .findAny();
            assertTrue(artistRepository.exists(apiArtist.getId()), "Can't find Artist by Spotify ID.");
            assertTrue(artistRepository.exists(apiArtist), "Can't find Artist by apiArtist/Spotify ID.");
            assertTrue(persistedArtist.isPresent());
            assertTrue(artistRepository.exists(persistedArtist.get()), "Can't find Artist by Object reference.");
            assertEquals(apiArtist.getGenres().length, persistedArtist.get().getGenres().size());
            assertEquals(apiArtist.getImages().length, persistedArtist.get().getImages().size());
        }
        assertEquals(oldCount + apiArtists.length, artistRepository.count());
    }
}
