package spotifybackup.storage;

public sealed abstract class SpotifyObject permits SpotifyGenre, SpotifyImage, SpotifyID, SpotifyArtist, SpotifyAlbum {
}
