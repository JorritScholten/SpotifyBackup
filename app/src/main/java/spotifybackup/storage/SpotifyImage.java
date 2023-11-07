package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;
import se.michaelthelin.spotify.model_objects.specification.Image;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Builder
@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
@ToString
@NamedQuery(name = "SpotifyImage.countBy", query = "select count(i) from SpotifyImage i")
@NamedQuery(name = "SpotifyImage.findByUrl", query = "select i from SpotifyImage i where i.url = :url")
@NamedQuery(name = "SpotifyImage.findByUrlWH",
        query = "select i from SpotifyImage i where i.url = :url and i.width = :width and i.height = :height")
public final class SpotifyImage extends SpotifyObject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @Column(nullable = false, unique = true)
    private String url;

    @Getter(AccessLevel.NONE)
    private Integer width;
    @Getter(AccessLevel.NONE)
    private Integer height;

    /**
     * Factory method to create a Set of SpotifyImages from an Image array.
     * @param images Array of Image objects generated by spotify-web-api.
     * @return Set of SpotifyImages.
     */
    static Set<SpotifyImage> setFactory(@NonNull Image[] images) {
        Set<SpotifyImage> imageSet = new HashSet<>();
        for (var image : images) {
            var newSpotifyImage = SpotifyImage.builder()
                    .url(image.getUrl())
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .build();
            imageSet.add(newSpotifyImage);
        }
        return imageSet;
    }

    public Optional<Integer> getHeight() {
        return height.describeConstable();
    }

    public Optional<Integer> getWidth() {
        return width.describeConstable();
    }
}
