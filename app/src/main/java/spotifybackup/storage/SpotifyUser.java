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
    @Getter(AccessLevel.PACKAGE)
    @ManyToMany(fetch = FetchType.LAZY, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyPlaylist> followedPlaylists = new HashSet<>();

    @Getter(AccessLevel.PACKAGE)
    @ManyToMany(fetch = FetchType.LAZY, cascade =
            {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH})
    private final Set<SpotifyArtist> followedArtists = new HashSet<>();

    @Getter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY)
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
    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR")
    private String spotifyUserID;

    @Column(columnDefinition = "VARCHAR")
    private String displayName;

    @Column(columnDefinition = "VARCHAR(2)")
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

    void addFollowedPlaylist(@NonNull SpotifyPlaylist newPlaylist) {
        followedPlaylists.add(newPlaylist);
        newPlaylist.addFollower(this);
    }

    void addFollowedPlaylists(@NonNull Set<SpotifyPlaylist> newPlaylists) {
        newPlaylists.forEach(this::addFollowedPlaylist);
    }

    public void removeFollowedPlaylist(@NonNull SpotifyPlaylist playlist) {
        followedPlaylists.remove(playlist);
        playlist.removeFollower(this);
    }

    public void removeFollowedPlaylists(@NonNull Set<SpotifyPlaylist> playlists) {
        playlists.forEach(this::removeFollowedPlaylist);
    }

    void addFollowedArtist(@NonNull SpotifyArtist newArtist) {
        followedArtists.add(newArtist);
        newArtist.addFollower(this);
    }

    void addFollowedArtists(@NonNull Set<SpotifyArtist> newArtists) {
        newArtists.forEach(this::addFollowedArtist);
    }

    public void removeFollowedArtist(@NonNull SpotifyArtist artist) {
        followedArtists.remove(artist);
        artist.removeFollower(this);
    }

    public void removeFollowedArtists(@NonNull Set<SpotifyArtist> artists) {
        artists.forEach(this::removeFollowedArtist);
    }
}
