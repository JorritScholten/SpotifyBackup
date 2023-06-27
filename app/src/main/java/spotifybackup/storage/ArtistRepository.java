package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class ArtistRepository {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("testdb");

    public Artist find(long id) {
        try (var entityManager = emf.createEntityManager()) {
            Artist artist = entityManager.find(Artist.class, id);
            return artist;
        }
    }

    public long count() {
        try (var entityManager = emf.createEntityManager()) {
            return (Long) entityManager.createNamedQuery("Artist.countBy").getSingleResult();
        }
    }

    public void seed() {
        try (var entityManager = emf.createEntityManager()) {
            var spotifyImage1 = new SpotifyImage.SpotifyImageBuilder() //0, new URI("localhost:~/test1"), 1, 2);
                    .url(new URI("localhost:~/test1"))
                    .width(1)
                    .height(2)
                    .build();
            var spotifyImage2 = new SpotifyImage.SpotifyImageBuilder() //2, new URI("localhost:~/test2"), 10, 20);
                    .url(new URI("localhost:~/test12"))
                    .width(11)
                    .height(20)
                    .build();
            Set<SpotifyImage> artistImages = new HashSet<>();
            artistImages.add(spotifyImage1);
            artistImages.add(spotifyImage2);
            var artist = new Artist();//123, "Test artist", new SpotifyID("0123456789abcdef"), artistImages);
            artist.setName("Test artist");
//            var id = new SpotifyID();
//            id.setId("0123456789abcdef");
            artist.setSpotifyID(new SpotifyID("0123456789abcdef"));
            artist.setImages(artistImages);
            entityManager.persist(artist);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
