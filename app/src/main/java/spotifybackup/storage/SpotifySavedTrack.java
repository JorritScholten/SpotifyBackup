package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "SpotifySavedTrack")
public final class SpotifySavedTrack extends SpotifyObject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "track_id", nullable = false)
    private SpotifyTrack track;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private SpotifyUser user;

    @NonNull
    @Column(columnDefinition = "TIMESTAMP(0) WITH TIME ZONE", nullable = false)
    private ZonedDateTime dateAdded;
}
