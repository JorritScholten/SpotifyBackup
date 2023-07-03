package spotifybackup.api_wrapper;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.io.IOException;

public class ApiWrapper {
    private final SpotifyApi spotifyApi;

    public ApiWrapper(SpotifyApi.Builder builder) {
        spotifyApi = builder.build();
    }

    public String getArtistName(String spotifyId) throws IOException {
        try {
            final Artist artist = spotifyApi.getArtist(spotifyId).build().execute();
            return artist.getName();
        } catch (SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
