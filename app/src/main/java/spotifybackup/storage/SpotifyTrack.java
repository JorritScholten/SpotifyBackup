package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import io.hypersistence.utils.hibernate.type.array.LongArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.LongArrayTypeDescriptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CollectionType;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.java.BasicJavaType;
import org.hibernate.type.descriptor.java.LongPrimitiveArrayJavaType;
import org.hibernate.type.descriptor.java.ObjectJavaType;
import org.hibernate.type.descriptor.jdbc.*;

import java.util.EnumSet;
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

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "track")
    private final Set<SpotifyPlaylistItem> playlistItems = new HashSet<>();

    @Getter(AccessLevel.NONE)
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
    @Column(length = 12)
    private String isrcID;

    @Setter
    private String upcID;

    @Setter
    private String eanID;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @Column(columnDefinition = "VARBINARY")
    @Convert(converter = AvailableMarketsConverter.class)
    private AvailableMarkets availableMarkets;

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
