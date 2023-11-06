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
@Entity(name = "SpotifyUser")
@NamedQuery(name = "SpotifyUser.countBy", query = "select count(u) from SpotifyUser u")
@NamedQuery(name = "SpotifyUser.findBySpotifyUserID", query = "select u from SpotifyUser u where u.spotifyUserID = :spotifyUserID")
@NoArgsConstructor
public final class SpotifyUser extends SpotifyObject {
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final Set<SpotifyImage> images = new HashSet<>();

    @OneToMany(mappedBy = "addedBy")
    private final Set<SpotifyPlaylistItem> playlistItems = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private final Set<SpotifyPlaylist> playlists = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NonNull
    @Column(name = "spotify_user_id", nullable = false)
    private String spotifyUserID;

    @Getter(AccessLevel.NONE)
    @Column(name = "display_name")
    private String displayName;

    @Getter(AccessLevel.NONE)
    @Column(columnDefinition = "varchar(2)")
    private String countryCode;

    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
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
