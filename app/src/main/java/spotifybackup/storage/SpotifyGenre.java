package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity(name = "SpotifyGenre")
@NoArgsConstructor
@Getter
@RequiredArgsConstructor
@ToString
@NamedQueries({
        @NamedQuery(name = "SpotifyGenre.countBy", query = "select count(g) from SpotifyGenre g"),
        @NamedQuery(name = "SpotifyGenre.findByName", query = "select g from SpotifyGenre g where g.name = :name")
})
public class SpotifyGenre {
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "spotifyGenres",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private final Set<SpotifyArtist> spotifyArtists = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "spotifyGenres",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private final Set<SpotifyAlbum> spotifyAlbums = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    @NonNull
    private String name;

    /**
     * Factory method to create a Set of Genres from an array of genre names.
     * @param genreNames an array of genre names as defined by Spotify.
     * @return Set of SpotifyGenres.
     */
    static Set<SpotifyGenre> setFactory(@NonNull String[] genreNames) {
        Set<SpotifyGenre> spotifyGenreSet = new HashSet<>();
        for (var genreName : genreNames) {
            if (!genreName.isBlank()) {
                spotifyGenreSet.add(new SpotifyGenre(genreName.toLowerCase(Locale.ENGLISH)));
            }
        }
        return spotifyGenreSet;
    }

    void addArtist(@NonNull SpotifyArtist spotifyArtist) {
        spotifyArtists.add(spotifyArtist);
    }
}