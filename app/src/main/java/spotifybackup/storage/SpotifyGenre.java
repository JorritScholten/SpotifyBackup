package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@NoArgsConstructor
@Getter
@RequiredArgsConstructor
@ToString
@Entity
public final class SpotifyGenre extends SpotifyObject {
    @Getter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = SpotifyArtist_.GENRES,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private final Set<SpotifyArtist> artists = new HashSet<>();

    @Getter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = SpotifyAlbum_.GENRES,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private final Set<SpotifyAlbum> albums = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR")
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

    /** Non-owning side. */
    void addArtist(@NonNull SpotifyArtist spotifyArtist) {
        artists.add(spotifyArtist);
    }

    /** Non-owning side. */
    void addAlbum(@NonNull SpotifyAlbum spotifyAlbum) {
        albums.add(spotifyAlbum);
    }
}
