package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "SpotifyPlaylistItem")
@NamedQuery(name = "SpotifyPlaylistItem.countBy", query = "select count(i) from SpotifyPlaylistItem i")
@NamedQuery(name = "SpotifyPlaylistItem.findByPlaylistIDAndTrackID",
        query = "select i from SpotifyPlaylistItem i where i.playlist = :playlistID and i.track = :trackID")
@NamedQuery(name = "SpotifyPlaylistItem.findByPlaylistIDAndTrackIDAndUserID",
        query = "select i from SpotifyPlaylistItem i where i.playlist = :playlistID and i.track = :trackID and i.addedBy = :addedBy")
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
    @Column(columnDefinition = "TIMESTAMP(0) WITH TIME ZONE", name = "date_added")
    private ZonedDateTime dateAdded;

    public Optional<SpotifyUser> getAddedBy() {
        return Optional.ofNullable(addedBy);
    }

    public Optional<ZonedDateTime> getDateAdded() {
        return Optional.ofNullable(dateAdded);
    }
}
