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
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class ApiWrapper {
    private final SpotifyApi spotifyApi;
    private final boolean performPKCE;
    private final Semaphore waitingForAPI = new Semaphore(1);
    private final String state = UUID.randomUUID().toString();
    private final String key;
    private final String keyDigest;
    private final CallbackHandler callbackHandler = new CallbackHandler();

    public ApiWrapper(SpotifyApi.Builder builder) {
        spotifyApi = builder.build();
        performPKCE = spotifyApi.getClientSecret() == null || spotifyApi.getClientSecret().isEmpty();
        if (performPKCE) {
            try {
                key = RandomStringUtils.randomAlphanumeric(128);
                var md = MessageDigest.getInstance("SHA-256");
                keyDigest = Base64.encodeBase64URLSafeString(md.digest(key.getBytes()));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } else {
            keyDigest = null;
            key = null;
        }
    }

    public void performTokenRequest() {
        try {
            waitingForAPI.acquire();
            AuthorizationCodeUriRequest authorizationCodeUriRequest;
            if (performPKCE) {
                authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(keyDigest)
                        .state(state)
                        .build();
            } else {
                authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                        .state(state)
                        .build();
            }
            final URI uri = authorizationCodeUriRequest.execute();

            // host https:localhost:8888/callback RESTfull webserver to catch callback code
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
                AuthorizationCodeCredentials authorizationCodeCredentials;
                try {
                    if (performPKCE) {
                        final var authorizationCodePKCERequest = spotifyApi.authorizationCodePKCE(code, key).build();
                        authorizationCodeCredentials = authorizationCodePKCERequest.execute();
                    } else {
                        final var authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
                        authorizationCodeCredentials = authorizationCodeRequest.execute();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
                waitingForAPI.release();
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
