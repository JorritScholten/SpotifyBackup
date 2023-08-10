package spotifybackup.api_wrapper;

import com.google.gson.JsonParser;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import spotifybackup.storage.ArtistRepository;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApiWrapperIntegrationTest {
    static ApiWrapper apiWrapper;

    @BeforeAll
    public static void perform_authentication() throws URISyntaxException, FileNotFoundException, IOException {
        try (var file = new FileReader(System.getProperty("user.home") + System.getProperty("file.separator") + ".java_spotify_backup.json")) {
            var parser = JsonParser.parseReader(file);
            apiWrapper = new ApiWrapper(SpotifyApi.builder()
                    .setClientId(parser.getAsJsonObject().get("clientId").getAsString())
//                    .setClientSecret(parser.getAsJsonObject().get("clientSecret").getAsString())
                    .setRedirectUri(new URI(parser.getAsJsonObject().get("redirectUri").getAsString()))
            );
            apiWrapper.performTokenRequest();
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            2exebQUDoIoT0dXA8BcN1P, Home
            """)
    public void request_artist_name(String spotifyId, String name) throws IOException {
        // Act
        final String artistName = apiWrapper.getArtist(spotifyId).orElseThrow().getName();

        // Assert
        assertEquals(artistName, name);
    }

    @Test
    public void persist_requested_artist() throws IOException {
        // Arrange
        final String artistId = "5VPCIIfZPK8KPsgz4jmOEC", artistName = "The Blue Stones";
        ArtistRepository artistRepository;
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hikari.dataSource.url", "jdbc:h2:./build/test;DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "create");
        DB_ACCESS.put("hibernate.show_sql", "false");
        DB_ACCESS.put("persistenceUnitName", "testdb");
        try {
            artistRepository = new ArtistRepository(DB_ACCESS);
        } catch (ServiceException e) {
            throw new RuntimeException("Can't create db access service, is db version out of date?\n" + e.getMessage());
        }
        final long oldCount = artistRepository.count();

        // Act
        final Artist apiArtist = apiWrapper.getArtist(artistId).orElseThrow();
        final spotifybackup.storage.Artist artist = artistRepository.persist(apiArtist);

        // Assert
        assertNotNull(artist);
        assertEquals(artist.getName(), artistName);
        assertEquals(artistRepository.count(), oldCount + 1);
    }
}
