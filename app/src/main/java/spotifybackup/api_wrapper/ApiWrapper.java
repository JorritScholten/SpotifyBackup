package spotifybackup.api_wrapper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class ApiWrapper {
    private final SpotifyApi spotifyApi;
    final private boolean performPKCE;
    private boolean callbackTriggered = false;
    private Optional<String> code;
    private String state = UUID.randomUUID().toString();
    private String key = RandomStringUtils.randomAlphanumeric(128);
    private String keyDigest;

    public ApiWrapper(SpotifyApi.Builder builder) {
        spotifyApi = builder.build();
        performPKCE = spotifyApi.getClientSecret() == null || spotifyApi.getClientSecret().isEmpty();
        if (performPKCE) {
            try {
                var md = MessageDigest.getInstance("SHA-256");
                keyDigest = Base64.encodeBase64URLSafeString(md.digest(key.getBytes()));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void authorizationCodeUri_Sync() throws IOException {
        //open redirect uri in browser or local window
        try {
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
            server.createContext(redirectUri.getPath(), new CallbackHandler());
            server.setExecutor(null);
            server.start();

            // execute above URL by opening browser window with it
            Desktop.getDesktop().browse(uri);

            while (!callbackTriggered) {
                Thread.sleep(10);
            }
            server.stop(0);
            if (code.isEmpty()) {
                new RuntimeException("callback triggered without valid code return.");
            } else {
                AuthorizationCodeCredentials authorizationCodeCredentials;
                if (performPKCE) {
                    final var authorizationCodePKCERequest = spotifyApi.authorizationCodePKCE(code.get(), key).build();
                    authorizationCodeCredentials = authorizationCodePKCERequest.execute();
                } else {
                    final var authorizationCodeRequest = spotifyApi.authorizationCode(code.get()).build();
                    authorizationCodeCredentials = authorizationCodeRequest.execute();
                }
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            }
        } catch (Exception e) {
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

    class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Response from callback.";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();

            // code is contained in URI query
            var matcher = Pattern.compile("state=(?<state>[^&]*)&?")
                    .matcher(t.getRequestURI().getQuery());
            if (matcher.find() && state.equals(matcher.group("state"))) {
                matcher = Pattern.compile("error=(?<error>[^&]*)&?")
                        .matcher(t.getRequestURI().getQuery());
                if (matcher.find()) {
                    code = Optional.empty();
//                    throw new RuntimeException("Authorization has failed, reason: " + matcher.group("error"));
                } else {
                    matcher = Pattern.compile("code=(?<code>[^&]*)&?")
                            .matcher(t.getRequestURI().getQuery());
                    matcher.find();
                    code = Optional.ofNullable(matcher.group("code"));
                }
            } else {
                code = Optional.empty();
//                throw new RuntimeException("Found state mismatch in authorization response, aborting request.");
            }
            callbackTriggered = true;
        }
    }
}
