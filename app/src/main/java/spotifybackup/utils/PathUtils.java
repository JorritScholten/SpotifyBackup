package spotifybackup.utils;

import spotifybackup.app.App;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Map;

public class PathUtils {
    private static final String HOME_DIR = System.getProperty("user.home");
    private static final String SEP = FileSystems.getDefault().getSeparator();
    private static final Map<String, String> env = System.getenv();

    private PathUtils() {}

    public static File configDir() {
        var path = new File(switch (System.getProperty("os.name")) {
            case "Linux" -> env.getOrDefault("XDG_CONFIG_HOME",
                    HOME_DIR + SEP + ".config" + SEP + App.APP_NAME);
            case "Windows" -> env.getOrDefault("APPDATA",
                    HOME_DIR + SEP + "AppData" + SEP + App.APP_NAME);
            default -> System.getProperty("user.dir");
        } + SEP);
        if (!path.exists() && !path.mkdirs()) throw new RuntimeException("Couldn't create configuration directory.");
        return path;
    }

    public static File dataDir() {
        var path = new File(switch (System.getProperty("os.name")) {
            case "Linux" -> env.getOrDefault("XDG_DATA_HOME",
                    HOME_DIR + SEP + ".local" + SEP + "share" + SEP + App.APP_NAME);
            case "Windows" -> env.getOrDefault("APPDATA",
                    HOME_DIR + SEP + "AppData" + SEP + App.APP_NAME);
            default -> System.getProperty("user.dir");
        } + SEP);
        if (!path.exists() && !path.mkdirs()) throw new RuntimeException("Couldn't create data directory.");
        return path;
    }
}
