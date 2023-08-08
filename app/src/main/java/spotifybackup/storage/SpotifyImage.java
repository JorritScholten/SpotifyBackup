package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "SpotifyImage.countBy", query = "select count(i) from SpotifyImage i"),
        @NamedQuery(name = "SpotifyImage.findByUrl", query = "select i from SpotifyImage i where i.url = :url"),
        @NamedQuery(name = "SpotifyImage.findByUrlWH",
                query = "select i from SpotifyImage i where i.url = :url and i.width = :width and i.height = :height")
})
public class SpotifyImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @Column(nullable = false)
    private String url;

    @Column
    private Integer width, height;
}
