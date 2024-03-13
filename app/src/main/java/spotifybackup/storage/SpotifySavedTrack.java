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
    @Setter(AccessLevel.PACKAGE)
    @Column(nullable = false)
    private ZonedDateTime dateAdded;

    @NonNull
    @Builder.Default
    @Setter(AccessLevel.PACKAGE)
    private Boolean isRemoved = false;

    @Setter(AccessLevel.PACKAGE)
    private ZonedDateTime dateRemoved;

    public Optional<ZonedDateTime> getDateRemoved() {
        return Optional.ofNullable(dateRemoved);
    }
}
