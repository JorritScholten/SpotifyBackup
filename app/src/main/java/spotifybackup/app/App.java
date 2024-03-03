package spotifybackup.app;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import spotifybackup.cmd.CmdParser;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.argument.file.DefaultFilePathArgument;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

public class App {
    static final String HOME_DIR = System.getProperty("user.home") + FileSystems.getDefault().getSeparator();
    static final String USER_DIR = System.getProperty("user.dir") + FileSystems.getDefault().getSeparator();
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
    static final FlagArgument verboseArg = new FlagArgument.Builder()
            .name("verbose")
            .shortName('v')
            .description("Print full stacktrace.")
            .build();
    static final CmdParser argParser;
    static final Terminal term;

    static {
        argParser = new CmdParser.Builder()
                .arguments(configFileArg, dbFileArg, getMeArg, verboseArg)
                .description("Program to create offline backup of users Spotify account.")
                .programName("SpotifyBackup.jar")
                .addHelp()
                .build();
        try {
            term = TerminalBuilder.terminal();
        } catch (IOException e) {
            throw new RuntimeException("Can't create terminal. " + e);
        }
    }

    public static void println(String message) {
        term.writer().println(message);
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            argParser.parseArguments(args);
            if (argParser.isPresent("help")) {
                println(argParser.getHelp(term.getWidth()));
            } else {
                var cli = new CLI();
                cli.save_liked_songs();
            }
            System.exit(0);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            println("Error with input: " + e.getMessage());
            println(argParser.getHelp(term.getWidth()));
            if (verboseArg.getValue()) e.printStackTrace(term.writer());
            System.exit(1);
        }
    }
}
