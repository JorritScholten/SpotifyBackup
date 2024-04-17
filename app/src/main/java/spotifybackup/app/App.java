package spotifybackup.app;

import lombok.Getter;
import spotifybackup.cmd.CmdParser;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.argument.enumeration.DefaultEnumArgument;
import spotifybackup.cmd.argument.file.DefaultFilePathArgument;
import spotifybackup.cmd.argument.file.OptionalFilePathArgument;
import spotifybackup.cmd.argument.integer.DefaultBoundedIntArgument;
import spotifybackup.storage.ImageSelection;
import spotifybackup.utils.PathUtils;

import java.io.File;

public class App {
    public static final String APP_NAME = "SpotifyBackup";
    static final DefaultFilePathArgument configFileArg = new DefaultFilePathArgument.Builder()
            .name("config")
            .shortName('c')
            .isFile()
            .description("Settings file containing the Spotify API key and persistent configuration properties.")
            .defaultValue(new File(PathUtils.configDir(), "config.json"))
            .makeValueMandatory()
            .build();
    static final DefaultFilePathArgument dbFileArg = new DefaultFilePathArgument.Builder()
            .name("database")
            .shortName('d')
            .isFile()
            .description("Path of H2 db file containing the data from the user.")
            .defaultValue(new File(PathUtils.dataDir(), "backup.mv.db"))
            .makeValueMandatory()
            .build();
    static final OptionalFilePathArgument sqlOutputFileArg = new OptionalFilePathArgument.Builder()
            .name("output-SQL")
            .shortName('o')
            .isFile()
            .description("Path to text file of SQL script of the database, useful for version tracking with git. " +
                    "Output only created if argument is present.")
            .build();
    static final FlagArgument verboseArg = new FlagArgument.Builder()
            .name("verbose")
            .shortName('v')
            .description("Print full stacktrace and verbose progress messages.")
            .build();
    static final FlagArgument configureBackups = new FlagArgument.Builder()
            .name("specify-accounts-to-backup")
            .shortName('b')
            .description("Specify in config which accounts should be backed up.")
            .build();
    static final FlagArgument noBackups = new FlagArgument.Builder()
            .name("no-backups")
            .description("Perform no backup for any account.")
            .build();
    static final FlagArgument configureCloningTargets = new FlagArgument.Builder()
            .name("configure-cloning-targets")
            .description("Specify in config which accounts should copy their Liked songs and playlists onto which " +
                    "accounts. Multiple differing accounts can target the same account but an account that is a " +
                    "cloning target can not do its own backups.")
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
            .makeValueMandatory()
            .build();
    static final FlagArgument showTotalLibraryDuration = new FlagArgument.Builder()
            .name("show-total-library-duration")
            .description("Print out the total duration of a users' Liked songs.")
            .build();
    static final FlagArgument showDurationOfNew = new FlagArgument.Builder()
            .name("show-duration-new-liked")
            .description("Print out total duration of all new Liked songs, only done during backup.")
            .build();
    static final DefaultEnumArgument<CLI.PlaylistFilter> playlistSaveRestriction = new DefaultEnumArgument.Builder<CLI.PlaylistFilter>()
            .enumClass(CLI.PlaylistFilter.class)
            .name("restrict-playlists")
            .description("Restrict which playlists are saved, meant for broadly filtering away the auto-generated " +
                    "playlists by Spotify or just everything not made by the user. Examples of Spotify auto-generated " +
                    "playlists are Discover Weekly and This is <Artist name>.")
            .shortName('p')
            .defaultValue(CLI.PlaylistFilter.ALL_BUT_SPOTIFY)
            .makeValueMandatory()
            .build();
    static final FlagArgument listUserAccounts = new FlagArgument.Builder()
            .name("list-user-accounts")
            .description("List user accounts stored the database which have library information and accounts in the " +
                    "config file which can be targets in account cloning.")
            .build();
    static final FlagArgument setConfigValues = new FlagArgument.Builder()
            .name("set-config-values")
            .description("Set application credentials in config file needed to connect to the Spotify API.")
            .build();
    static final CmdParser argParser;
    @Getter
    static Config config;

    static {
        argParser = new CmdParser.Builder()
                .arguments(
                        configFileArg,
                        dbFileArg,
                        sqlOutputFileArg,
                        setConfigValues,
                        addAccounts,
                        configureBackups,
                        configureCloningTargets,
                        imageSaveRestriction,
                        playlistSaveRestriction,
                        verboseArg,
                        showTotalLibraryDuration,
                        showDurationOfNew,
                        listUserAccounts,
                        noBackups
                )
                .description("Program to create offline backup of users Spotify account.")
                .programName("SpotifyBackup.jar")
                .addHelp()
                .build();
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            PathUtils.configDir();
            argParser.parseArguments(args);
            if (argParser.isPresent("help")) {
                CLI.println(argParser.getHelp(CLI.getTerm().getType().equals("dumb") ? 120 : CLI.getTerm().getWidth()));
            } else {
                new CLI();
            }
            System.exit(0);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            if (verboseArg.isPresent()) e.printStackTrace(CLI.getTerm().writer());
            else CLI.println(e.getMessage());
            CLI.getTerm().flush();
            System.exit(1);
        }
    }
}
