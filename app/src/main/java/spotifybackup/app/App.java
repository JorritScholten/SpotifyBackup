package spotifybackup.app;

import com.google.gson.JsonParser;
import spotifybackup.cmd.CmdParser;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.argument.file.DefaultFilePathArgument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class App {
    static final String HOME_DIR = System.getProperty("user.home") + System.getProperty("file.separator");
    static final DefaultFilePathArgument apiKeyFileArg = new DefaultFilePathArgument.Builder()
            .name("api-key")
            .shortName('a')
            .isFile()
            .description("Path of json file containing the Spotify API key.")
            .defaultValue(new File(HOME_DIR + ".spotify_api_key.json"))
            .build();
    static final DefaultFilePathArgument dbFileArg = new DefaultFilePathArgument.Builder()
            .name("database")
            .shortName('d')
            .isFile()
            .description("Path of H2 db file containing the data from the user.")
            .defaultValue(new File(HOME_DIR + "spotify_backup.mv.db"))
            .build();
    static final FlagArgument getMeArg = new FlagArgument.Builder()
            .name("getMe")
            .description("Get users account info.")
            .build();
    static final CmdParser argParser;

    static {
        argParser = new CmdParser.Builder()
                .arguments(apiKeyFileArg, dbFileArg, getMeArg)
                .description("Program to create offline backup of users Spotify account.")
                .programName("SpotifyBackup.jar")
                .addHelp()
                .build();
    }

    private App() {
        try (var apiKeyFile = new FileReader(apiKeyFileArg.getValue())) {
            var apiKeyParser = JsonParser.parseReader(apiKeyFile);
            System.out.println("API key: " + apiKeyParser.getAsJsonObject().get("clientId").getAsString());
            System.out.println("getMe: " + getMeArg.getValue());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to find file containing Spotify API key.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to close FileReader of Spotify API key.");
        }
    }

    public static void main(String[] args) {
        try {
            argParser.parseArguments(args);
            if (argParser.isPresent("help")) {
                System.out.println(argParser.getHelp());
                System.exit(1);
            } else {
                new App();
            }
        } catch (Exception e) {
            System.out.println("Error with input: " + e.getMessage());
            System.out.println(argParser.getHelp());
            System.exit(-1);
        }
    }
}
