package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@Entity
@NamedQueries({
        @NamedQuery(name = "Artist.countBy", query = "select count(a) from Artist a")
})
@NoArgsConstructor
@Getter
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = "id")
    private SpotifyID spotifyID;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<SpotifyImage> images;

    // more info: https://stackoverflow.com/a/59523218
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinColumn(nullable = true, name = "genre_id", referencedColumnName = "id")
    private Set<Genre> genres;
}
