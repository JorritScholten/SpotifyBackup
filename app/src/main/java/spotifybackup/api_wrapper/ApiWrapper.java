package spotifybackup.api_wrapper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

public class ApiWrapper {
    private final SpotifyApi spotifyApi;
    static private boolean callbackTriggered = false;
    static private Optional<String> code;
    static private String state = UUID.randomUUID().toString();

    public ApiWrapper(SpotifyApi.Builder builder) {
        spotifyApi = builder.build();
    }

    public void authorizationCodeUri_Sync() throws IOException {
        //open redirect uri in browser or local window
        try {
            final var authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                    .state(state)
                    .build();
            final URI uri = authorizationCodeUriRequest.execute();
            // host https:localhost:8888/callback RESTfull webserver to catch callback code
            var redirectUri = spotifyApi.getRedirectURI();
            HttpServer server = HttpServer.create(new InetSocketAddress(redirectUri.getHost(), redirectUri.getPort()), 0);
            server.createContext(redirectUri.getPath(), new CallbackHandler());
            server.setExecutor(null);
            server.start();

            // execute above URL by opening browser window with it
            System.out.println("URI: " + uri.toString());
            Desktop.getDesktop().browse(uri);

            System.out.println("waiting for callback");
            while (!callbackTriggered) {
                Thread.sleep(10);
            }
            server.stop(0);
            if (code.isEmpty()) {
                System.err.println("callback triggered without valid code return.");
            } else {
                System.out.println("found code: " + code.get());
                final var authorizationCodeRequest = spotifyApi.authorizationCode(code.get()).build();
                final var authorizationCode = authorizationCodeRequest.execute();
                spotifyApi.setAccessToken(authorizationCode.getAccessToken());
                spotifyApi.setRefreshToken(authorizationCode.getRefreshToken());
            }
        } catch (CompletionException e) {
            System.out.println("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            System.out.println("Async operation cancelled.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (SpotifyWebApiException e) {
            throw new RuntimeException(e);
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

    static class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Response from callback.";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();

            // code is contained in URI query
            System.out.println("callback header: " + t.getRequestURI().getQuery());
            var matcher = Pattern.compile("state=(?<state>[^&]*)&?")
                    .matcher(t.getRequestURI().getQuery());
            if (matcher.find() && state.equals(matcher.group("state"))) {
                matcher = Pattern.compile("code=(?<code>[^&]*)&?")
                        .matcher(t.getRequestURI().getQuery());
                matcher.find();
                code = Optional.ofNullable(matcher.group("code"));
            } else {
                code = Optional.empty();
            }
            callbackTriggered = true;
        }
    }
}
