package spotifybackup.api_wrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import spotifybackup.app.Config;
import spotifybackup.storage.SpotifyArtist;
import spotifybackup.storage.SpotifyObject;
import spotifybackup.storage.SpotifyObjectRepository;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "EnableIntegrationTests", matches = "true")
public class ApiWrapperIntegrationTest {
    static final String CONFIG_FILE_PATH = System.getProperty("user.home") +
            System.getProperty("file.separator") + ".spotify_backup_config.json";
    static ApiWrapper apiWrapper;

    @BeforeAll
    public static void perform_authentication() throws IOException, InterruptedException {
        Config.loadFromFile(new File(CONFIG_FILE_PATH));
        apiWrapper = new ApiWrapper();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            2exebQUDoIoT0dXA8BcN1P, Home
            """)
    void request_artist_name(String spotifyId, String name) throws IOException, InterruptedException {
        // Act
        final String artistName = apiWrapper.getArtist(spotifyId).orElseThrow().getName();

        // Assert
        assertEquals(artistName, name);
    }

    @Test
    void persist_requested_artist() throws IOException, InterruptedException {
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
