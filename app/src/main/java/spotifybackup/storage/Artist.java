package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@NamedQueries({
        @NamedQuery(name = "Artist.countBy", query = "select count(a) from Artist a")
})
@NoArgsConstructor
@Getter
@Setter
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "spotify_id", referencedColumnName = "id")
    private SpotifyID spotifyID;

    @OneToMany
    @JoinColumn(nullable = true, name = "image_id", referencedColumnName = "id")
    private Set<SpotifyImage> images;
}
