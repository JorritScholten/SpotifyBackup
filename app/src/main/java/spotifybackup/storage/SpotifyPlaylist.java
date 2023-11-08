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
@Entity(name = "SpotifyPlaylist")
@NamedQuery(name = "SpotifyPlaylist.countBy", query = "select count(p) from SpotifyPlaylist p")
@NamedQuery(name = "SpotifyPlaylist.findBySpotifyID", query = "select p from SpotifyPlaylist p where p.spotifyID = :spotifyID")
@NoArgsConstructor
public final class SpotifyPlaylist extends SpotifyObject {
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

    @Setter
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

    @Setter
    @NonNull
    @Column(name = "simplified")
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
}
