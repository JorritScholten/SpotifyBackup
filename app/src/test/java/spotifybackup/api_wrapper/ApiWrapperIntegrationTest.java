package spotifybackup.api_wrapper;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import spotifybackup.storage.SpotifyArtist;
import spotifybackup.storage.SpotifyObject;
import spotifybackup.storage.SpotifyObjectRepository;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "EnableIntegrationTests", matches = "true")
public class ApiWrapperIntegrationTest {
    static final String SPOTIFY_API_KEY = System.getProperty("user.home") +
            System.getProperty("file.separator") + ".spotify_api_key.json";
    static ApiWrapper apiWrapper;

    @BeforeAll
    public static void perform_authentication() throws URISyntaxException, IOException, InterruptedException {
        try (var file = new FileReader(SPOTIFY_API_KEY)) {
            var parser = JsonParser.parseReader(file);
            apiWrapper = new ApiWrapper(SpotifyApi.builder()
                    .setClientId(parser.getAsJsonObject().get("clientId").getAsString())
//                    .setClientSecret(parser.getAsJsonObject().get("clientSecret").getAsString())
                    .setRedirectUri(new URI(parser.getAsJsonObject().get("redirectUri").getAsString()))
            );
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            2exebQUDoIoT0dXA8BcN1P, Home
            """)
    void request_artist_name(String spotifyId, String name) throws IOException {
        // Act
        final String artistName = apiWrapper.getArtist(spotifyId).orElseThrow().getName();

        // Assert
        assertEquals(artistName, name);
    }

    @Test
    void persist_requested_artist() throws IOException {
        // Arrange
        final String artistId = "5VPCIIfZPK8KPsgz4jmOEC", artistName = "The Blue Stones";
        SpotifyObjectRepository spotifyObjectRepository;
        spotifyObjectRepository = SpotifyObjectRepository.testFactory(false);
        final long oldCount = spotifyObjectRepository.count(SpotifyObject.SubTypes.ARTIST);

        // Act
        final Artist apiArtist = apiWrapper.getArtist(artistId).orElseThrow();
        final SpotifyArtist spotifyArtist = spotifyObjectRepository.persist(apiArtist);

        // Assert
        assertNotNull(spotifyArtist);
        assertEquals(spotifyArtist.getName(), artistName);
        assertEquals(spotifyObjectRepository.count(SpotifyObject.SubTypes.ARTIST), oldCount + 1);
    }
}
