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
    @Getter(AccessLevel.NONE)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final Set<SpotifyImage> images = new HashSet<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = SpotifyPlaylistItem_.ADDED_BY, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final Set<SpotifyPlaylistItem> addedPlaylistItems = new HashSet<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = SpotifySavedTrack_.USER, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final Set<SpotifySavedTrack> savedTracks = new HashSet<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = SpotifyPlaylist_.OWNER, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final Set<SpotifyPlaylist> ownedPlaylists = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @Column(nullable = false, unique = true)
    private String spotifyUserID;

    private String displayName;

    @Column(columnDefinition = "varchar(2)")
    private String countryCode;

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
