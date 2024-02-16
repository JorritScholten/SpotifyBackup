package spotifybackup.storage;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public final class SpotifyTrack extends SpotifyObject {
    @ManyToMany(fetch = FetchType.LAZY, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyArtist> artists = new HashSet<>();

    @OneToMany(mappedBy = "track")
    private final Set<SpotifyPlaylistItem> playlistItems = new HashSet<>();

    @OneToMany(mappedBy = "track")
    private final Set<SpotifySavedTrack> savedTracks = new HashSet<>();

    @NonNull
    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private SpotifyAlbum album;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = "id", nullable = false)
    private SpotifyID spotifyID;

    @NonNull
    private Integer discNumber;

    @NonNull
    private Integer trackNumber;

    @NonNull
    private Integer durationMs;

    @NonNull
    private Boolean explicit;

    @Setter
    @Getter(AccessLevel.NONE)
    @Column(length = 12)
    private String isrcID;

    @Setter
    @Getter(AccessLevel.NONE)
    private String upcID;

    @Setter
    @Getter(AccessLevel.NONE)
    private String eanID;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @Type(StringArrayType.class)
    @Column(length = 2, columnDefinition = "varchar(2) ARRAY")
    // Array of ISO 3166-1 alpha-2 codes
    private String[] availableMarkets;

    @Setter
    @NonNull
    private Boolean isSimplified;

    public Optional<String> getIsrcID() {
        return Optional.ofNullable(isrcID);
    }

    public Optional<String> getUpcID() {
        return Optional.ofNullable(upcID);
    }

    public Optional<String> getEanID() {
        return Optional.ofNullable(eanID);
    }

    void addArtist(@NonNull SpotifyArtist newSpotifyArtist) {
        artists.add(newSpotifyArtist);
        newSpotifyArtist.addTrack(this);
    }

    void addArtists(@NonNull Set<SpotifyArtist> newSpotifyArtists) {
        newSpotifyArtists.forEach(this::addArtist);
    }
}
