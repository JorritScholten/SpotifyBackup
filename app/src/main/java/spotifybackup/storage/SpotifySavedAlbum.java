package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public final class SpotifySavedAlbum extends SpotifyObject{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private SpotifyAlbum album;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private SpotifyUser user;

    @NonNull
    @Setter(AccessLevel.PACKAGE)
    @Column(columnDefinition = "TIMESTAMP(0) WITH TIME ZONE", nullable = false)
    private ZonedDateTime dateAdded;
}
