package spotifybackup.storage;

import io.hypersistence.utils.hibernate.type.array.LongArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.type.descriptor.jdbc.ArrayJdbcType;
import org.hibernate.type.descriptor.jdbc.BigIntJdbcType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;
import se.michaelthelin.spotify.enums.AlbumType;
import se.michaelthelin.spotify.enums.ReleaseDatePrecision;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public final class SpotifyAlbum extends SpotifyObject {
    @OneToMany(mappedBy = "album")
    private final Set<SpotifyTrack> tracks = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final Set<SpotifyImage> images = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    private final Set<SpotifyArtist> artists = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyGenre> genres = new HashSet<>();

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
    private String upcID;

    @Setter
    private String eanID;

    @NonNull
    @Column(columnDefinition = "VARBINARY")
    @Convert(converter = AvailableMarketsConverter.class)
    private AvailableMarkets availableMarkets;

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
        artists.add(newSpotifyArtist);
        newSpotifyArtist.addAlbum(this);
    }

    void addGenres(@NonNull Set<SpotifyGenre> newSpotifyGenres) {
        for (var newGenre : newSpotifyGenres) {
            genres.add(newGenre);
            newGenre.addAlbum(this);
        }
    }

    void addImages(@NonNull Set<SpotifyImage> newSpotifyImages) {
        images.addAll(newSpotifyImages);
    }

    /** Non-owning side. */
    void addTracks(@NonNull Set<SpotifyTrack> newSpotifyTracks) {
        tracks.addAll(newSpotifyTracks);
    }

    /** Non-owning side. */
    void addTrack(@NonNull SpotifyTrack newSpotifyTrack) {
        tracks.add(newSpotifyTrack);
    }
}
