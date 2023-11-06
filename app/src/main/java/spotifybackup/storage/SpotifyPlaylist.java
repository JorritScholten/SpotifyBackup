package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "SpotifyPlaylist")
@NamedQuery(name = "SpotifyPlaylist.countBy", query = "select count(p) from SpotifyPlaylist p")
@NamedQuery(name = "SpotifyPlaylist.findBySpotifyID", query = "select p from SpotifyPlaylist p where p.spotifyID = :spotifyID")
@NoArgsConstructor
public final class SpotifyPlaylist extends SpotifyObject {
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final Set<SpotifyImage> images = new HashSet<>();

    @OneToMany(mappedBy = "playlist")
    private final Set<SpotifyPlaylistItem> tracks = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = "id", nullable = false)
    private SpotifyID spotifyID;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private SpotifyUser owner;

    @NonNull
    @Column(nullable = false)
    private String name;

    @Getter(AccessLevel.NONE)
    private String description;

    @NonNull
    @Column(name = "collaborative")
    private Boolean isCollaborative;

    @NonNull
    @Column(name = "public")
    private Boolean isPublic;

    @NonNull
    @Column(name = "snapshot_id")
    private String snapshotId;

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }
}
