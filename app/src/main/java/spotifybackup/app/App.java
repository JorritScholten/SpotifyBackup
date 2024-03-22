package spotifybackup.app;

import lombok.Getter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import spotifybackup.cmd.CmdParser;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.argument.enumeration.DefaultEnumArgument;
import spotifybackup.cmd.argument.file.DefaultFilePathArgument;
import spotifybackup.cmd.argument.file.OptionalFilePathArgument;
import spotifybackup.cmd.argument.integer.DefaultBoundedIntArgument;
import spotifybackup.storage.ImageSelection;

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
    static final OptionalFilePathArgument sqlOutputFileArg = new OptionalFilePathArgument.Builder()
            .name("output-SQL")
            .shortName('o')
            .isFile()
            .description("Path to text file of SQL script of the database, useful for version tracking with git. Output only created if argument is present.")
            .build();
    static final FlagArgument verboseArg = new FlagArgument.Builder()
            .name("verbose")
            .shortName('v')
            .description("Print full stacktrace and verbose progress messages.")
            .build();
    static final FlagArgument doBackup = new FlagArgument.Builder()
            .name("do-backup")
            .shortName('b')
            .description("Perform backups for all accounts. Added for development, to be replaced with option to explicitly disable backup.")
            .build();
    static final DefaultBoundedIntArgument addAccounts = new DefaultBoundedIntArgument.Builder()
            .name("add-accounts")
            .defaultValue(1)
            .minimum(1)
            .shortName('a')
            .description("Number of accounts to add.")
            .build();
    static final DefaultEnumArgument<ImageSelection> imageSaveRestriction = new DefaultEnumArgument
            .Builder<ImageSelection>()
            .name("restrict-images")
            .description("Restrict which images are saved to save on database size.")
            .defaultValue(ImageSelection.ONLY_LARGEST)
            .enumClass(ImageSelection.class)
            .build();
    static final FlagArgument showTotalLibraryDuration = new FlagArgument.Builder()
            .name("show-total-library-duration")
            .description("Print out the total duration of a users' Liked songs.")
            .build();
    static final FlagArgument showDurationOfNew = new FlagArgument.Builder()
            .name("show-duration-new-liked")
            .description("Print out total duration of all new Liked songs, only done during backup.")
            .build();
    static final CmdParser argParser;
    static final Terminal term;
    @Getter
    static Config config;

    static {
        argParser = new CmdParser.Builder()
                .arguments(configFileArg, dbFileArg, sqlOutputFileArg, doBackup, imageSaveRestriction, addAccounts,
                        verboseArg, showTotalLibraryDuration, showDurationOfNew)
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
        term.flush();
    }

    /** @param spaces Amount of spaces to prepend to message. */
    public static void println(int spaces, String message) {
        println(" ".repeat(spaces) + message);
    }

    public static void verbosePrintln(String message) {
        if (verboseArg.isPresent()) println(message);
    }

    /** @param spaces Amount of spaces to prepend to message. */
    public static void verbosePrintln(int spaces, String message) {
        if (verboseArg.isPresent()) println(spaces, message);
    }

    public static void print(String message) {
        term.writer().print(message);
        term.flush();
    }

    /** @param spaces Amount of spaces to prepend to message. */
    public static void print(int spaces, String message) {
        print(" ".repeat(spaces) + message);
    }

    public static void verbosePrint(String message) {
        if (verboseArg.isPresent()) print(message);
    }

    /** @param spaces Amount of spaces to prepend to message. */
    public static void verbosePrint(int spaces, String message) {
        if (verboseArg.isPresent()) print(spaces, message);
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            argParser.parseArguments(args);
            if (argParser.isPresent("help")) {
                println(argParser.getHelp(term.getType().equals("dumb") ? 120 : term.getWidth()));
            } else {
                new CLI();
            }
            System.exit(0);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            println(e.getMessage());
            if (verboseArg.isPresent()) e.printStackTrace(term.writer());
            term.flush();
            System.exit(1);
        }
    }
}
