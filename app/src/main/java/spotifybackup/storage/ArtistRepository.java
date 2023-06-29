package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ArtistRepository {
    private EntityManagerFactory emf; // = Persistence.createEntityManagerFactory("testdb");

    public ArtistRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory("testdb", DB_ACCESS);
    }

    public Artist find(long id) {
        try (var entityManager = emf.createEntityManager()) {
            return entityManager.find(Artist.class, id);
        }
    }

    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("Artist.countBy").getSingleResult();
        }
    }

    public void seed() {
        try (var entityManager = emf.createEntityManager()) {
            Set<SpotifyImage> artistImages = new HashSet<>();
            var spotifyImage1 = new SpotifyImage.SpotifyImageBuilder() //0, new URI("localhost:~/test1"), 1, 2);
                    .url(new URI("localhost:~/test1"))
                    .width(1)
                    .height(2)
                    .build();
            artistImages.add(spotifyImage1);
            var spotifyImage2 = new SpotifyImage.SpotifyImageBuilder() //2, new URI("localhost:~/test2"), 10, 20);
                    .url(new URI("localhost:~/test12"))
                    .width(11)
                    .height(20)
                    .build();
            artistImages.add(spotifyImage2);
            Set<Genre> genres = new HashSet<>();
            genres.add(new Genre("rock"));
            genres.add(new Genre("prog-rock"));
            genres.add(new Genre("alt-rock"));

            var artist = new Artist.ArtistBuilder() //123, "Test artist", new SpotifyID("0123456789abcdef"), artistImages);
                    .name("Test artist")
                    .spotifyID(new SpotifyID("0123456789abcdef"))
                    .images(artistImages)
                    .genres(genres)
                    .build();

            if (entityManager.find(SpotifyID.class, artist.getSpotifyID().getId()) == null) {
                entityManager.getTransaction().begin();
                entityManager.persist(artist);
                entityManager.flush();
                entityManager.getTransaction().commit();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
