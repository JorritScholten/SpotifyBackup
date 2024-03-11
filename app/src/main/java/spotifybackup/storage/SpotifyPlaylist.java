package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public final class SpotifyPlaylist extends SpotifyObject {
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = SpotifyUser_.FOLLOWED_PLAYLISTS, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyUser> followers = new HashSet<>();

    @OneToMany(mappedBy = SpotifyPlaylistItem_.PLAYLIST)
    private final Set<SpotifyPlaylistItem> tracks = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = SpotifyID_.ID, nullable = false)
    private SpotifyID spotifyID;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private SpotifyUser owner;

    @Setter(AccessLevel.PACKAGE)
    @NonNull
    @Column(nullable = false)
    private String name;

    @Setter(AccessLevel.PACKAGE)
    private String description;

    @Setter(AccessLevel.PACKAGE)
    @NonNull
    private Boolean isCollaborative;

    @Setter(AccessLevel.PACKAGE)
    @NonNull
    private Boolean isPublic;

    @Setter(AccessLevel.PACKAGE)
    @NonNull
    private String snapshotId;

    @Setter(AccessLevel.PACKAGE)
    @NonNull
    private Boolean isSimplified;

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    /** Non-owning side. */
    void addPlaylistItem(@NonNull SpotifyPlaylistItem newPlaylistItem) {
        tracks.add(newPlaylistItem);
    }

    /** Non-owning side. */
    void addPlaylistItems(@NonNull List<SpotifyPlaylistItem> newPlaylistItems) {
        newPlaylistItems.forEach(this::addPlaylistItem);
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
