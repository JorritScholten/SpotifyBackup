package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "SpotifyPlaylistItem")
@NamedQuery(name = "SpotifyPlaylistItem.findByPlaylistID", query = "select i from SpotifyPlaylistItem i where i.playlist = :playlistID")
@NoArgsConstructor
public final class SpotifyPlaylistItem extends SpotifyObject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "track_id", nullable = false)
    private SpotifyTrack track;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "playlist_id", nullable = false)
    private SpotifyPlaylist playlist;

    @Getter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "added_by_user_id")
    private SpotifyUser addedBy;

    @Getter(AccessLevel.NONE)
    @Column(columnDefinition = "DATE", name = "date_added")
    private LocalDate dateAdded;

    public Optional<SpotifyUser> getAddedBy() {
        return Optional.ofNullable(addedBy);
    }

    public Optional<LocalDate> getDateAdded() {
        return Optional.ofNullable(dateAdded);
    }
}
