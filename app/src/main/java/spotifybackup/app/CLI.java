package spotifybackup.app;

import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CLI {
    CLI() {
        try (var apiKeyFile = new FileReader(App.apiKeyFileArg.getValue())) {
            var apiKeyParser = JsonParser.parseReader(apiKeyFile);
            System.out.println("API key: " + apiKeyParser.getAsJsonObject().get("clientId").getAsString());
            System.out.println("getMe: " + App.getMeArg.getValue());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to find file containing Spotify API key.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to close FileReader of Spotify API key.");
        }
    }
}
