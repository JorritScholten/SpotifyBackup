package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@Entity(name = "Artist")
@NamedQueries({
        @NamedQuery(name = "Artist.countBy", query = "select count(a) from Artist a"),
        @NamedQuery(name = "Artist.findBySpotifyID", query = "select a from Artist a where a.spotifyID = :spotifyID")
})
@NoArgsConstructor
@Getter
//@Table(name = "artist")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = "id")
    private SpotifyID spotifyID;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<SpotifyImage> images;

    //    @JoinTable(
//            name = "artists_genres",
//            joinColumns = @JoinColumn(name = "artist_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id")
//    )
//    @Setter
    // more info: https://stackoverflow.com/a/59523218
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<Genre> genres = new HashSet<>();

    public void addGenres(@NonNull Set<Genre> newGenres) {
        for (var genre : newGenres) {
            genres.add(genre);
            genre.addArtist(this);
//            genre.getArtists().add(this);
        }
    }
}
