package spotifybackup.storage;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import se.michaelthelin.spotify.enums.AlbumType;
import se.michaelthelin.spotify.enums.ReleaseDatePrecision;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@Entity(name = "SpotifyAlbum")
@NamedQuery(name = "SpotifyAlbum.countBy", query = "select count(a) from SpotifyAlbum a")
@NamedQuery(name = "SpotifyAlbum.findBySpotifyID", query = "select a from SpotifyAlbum a where a.spotifyID = :spotifyID")
@NoArgsConstructor
@Getter
public final class SpotifyAlbum extends SpotifyObject {
    @OneToMany(mappedBy = "spotifyAlbum")
    private final Set<SpotifyTrack> spotifyTracks = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final Set<SpotifyImage> spotifyImages = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private final Set<SpotifyArtist> spotifyArtists = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyGenre> spotifyGenres = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "spotify_id", referencedColumnName = "id", nullable = false)
    private SpotifyID spotifyID;

    @Setter
    @Column(length = 12)
    private String isrcID;

    @Setter
    @Column(length = 12)
    private String upcID;

    @Setter
    @Column(length = 13)
    private String eanID;

    @Type(StringArrayType.class)
    @Column(length = 2, columnDefinition = "varchar(2) ARRAY")
    // Array of ISO 3166-1 alpha-2 codes
    private String[] availableMarkets;

    @NonNull
    @Enumerated(EnumType.STRING)
    private AlbumType spotifyAlbumType;

    @NonNull
    @Column(columnDefinition = "DATE")
    private LocalDate releaseDate;

    @NonNull
    @Enumerated(EnumType.STRING)
    private ReleaseDatePrecision releaseDatePrecision;

    @Setter
    @NonNull
    private Boolean isSimplified;

    void addArtist(@NonNull Set<SpotifyArtist> newSpotifyArtists) {
        newSpotifyArtists.forEach(this::addArtist);
    }

    void addArtist(@NonNull SpotifyArtist newSpotifyArtist) {
        spotifyArtists.add(newSpotifyArtist);
        newSpotifyArtist.addAlbum(this);
    }

    void addGenres(@NonNull Set<SpotifyGenre> newSpotifyGenres) {
        for (var newGenre : newSpotifyGenres) {
            spotifyGenres.add(newGenre);
            newGenre.addAlbum(this);
        }
    }

    void addImages(@NonNull Set<SpotifyImage> newSpotifyImages) {
        spotifyImages.addAll(newSpotifyImages);
    }

    /** Non-owning side. */
    void addTracks(@NonNull Set<SpotifyTrack> newSpotifyTracks) {
        spotifyTracks.addAll(newSpotifyTracks);
    }

    /** Non-owning side. */
    void addTrack(@NonNull SpotifyTrack newSpotifyTrack) {
        spotifyTracks.add(newSpotifyTrack);
    }
}
