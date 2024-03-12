package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public final class SpotifyArtist extends SpotifyObject {
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = SpotifyUser_.FOLLOWED_ARTISTS, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyUser> followers = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private final Set<SpotifyImage> images = new HashSet<>();

    // more info: https://stackoverflow.com/a/59523218
    @ManyToMany(fetch = FetchType.LAZY, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyGenre> genres = new HashSet<>();

    @Getter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = SpotifyAlbum_.ARTISTS, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyAlbum> albums = new HashSet<>();

    @Getter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = SpotifyTrack_.ARTISTS, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyTrack> tracks = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @Column(nullable = false, columnDefinition = "VARCHAR")
    private String name;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = SpotifyID_.ID, nullable = false)
    private SpotifyID spotifyID;

    @Setter
    @NonNull
    private Boolean isSimplified;

    void addGenres(@NonNull Set<SpotifyGenre> newSpotifyGenres) {
        for (var genre : newSpotifyGenres) {
            genres.add(genre);
            genre.addArtist(this);
        }
    }

    void addImages(@NonNull Set<SpotifyImage> newSpotifyImages) {
        images.addAll(newSpotifyImages);
    }

    /** Non-owning side. */
    void addTracks(@NonNull Set<SpotifyTrack> newSpotifyTracks) {
        tracks.addAll(newSpotifyTracks);
    }

    /** Non-owning side. */
    void addTrack(@NonNull SpotifyTrack newSpotifyTrack) {
        tracks.add(newSpotifyTrack);
    }

    /** Non-owning side. */
    void addAlbum(@NonNull SpotifyAlbum newSpotifyAlbum) {
        albums.add(newSpotifyAlbum);
    }

    /** Non-owning side. */
    public void addFollower(@NonNull SpotifyUser user) {
        followers.add(user);
    }

    /** Non-owning side. */
    public void removeFollower(@NonNull SpotifyUser user) {
        followers.remove(user);
    }
}
