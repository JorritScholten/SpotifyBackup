package spotifybackup.storage;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import se.michaelthelin.spotify.enums.AlbumType;
import se.michaelthelin.spotify.enums.ReleaseDatePrecision;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
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
    @Type(StringArrayType.class)
    @Column(length = 2, columnDefinition = "varchar(2) ARRAY", name = "available_markets")
    // Array of ISO 3166-1 alpha-2 codes
    private String[] availableMarkets;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "spotify_album_type")
    private AlbumType spotifyAlbumType;

    @NonNull
    @Column(columnDefinition = "DATE", name = "release_date")
    private LocalDate releaseDate;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "release_date_precision")
    private ReleaseDatePrecision releaseDatePrecision;

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
