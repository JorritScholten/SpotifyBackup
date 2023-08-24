package spotifybackup.storage;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@Entity(name = "SpotifyTrack")
@NamedQueries({
        @NamedQuery(name = "SpotifyTrack.countBy", query = "select count(t) from SpotifyTrack t"),
        @NamedQuery(name = "SpotifyTrack.findBySpotifyID", query = "select t from SpotifyTrack t where t.spotifyID = :spotifyID")
})
@NoArgsConstructor
@Getter
public class SpotifyTrack {
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
    private Integer discNumber;

    @NonNull
    private Integer duration_ms;

    @NonNull
    private Boolean explicit;

    @NonNull
    @Column(length = 12, nullable = false)
    private String isrcID;

    @NonNull
    @Column(nullable = false)
    private String name;

    @Type(StringArrayType.class)
    @Column(length = 2, columnDefinition = "varchar(2) ARRAY")
    // Array of ISO 3166-1 alpha-2 codes
    private String[] availableMarkets;

    @Setter
    @NonNull
    private Boolean isSimplified;

    void addArtist(@NonNull SpotifyArtist newSpotifyArtist) {
        spotifyArtists.add(newSpotifyArtist);
        newSpotifyArtist.addTrack(this);
    }

    void addArtists(@NonNull Set<SpotifyArtist> newSpotifyArtists) {
        newSpotifyArtists.forEach(this::addArtist);
    }
}
