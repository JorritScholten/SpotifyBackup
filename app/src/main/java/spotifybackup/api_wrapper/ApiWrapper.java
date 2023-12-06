package spotifybackup.api_wrapper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.NonNull;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.AbstractRequest;
import spotifybackup.app.Config;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ApiWrapper {
    private final SpotifyApi spotifyApi;
    private final Semaphore waitingForAPI = new Semaphore(1);
    private final String state = UUID.randomUUID().toString();
    private final CallbackHandler callbackHandler = new CallbackHandler();
    private final ScheduledExecutorService tokenRefresh = Executors.newScheduledThreadPool(1);
    // Strategy patterns
    private final Supplier<AbstractRequest<AuthorizationCodeCredentials>> authorizationRefreshRequest;
    private final AbstractRequest<URI> authorizationCodeUriRequest;
    private final Function<String, AbstractRequest<AuthorizationCodeCredentials>> authorizationCodeRequest;

    /**
     * @throws InterruptedException when there is an error with acquiring the API handling semaphore.
     * @throws IOException          when an issue occurs with creating the redirect catch server or there is a network
     *                              issue (HTTP 3xx status code).
     */
    public ApiWrapper() throws InterruptedException, IOException {
        var apiBuilder = SpotifyApi.builder();
        apiBuilder.setClientId(Config.clientId.get());
        apiBuilder.setRedirectUri(Config.redirectURI.get());
        if (Config.clientSecret.get().isPresent()) apiBuilder.setClientSecret(Config.clientSecret.get().get());
        spotifyApi = apiBuilder.build();
        try {
            if (Config.clientSecret.get().isEmpty()) {
                final String key = RandomStringUtils.randomAlphanumeric(128);
                final var md = MessageDigest.getInstance("SHA-256");
                final String keyDigest = Base64.encodeBase64URLSafeString(md.digest(key.getBytes()));
                authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(keyDigest).state(state).build();
                authorizationCodeRequest = code -> spotifyApi.authorizationCodePKCE(code, key).build();
                authorizationRefreshRequest = () -> spotifyApi.authorizationCodePKCERefresh().build();
            } else {
                authorizationCodeUriRequest = spotifyApi.authorizationCodeUri().state(state).build();
                authorizationCodeRequest = code -> spotifyApi.authorizationCode(code).build();
                authorizationRefreshRequest = () -> spotifyApi.authorizationCodeRefresh().build();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Select correct algorithm spelling: " + e);
        }
        waitingForAPI.acquire(); // ensure that the first networking operation performed is performTokenRequest()
        performTokenRequest();
    }

    /**
     * host webserver to catch callback code from redirect url
     * @throws IOException when an issue occurs with creating the redirect catch server.
     */
    private HttpServer startCallbackServer() throws IOException {
        var redirectUri = spotifyApi.getRedirectURI();
        HttpServer server = HttpServer.create(
                new InetSocketAddress(redirectUri.getHost(), redirectUri.getPort()), 0);
        server.createContext(redirectUri.getPath(), callbackHandler);
        server.setExecutor(null);
        server.start();
        return server;
    }

    /**
     * Get access tokens for Spotify API, must be called first and only once.
     * @throws IOException when an issue occurs with creating the redirect catch server or there is a network issue
     *                     (HTTP 3xx status code).
     */
    private void performTokenRequest() throws IOException {
        try {
            final URI uri = authorizationCodeUriRequest.execute();
            var server = startCallbackServer();
            Desktop.getDesktop().browse(uri); // execute above URL by opening browser window with it
            CompletableFuture.runAsync(() -> {
                var code = callbackHandler.getCodeSync();
                server.stop(0);
                performTokenGet(code);
            });
        } catch (ParseException | NullPointerException | SpotifyWebApiException e) {
            // error with authorizationCodeUriRequest.execute(); or its return value
            throw new RuntimeException(e);
        } catch (SecurityException | UnsupportedOperationException e) {
            // caused by denial of automatic opening of uri in browser
            throw new UnsupportedOperationException("Desktop not supported, can't open URLs in web browser. "
                    + e.getMessage());
        }
    }

    /**
     * @param tokenExpiresIn Seconds until current token expires.
     */
    private void scheduleTokenRefresh(long tokenExpiresIn) {
        // tokens should be valid for an hour so 5 seconds earlier seems reasonable
        long refreshRate = tokenExpiresIn > 5 ? tokenExpiresIn - 5 : 5;
        tokenRefresh.schedule(this::performTokenRefresh, refreshRate, TimeUnit.SECONDS);
    }

    private void performTokenRefresh() {
        performTokenGet(null);
    }

    /**
     * Get access tokens (or refresh current ones) for Spotify API, what action is performed is decided by the value of
     * spotifyApi.getAccessToken()
     * @param requestCode code required to generate a new token set, can be null for token refresh.
     */
    private void performTokenGet(String requestCode) {
        try {
            AuthorizationCodeCredentials authorizationCodeCredentials;
            if (spotifyApi.getAccessToken() == null || spotifyApi.getAccessToken().isBlank()) {
                authorizationCodeCredentials = authorizationCodeRequest.apply(requestCode).execute();
            } else {
                waitingForAPI.acquire();
                authorizationCodeCredentials = authorizationRefreshRequest.get().execute();
            }
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            scheduleTokenRefresh(authorizationCodeCredentials.getExpiresIn());
        } catch (SpotifyWebApiException e) {
            // spotify has returned an HTTP 4xx or 5xx status code
            throw new RuntimeException(e);
        } catch (ParseException e) {
            // caused by malformed HTTP response
            throw new RuntimeException(e);
        } catch (IOException e) {
            // caused by network issues (HTTP 3xx status code)
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // caused by semaphore interruptions
            throw new RuntimeException(e);
        } finally {
            waitingForAPI.release();
        }
    }

    /**
     * Perform a fetch request to the Spotify API to get information on an artist.
     * @param spotifyId A String containing a Spotify ID of an artist.
     * @return Artist object generated by the spotify-web-api if spotifyId is a valid id for an artist.
     * @throws IOException In case of networking issues (HTTP 3xx status code).
     */
    public Optional<Artist> getArtist(@NonNull String spotifyId) throws IOException {
        try {
            waitingForAPI.acquire();
            final Artist artist = spotifyApi.getArtist(spotifyId).build().execute();
            waitingForAPI.release();
            return Optional.of(artist);
        } catch (SpotifyWebApiException | ParseException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            // caused by semaphore interruptions
            throw new RuntimeException(e);
        }
    }

    private class CallbackHandler implements HttpHandler {
        private static final Pattern statePattern = Pattern.compile("state=(?<state>[^&]*)&?");
        private static final Pattern errorPattern = Pattern.compile("error=(?<error>[^&]*)&?");
        private static final Pattern codePattern = Pattern.compile("code=(?<code>[^&]*)&?");
        private final Semaphore waitingForHandle = new Semaphore(1);
        private String responseQuery;

        CallbackHandler() {
            try {
                waitingForHandle.acquire();
            } catch (InterruptedException e) {
                // caused by semaphore interruptions
                throw new RuntimeException(e);
            }
        }

        /**
         * @return code needed to request access tokens from Spotify API.
         * @throws RuntimeException When there is a valid API response that won't return a usable code.
         * @apiNote Function will block using private semaphore until CallbackHandler.handle() is called, should only be
         * called asynchronously.
         */
        public String getCodeSync() throws RuntimeException {
            try {
                waitingForHandle.acquire();
                var stateMatcher = statePattern.matcher(responseQuery);
                if (!stateMatcher.find() || !state.equals(stateMatcher.group("state"))) {
                    throw new RuntimeException("Found state mismatch in authorization response, aborting request.");
                }
                var errorMatcher = errorPattern.matcher(responseQuery);
                if (errorMatcher.find()) {
                    throw new RuntimeException("Authorization has failed, reason: " + errorMatcher.group("error"));
                }
                var codeMatcher = codePattern.matcher(responseQuery);
                if (codeMatcher.find()) {
                    return codeMatcher.group("code");
                } else {
                    throw new RuntimeException("Can't find code in successful authorization response.");
                }
            } catch (InterruptedException e) {
                // caused by semaphore interruptions
                throw new RuntimeException(e);
            } finally {
                waitingForHandle.release();
            }
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = """
                    <div>Response from callback.<noscript> JS blocked, tab/window can be closed.</noscript></div>
                    <script>
                    window.onload = function(){
                    setTimeout(function(){
                       window.close();
                      },100);
                    };
                    </script>""";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();

            // code is contained in URI query
            responseQuery = t.getRequestURI().getQuery();
            waitingForHandle.release();
        }
    }
}
