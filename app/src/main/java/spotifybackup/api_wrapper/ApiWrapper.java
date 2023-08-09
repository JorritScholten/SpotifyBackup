package spotifybackup.api_wrapper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.AbstractRequest;

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
    private final String key;
    private final String keyDigest;
    private final CallbackHandler callbackHandler = new CallbackHandler();
    private final ScheduledExecutorService tokenRefresh = Executors.newScheduledThreadPool(1);
    // Strategy patterns
    private final Supplier<AbstractRequest<AuthorizationCodeCredentials>> authorizationRefreshRequest;
    private final AbstractRequest<URI> authorizationCodeUriRequest;
    private final Function<String, AbstractRequest<AuthorizationCodeCredentials>> authorizationCodeRequest;

    public ApiWrapper(SpotifyApi.Builder builder) {
        spotifyApi = builder.build();
        if (spotifyApi.getClientSecret() == null || spotifyApi.getClientSecret().isEmpty()) {
            try {
                key = RandomStringUtils.randomAlphanumeric(128);
                var md = MessageDigest.getInstance("SHA-256");
                keyDigest = Base64.encodeBase64URLSafeString(md.digest(key.getBytes()));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(keyDigest).state(state).build();
            authorizationCodeRequest = (code) -> spotifyApi.authorizationCodePKCE(code, key).build();
            authorizationRefreshRequest = () -> spotifyApi.authorizationCodePKCERefresh().build();
        } else {
            keyDigest = null;
            key = null;
            authorizationCodeUriRequest = spotifyApi.authorizationCodeUri().state(state).build();
            authorizationCodeRequest = (code) -> spotifyApi.authorizationCode(code).build();
            authorizationRefreshRequest = () -> spotifyApi.authorizationCodeRefresh().build();
        }
    }

    public void performTokenRequest() {
        try {
            waitingForAPI.acquire();
            final URI uri = authorizationCodeUriRequest.execute();

            // host http:localhost:8888/callback RESTfull webserver to catch callback code
            var redirectUri = spotifyApi.getRedirectURI();
            HttpServer server = HttpServer.create(new InetSocketAddress(redirectUri.getHost(), redirectUri.getPort()), 0);
            server.createContext(redirectUri.getPath(), callbackHandler);
            server.setExecutor(null);
            server.start();

            // execute above URL by opening browser window with it
            if (!(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))) {
                throw new RuntimeException("Desktop not supported, can't open URLs in web browser.");
            }
            Desktop.getDesktop().browse(uri);

            CompletableFuture.runAsync(() -> {
                var code = callbackHandler.getCodeSync();
                server.stop(0);
                try {
                    final var authorizationCodeCredentials = authorizationCodeRequest.apply(code).execute();
                    spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                    spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
                    waitingForAPI.release();
                    scheduleTokenRefresh(authorizationCodeCredentials.getExpiresIn());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        try {
            waitingForAPI.acquire();
            final var authorizationCodeCredentials = authorizationRefreshRequest.get().execute();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            waitingForAPI.release();
            scheduleTokenRefresh(authorizationCodeCredentials.getExpiresIn());
        } catch (InterruptedException | SpotifyWebApiException | ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param spotifyId A String containing a Spotify ID of an artist.
     * @return name of artist.
     * @throws IOException In case of networking issues.
     * @deprecated Use getArtist() instead and use .getName() on the return value.
     */
    @Deprecated
    public String getArtistName(String spotifyId) throws IOException {
        try {
            waitingForAPI.acquire();
            final Artist artist = spotifyApi.getArtist(spotifyId).build().execute();
            waitingForAPI.release();
            return artist.getName();
        } catch (SpotifyWebApiException | ParseException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform a fetch request to the Spotify API to get information on an artist.
     * @param spotifyId A String containing a Spotify ID of an artist.
     * @return Artist object generated by the spotify-web-api if spotifyId is a valid id for an artist.
     * @throws IOException In case of networking issues.
     */
    public Optional<Artist> getArtist(String spotifyId) throws IOException {
        try {
            waitingForAPI.acquire();
            final Artist artist = spotifyApi.getArtist(spotifyId).build().execute();
            waitingForAPI.release();
            return Optional.of(artist);
        } catch (SpotifyWebApiException | ParseException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class CallbackHandler implements HttpHandler {
        private final Semaphore waitingForHandle = new Semaphore(1);
        private String responseQuery;

        {
            try {
                waitingForHandle.acquire();
            } catch (InterruptedException e) {
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
                var matcher = Pattern.compile("state=(?<state>[^&]*)&?").matcher(responseQuery);
                if (matcher.find() && state.equals(matcher.group("state"))) {
                    matcher = Pattern.compile("error=(?<error>[^&]*)&?").matcher(responseQuery);
                    if (matcher.find()) {
                        throw new RuntimeException("Authorization has failed, reason: " + matcher.group("error"));
                    } else {
                        matcher = Pattern.compile("code=(?<code>[^&]*)&?").matcher(responseQuery);
                        if (matcher.find()) {
                            return matcher.group("code");
                        } else {
                            throw new RuntimeException("Can't find code in successful authorization response.");
                        }
                    }
                } else {
                    throw new RuntimeException("Found state mismatch in authorization response, aborting request.");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                waitingForHandle.release();
            }
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Response from callback.";
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
