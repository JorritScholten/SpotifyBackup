package spotifybackup.api_wrapper;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.michaelthelin.spotify.SpotifyApi;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiWrapperTest {
    static ApiWrapper apiWrapper;

    @BeforeAll
    public static void perform_authentication() throws URISyntaxException, FileNotFoundException, IOException {
        try (var file = new FileReader(System.getProperty("user.home") + System.getProperty("file.separator") + ".java_spotify_backup.json")) {
            var parser = JsonParser.parseReader(file);
            apiWrapper = new ApiWrapper(SpotifyApi.builder()
                    .setClientId(parser.getAsJsonObject().get("clientId").getAsString())
                    .setClientSecret(parser.getAsJsonObject().get("clientSecret").getAsString())
                    .setRedirectUri(new URI(parser.getAsJsonObject().get("redirectUri").getAsString()))
            );
            apiWrapper.authorizationCodeUri_Sync();
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            2exebQUDoIoT0dXA8BcN1P, Home
            """)
    public void request_artist_name(String spotifyId, String name) throws IOException {
        // Act
        final String artistName = apiWrapper.getArtistName(spotifyId);

        // Assert
        assertEquals(artistName, name);
    }
}
