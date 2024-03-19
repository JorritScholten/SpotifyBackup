package spotifybackup.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import spotifybackup.app.exception.BlankConfigFieldException;
import spotifybackup.app.exception.ConfigFileException;

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
import java.util.function.Consumer;

@Getter
public class ConfigV2 {
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

    private ConfigV2() {}

    /** @apiNote Only to be used for testing purposes. */
    static ConfigV2 createNewForTesting(@NonNull File filePath) {
        var config = new ConfigV2();
        config.path = filePath;
        config.users = new ArrayList<>();
        return config;
    }

    /**
     * Load config properties from a file, the file path is stored to allow for the saving of updated values.
     * @param filePath Path to .json config file.
     * @throws ConfigFileException Thrown when filePath doesn't point to an existing config file, a blank config file is
     *                             created at filePath.
     */
    public static ConfigV2 loadFromFile(@NonNull File filePath) throws IOException {
        if (filePath.isDirectory())
            throw new IllegalArgumentException("Supplied filepath must point to a file, supplied path: " + filePath);
        if (filePath.exists()) {
            ConfigV2 config;
            if (filePath.canRead()) config = readFile(filePath);
            else throw new IllegalArgumentException("Can't read file at supplied filepath: " + filePath);
            if (!filePath.canWrite())
                throw new IllegalArgumentException("Can't write to config file at supplied filepath: " + filePath);
            else return config;
        } else {
            createNewFile(filePath);
            throw new ConfigFileException("Created empty config file, please fill in the fields at: " + filePath);
        }
    }

    private static ConfigV2 readFile(File file) throws IOException {
        try (var reader = new FileReader(file)) {
            var config = gson.fromJson(reader, ConfigV2.class);
            config.path = file;
            checkAllFields(file, config);
            config.users.forEach(u -> u.serialize = v -> config.serialize());
            return config;
        }
    }

    private static void checkAllFields(File file, ConfigV2 config) {
        if (isNullOrBlank(config.clientId))
            throw new BlankConfigFieldException("clientId field blank or missing in: " + file);
        if (config.redirectURI == null || config.redirectURI.toString().isBlank())
            throw new BlankConfigFieldException("redirectURI field blank or missing in: " + file);
        if (config.clientSecret != null && config.clientSecret.isBlank())
            throw new BlankConfigFieldException("clientSecret field blank: " + file);
        if (config.users == null)
            throw new BlankConfigFieldException("users array field missing in: " + file);
        else config.users.forEach(user -> {
            if (isNullOrBlank(user.spotifyId))
                throw new BlankConfigFieldException("user.spotifyId field blank or missing in: " + file);
            if (isNullOrBlank(user.displayName))
                throw new BlankConfigFieldException("user.displayName field blank or missing in: " + file);
            if (isNullOrBlank(user.refreshToken))
                throw new BlankConfigFieldException("user.refreshToken field blank or missing in: " + file);
        });
    }

    private static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }

    private static void createNewFile(File file) throws IOException {
        try (var writer = new FileWriter(file)) {
            ConfigV2 config = new ConfigV2();
            config.clientId = "";
            config.redirectURI = new URI("");
            config.clientSecret = "can be removed/omitted";
            config.users = new ArrayList<>();
            writer.write(gson.toJson(config));
            writer.write('\n');
        } catch (URISyntaxException e) {
            throw new ConfigFileException("This shouldn't be thrown for a blank URI.");
        }
    }

    public UserInfo addEmptyUser() {
        final UserInfo newUser = new UserInfo(v -> this.serialize());
        users.add(newUser);
        return newUser;
    }

    public UserInfo[] getUsers() {
        return users.toArray(new UserInfo[0]);
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

    @AllArgsConstructor
    public static class UserInfo {
        private Consumer<Void> serialize;
        @Expose
        private String spotifyId;
        @Expose
        private String displayName;
        @Expose
        private String refreshToken;

        UserInfo(Consumer<Void> serialize) {
            this.serialize = serialize;
        }

        public Optional<String> getDisplayName() {
            return Optional.ofNullable(displayName);
        }

        public void setDisplayName(@NonNull String displayName) {
            this.displayName = displayName;
            serialize.accept(null);
        }

        public Optional<String> getSpotifyId() {
            return Optional.ofNullable(spotifyId);
        }

        public void setSpotifyId(@NonNull String spotifyId) {
            this.spotifyId = spotifyId;
            serialize.accept(null);
        }

        public Optional<String> getRefreshToken() {
            return Optional.ofNullable(refreshToken);
        }

        public void setRefreshToken(@NonNull String refreshToken) {
            this.refreshToken = refreshToken;
            serialize.accept(null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserInfo userInfo)) return false;
            return Objects.equals(spotifyId, userInfo.spotifyId) &&
                    Objects.equals(displayName, userInfo.displayName) &&
                    Objects.equals(refreshToken, userInfo.refreshToken);
        }

        @Override
        public int hashCode() {
            return Objects.hash(spotifyId, displayName, refreshToken);
        }
    }
}
