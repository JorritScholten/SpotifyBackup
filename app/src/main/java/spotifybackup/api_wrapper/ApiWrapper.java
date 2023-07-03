package spotifybackup.api_wrapper;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

public class ApiWrapper {
    private final SpotifyApi spotifyApi;

    public ApiWrapper(SpotifyApi.Builder builder) {
        spotifyApi = builder.build();
    }

    public void authorizationCodeUri_Sync() throws IOException {
        //open redirect uri in browser or local window
        try {
            final URI uri = spotifyApi.authorizationCodeUri().build().execute();
            // execute above URL by opening browser window with it
            System.out.println("URI: " + uri.toString());
            Desktop.getDesktop().browse(uri);
            // host https:localhost:8888/callback RESTfull webserver to catch callback code

        } catch (CompletionException e) {
            System.out.println("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            System.out.println("Async operation cancelled.");
        }
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
