package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;
import se.michaelthelin.spotify.enums.ProductType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public final class SpotifyUser extends SpotifyObject {
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final Set<SpotifyImage> images = new HashSet<>();

    @OneToMany(mappedBy = "addedBy")
    private final Set<SpotifyPlaylistItem> addedPlaylistItems = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private final Set<SpotifySavedTrack> savedTracks = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private final Set<SpotifyPlaylist> ownedPlaylists = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @Column(nullable = false, unique = true)
    private String spotifyUserID;

    @Getter(AccessLevel.NONE)
    private String displayName;

    @Getter(AccessLevel.NONE)
    @Column(columnDefinition = "varchar(2)")
    private String countryCode;

    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    public Optional<ProductType> getProductType() {
        return Optional.ofNullable(productType);
    }

    public Optional<String> getCountryCode() {
        return Optional.ofNullable(countryCode);
    }

    public Optional<String> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    void addImages(@NonNull Set<SpotifyImage> newSpotifyImages) {
        images.addAll(newSpotifyImages);
    }
}
