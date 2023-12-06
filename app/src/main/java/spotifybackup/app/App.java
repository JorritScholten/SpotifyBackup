package spotifybackup.app;

import spotifybackup.cmd.CmdParser;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.argument.file.DefaultFilePathArgument;

import java.io.File;
import java.io.IOException;

public class App {
    static final String HOME_DIR = System.getProperty("user.home") + System.getProperty("file.separator");
    static final String USER_DIR = System.getProperty("user.dir") + System.getProperty("file.separator");
    static final DefaultFilePathArgument apiKeyFileArg = new DefaultFilePathArgument.Builder()
            .name("api-key")
            .shortName('a')
            .isFile()
            .description("Path of json file containing the Spotify API key.")
            .defaultValue(new File(HOME_DIR + ".spotify_api_key.json"))
            .build();
    static final DefaultFilePathArgument configFileArg = new DefaultFilePathArgument.Builder()
            .name("config")
            .shortName('c')
            .isFile()
            .description("Settings file containing the Spotify API key and persistent configuration properties.")
            .defaultValue(new File(HOME_DIR + ".spotify_backup_config.json"))
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
    static final int TERMINAL_WIDTH;

    static {
        argParser = new CmdParser.Builder()
                .arguments(configFileArg, dbFileArg, getMeArg)
                .description("Program to create offline backup of users Spotify account.")
                .programName("SpotifyBackup.jar")
                .addHelp()
                .build();
        int width;
        try (var term = org.jline.terminal.TerminalBuilder.terminal()) {
            width = term.getWidth();
        } catch (IOException e) {
            width = 80;
        }
        TERMINAL_WIDTH = width;
    }

    public static void main(String[] args) {
        try {
            argParser.parseArguments(args);
            if (argParser.isPresent("help")) {
                System.out.println(argParser.getHelp(TERMINAL_WIDTH));
                System.exit(0);
            } else {
                new CLI();
            }
        } catch (Exception e) {
            System.out.println("Error with input: " + e.getMessage());
            System.out.println(argParser.getHelp(TERMINAL_WIDTH));
            System.exit(1);
        }
    }
}
