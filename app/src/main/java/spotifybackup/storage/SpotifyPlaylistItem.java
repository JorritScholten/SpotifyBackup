package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
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

    @ManyToOne
    @JoinColumn(name = "added_by_user_id")
    private SpotifyUser addedBy;

    @Column(columnDefinition = "TIMESTAMP(0) WITH TIME ZONE")
    private ZonedDateTime dateAdded;

    public Optional<SpotifyUser> getAddedBy() {
        return Optional.ofNullable(addedBy);
    }

    public Optional<ZonedDateTime> getDateAdded() {
        return Optional.ofNullable(dateAdded);
    }
}
