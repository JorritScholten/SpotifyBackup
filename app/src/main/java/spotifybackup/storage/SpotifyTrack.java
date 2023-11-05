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
@Entity(name = "SpotifyTrack")
@NamedQuery(name = "SpotifyTrack.countBy", query = "select count(t) from SpotifyTrack t")
@NamedQuery(name = "SpotifyTrack.findBySpotifyID", query = "select t from SpotifyTrack t where t.spotifyID = :spotifyID")
@NoArgsConstructor
@Getter
public final class SpotifyTrack extends SpotifyObject {
    @ManyToMany(fetch = FetchType.EAGER, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyArtist> spotifyArtists = new HashSet<>();

    @NonNull
    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private SpotifyAlbum spotifyAlbum;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = "id", nullable = false)
    private SpotifyID spotifyID;

    @NonNull
    @Column(name = "disc_number")
    private Integer discNumber;

    @NonNull
    @Column(name = "track_number")
    private Integer trackNumber;

    @NonNull
    @Column(name = "duration_ms")
    private Integer durationMs;

    @NonNull
    private Boolean explicit;

    @Setter
    @Getter(AccessLevel.NONE)
    @Column(length = 12, name = "ISRC")
    private String isrcID;

    @Setter
    @Getter(AccessLevel.NONE)
    @Column(name = "UPC")
    private String upcID;

    @Setter
    @Getter(AccessLevel.NONE)
    @Column(name = "EAN")
    private String eanID;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @Type(StringArrayType.class)
    @Column(length = 2, columnDefinition = "varchar(2) ARRAY", name = "available_markets")
    // Array of ISO 3166-1 alpha-2 codes
    private String[] availableMarkets;

    @Setter
    @NonNull
    @Column(name = "simplified")
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
        spotifyArtists.add(newSpotifyArtist);
        newSpotifyArtist.addTrack(this);
    }

    void addArtists(@NonNull Set<SpotifyArtist> newSpotifyArtists) {
        newSpotifyArtists.forEach(this::addArtist);
    }
}
