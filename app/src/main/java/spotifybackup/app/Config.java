package spotifybackup.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.*;
import spotifybackup.app.exception.BlankConfigFieldException;
import spotifybackup.app.exception.ConfigFileException;
import spotifybackup.app.exception.ConfigReferenceLoopException;
import spotifybackup.app.exception.ConfigUsersFieldException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
public class Config {
    @Getter(AccessLevel.NONE)
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();
    @Getter(AccessLevel.NONE)
    private File path;
    @Expose
    private String clientId;
    @Expose
    private URI redirectURI;
    @Expose
    private String clientSecret;
    @Expose
    private List<UserInfo> users;

    private Config() {}

    /**
     * Load config properties from a file, the file path is stored to allow for the saving of updated values.
     * @param filePath Path to .json config file.
     * @throws ConfigFileException       when {@code filepath} doesn't point to an existing config file, a blank config
     *                                   file is created at filePath.
     * @throws BlankConfigFieldException when the config file has a blank or missing field.
     * @throws ConfigUsersFieldException when one of the objects in the "users" field has a blank or missing field or
     *                                   there exists potential for a self-reference loop.
     * @throws IOException               when trying to read or write to <code>filepath</code> doesn't work.
     */
    public static void loadAppConfigFromFile(@NonNull File filePath) throws IOException {
        if (filePath.isDirectory())
            throw new IllegalArgumentException("Supplied filepath must point to a file, supplied path: " + filePath);
        if (filePath.exists()) {
            if (filePath.canRead()) readFile(filePath);
            else throw new IllegalArgumentException("Can't read file at supplied filepath: " + filePath);
            if (!filePath.canWrite())
                throw new IllegalArgumentException("Can't write to config file at supplied filepath: " + filePath);
        } else {
            createNewFile(filePath);
            throw new ConfigFileException("Created empty config file, please fill in the fields at: " + filePath);
        }
    }

    private static void readFile(File file) throws IOException {
        try (var reader = new FileReader(file)) {
            App.config = gson.fromJson(reader, Config.class);
            App.config.path = file;
            checkAllFields(file, App.config);
            App.config.users.forEach(u -> u.parent = App.config);
        }
    }

    private static void checkAllFields(File file, Config config) {
        checkSimpleFields(file, config);
        checkUsersField(file, config);
    }

    private static void checkSimpleFields(File file, Config config) {
        List<String> fieldWarnings = new ArrayList<>();
        if (isNullOrBlank(config.clientId))
            fieldWarnings.add("  clientId field blank or missing.");
        if (config.redirectURI == null || config.redirectURI.toString().isBlank())
            fieldWarnings.add("  redirectURI field blank or missing.");
        if (config.clientSecret != null && config.clientSecret.isBlank())
            fieldWarnings.add("  clientSecret field blank (can be omitted).");
        if (!fieldWarnings.isEmpty()) {
            fieldWarnings.addFirst("Blank or missing field(s) in: " + file);
            throw new BlankConfigFieldException(String.join("\n", fieldWarnings));
        }
    }

    private static void checkUsersField(File file, Config config) {
        List<String> fieldWarnings = new ArrayList<>();
        if (config.users == null) config.users = new ArrayList<>();
        else config.users.forEach(user -> {
            if (isNullOrBlank(user.spotifyId))
                fieldWarnings.add("   user.spotifyId field blank or missing.");
            if (isNullOrBlank(user.displayName))
                fieldWarnings.add("   user[" + user.spotifyId + "].displayName field blank or missing.");
            if (isNullOrBlank(user.refreshToken))
                fieldWarnings.add("   user[" + user.spotifyId + "].refreshToken field blank or missing.");
            if (user.doBackup == null)
                fieldWarnings.add("   user[" + user.spotifyId + "].doBackup field missing.");
            if (user.cloneTargets == null)
                user.cloneTargets = new ArrayList<>();
            else {
                if (!user.cloneTargets.isEmpty() && !Boolean.TRUE.equals(user.doBackup))
                    fieldWarnings.add("   user[" + user.spotifyId + "].doBackup is false whilst having cloning targets.");
                user.cloneTargets.forEach(id -> {
                    if (isNullOrBlank(id))
                        fieldWarnings.add("   user[" + user.spotifyId + "].cloneTargets has a blank entry.");
                    else if (config.users.stream().map(u -> u.getSpotifyId().orElseThrow()).noneMatch(t -> t.equals(id)))
                        fieldWarnings.add("   user[" + user.spotifyId + "].cloneTargets targets a Spotify User ID [" + id +
                                "] not found in config.");
                    else if (id.equals(user.spotifyId))
                        fieldWarnings.add("   user[" + user.spotifyId + "].cloneTargets targets self");
                });
            }
        });
        if (!fieldWarnings.isEmpty()) {
            fieldWarnings.addFirst("Blank or missing field(s) in: " + file);
            throw new ConfigUsersFieldException(String.join("\n", fieldWarnings));
        }
    }

