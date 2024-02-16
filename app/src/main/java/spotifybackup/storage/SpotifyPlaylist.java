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
    @JoinColumn(name = "owner_id", nullable = false)
    private SpotifyUser owner;

    @NonNull
    @Column(nullable = false)
    private String name;

    @Setter
    @Getter(AccessLevel.NONE)
    private String description;

    @NonNull
    private Boolean isCollaborative;

    @NonNull
    private Boolean isPublic;

    @NonNull
    private String snapshotId;

    @Setter
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
}
