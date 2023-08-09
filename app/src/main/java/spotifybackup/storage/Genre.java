package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity(name = "Genre")
@NoArgsConstructor
@Getter
@RequiredArgsConstructor
@ToString
//@Table(name = "genre")
@NamedQueries({
        @NamedQuery(name = "Genre.countBy", query = "select count(g) from Genre g"),
        @NamedQuery(name = "Genre.findByName", query = "select g from Genre g where g.name = :name")
})
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    @NonNull
    private String name;

    @Lob
    private String description;

//    @Setter
//    @JoinTable(
//            name = "artists_genres",
//            joinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "artist_id", referencedColumnName = "id")
//    )
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "genres", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private final Set<Artist> artists = new HashSet<>();

    public  void addArtist(@NonNull Artist artist){
        artists.add(artist);
    }

    /**
     * Factory method to create a Set of Genres from an array of genre names.
     * @param genreNames an array of genre names as defined by Spotify.
     * @return Set of Genres.
     */
    static Set<Genre> setFactory(@NonNull String[] genreNames) {
        Set<Genre> genreSet = new HashSet<>();
        for (var genreName : genreNames) {
            if(!genreName.isBlank()){
                genreSet.add(new Genre(genreName.toLowerCase(Locale.ENGLISH)));
            }
        }
        return genreSet;
    }
}