    private static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }

    private static void createNewFile(File file) throws IOException {
        try (var writer = new FileWriter(file)) {
            Config config = new Config();
            config.clientId = "";
            config.redirectURI = new URI("");
            config.users = new ArrayList<>();
            writer.write(gson.toJson(config));
            writer.write('\n');
            config.path = file;
            App.config = config;
        } catch (URISyntaxException e) {
            throw new ConfigFileException("This shouldn't be thrown for a blank URI.");
        }
    }

    public UserInfo addEmptyUser(boolean doBackup) {
        final UserInfo newUser = new UserInfo(this, doBackup);
        users.add(newUser);
        return newUser;
    }

    /** @return Unmodifiable list of UserInfo objects. */
    public List<UserInfo> getUsers() {
        return users.stream().toList();
    }

    public void setClientId(@NonNull String clientId) {
        this.clientId = clientId;
        serialize();
    }

    public void setRedirectURI(@NonNull URI redirectURI) {
        this.redirectURI = redirectURI;
        serialize();
    }

    public Optional<String> getClientSecret() {
        return Optional.ofNullable(clientSecret);
    }

    public void setClientSecret(@NonNull String clientSecret) {
        this.clientSecret = clientSecret;
        serialize();
    }

    public void clearClientSecret() {
        this.clientSecret = null;
        serialize();
    }

    private void serialize() {
        try (var writer = new FileWriter(path)) {
            writer.write(gson.toJson(this));
            writer.write('\n');
        } catch (IOException e) {
            throw new ConfigFileException("Couldn't write to config file at " + path.getAbsolutePath() + " " + e);
        }
    }

    public String getAsJson() {
        return gson.toJson(this);
    }

    @Builder(access = AccessLevel.PACKAGE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserInfo {
        private Config parent;
        @Expose
        private String spotifyId;
        @Expose
        private String displayName;
        @Expose
        private String refreshToken;
        @Expose
        private Boolean doBackup;
        @Expose
        @Builder.Default
        private List<String> cloneTargets = new ArrayList<>();

        private UserInfo(Config parent, boolean doBackup) {
            this.parent = parent;
            this.doBackup = doBackup;
            cloneTargets = new ArrayList<>();
        }

        public Optional<String> getDisplayName() {
            return Optional.ofNullable(displayName);
        }

        public void setDisplayName(@NonNull String displayName) {
            this.displayName = displayName;
            parent.serialize();
        }

        public Optional<String> getSpotifyId() {
            return Optional.ofNullable(spotifyId);
        }

        public void setSpotifyId(@NonNull String spotifyId) {
            this.spotifyId = spotifyId;
            parent.serialize();
        }

        public Optional<String> getRefreshToken() {
            return Optional.ofNullable(refreshToken);
        }

        public void setRefreshToken(@NonNull String refreshToken) {
            this.refreshToken = refreshToken;
            parent.serialize();
        }

        public boolean getDoBackup() {
            if (Objects.isNull(doBackup))
                throw new NullPointerException("doBackup is null, UserInfo improperly initialized.");
            else return doBackup;
        }

        public void setDoBackup(boolean doBackup) throws ConfigReferenceLoopException {
            if (!getDoBackup() && doBackup) {
                if (isCloningTarget()) throw new ConfigReferenceLoopException("account with spotifyId[" +
                        spotifyId + "] is a cloning target.");
            } else if (getDoBackup() && !doBackup) {
                if (!cloneTargets.isEmpty()) throw new ConfigReferenceLoopException("account with spotifyId[" +
                        spotifyId + "] still has cloning targets: [" + String.join(", ", cloneTargets) + "]");
            } else return;
            this.doBackup = doBackup;
            parent.serialize();
        }

        private boolean isCloningTarget() {
            for (var user : parent.users) {
                if (user.getCloneTargets().stream().anyMatch(t -> t.equals(this))) return true;
            }
            return false;
        }

        public List<UserInfo> getCloneTargets() {
            List<UserInfo> targets = new ArrayList<>();
            for (var targetId : cloneTargets)
                targets.add(parent.users.stream()
                        .filter(u -> u.getSpotifyId().isPresent() && u.getSpotifyId().orElseThrow().equals(targetId))
                        .findFirst().orElseThrow(() -> new RuntimeException("Trying to reference account in users " +
                                "with spotifyId[" + targetId + "] that no longer exists."))
                );
            return targets.stream().toList();
        }

        public boolean hasCloneTargets() {
            return !cloneTargets.isEmpty();
        }

        public void addCloneTarget(@NonNull UserInfo target) throws ConfigReferenceLoopException {
            if (!getDoBackup()) throw new ConfigReferenceLoopException("account with spotifyId[" +
                    spotifyId + "] not marked for backup.");
            if (target.getDoBackup())
                throw new ConfigReferenceLoopException("target[" + target.spotifyId + "] is marked for backup.");
            if (!target.parent.equals(parent))
                throw new IllegalArgumentException("target has different parent from this.");
            if (target.getSpotifyId().isEmpty()) {
                throw new IllegalArgumentException("target has no spotifyId.");
            } else {
                cloneTargets.add(target.getSpotifyId().get());
                parent.serialize();
            }
        }

        public void removeCloneTarget(@NonNull UserInfo target) {
            if (!target.parent.equals(parent))
                throw new IllegalArgumentException("target has different parent from this.");
            if (target.getSpotifyId().isEmpty()) {
                throw new IllegalArgumentException("target has no spotifyId.");
            } else {
                cloneTargets.remove(target.getSpotifyId().get());
                parent.serialize();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserInfo userInfo)) return false;
            return Objects.equals(spotifyId, userInfo.spotifyId) &&
                    Objects.equals(displayName, userInfo.displayName) &&
                    Objects.equals(refreshToken, userInfo.refreshToken) &&
                    Objects.equals(doBackup, userInfo.doBackup) &&
                    Objects.equals(cloneTargets, userInfo.cloneTargets);
        }

        @Override
        public int hashCode() {
            return Objects.hash(spotifyId, displayName, refreshToken);
        }
    }
}
