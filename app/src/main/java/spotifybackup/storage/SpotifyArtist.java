package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@Entity(name = "SpotifyArtist")
@NamedQueries({
        @NamedQuery(name = "SpotifyArtist.countBy", query = "select count(a) from SpotifyArtist a"),
        @NamedQuery(name = "SpotifyArtist.findBySpotifyID", query = "select a from SpotifyArtist a where a.spotifyID = :spotifyID")
})
@NoArgsConstructor
@Getter
public class SpotifyArtist {
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final Set<SpotifyImage> spotifyImages = new HashSet<>();

    // more info: https://stackoverflow.com/a/59523218
    @ManyToMany(fetch = FetchType.EAGER, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyGenre> spotifyGenres = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "spotifyArtists", cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyAlbum> spotifyAlbums = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "spotifyArtists", cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyTrack> spotifyTracks = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = "id", nullable = false)
    private SpotifyID spotifyID;

    @Setter
    @NonNull
    private Boolean isSimplified;

    void addGenres(@NonNull Set<SpotifyGenre> newSpotifyGenres) {
        for (var genre : newSpotifyGenres) {
            spotifyGenres.add(genre);
            genre.addArtist(this);
        }
    }

    void addImages(@NonNull Set<SpotifyImage> newImages) {
        spotifyImages.addAll(newImages);
    }

    void addTracks(){

    }

    void addAlbums(){

    }
}
