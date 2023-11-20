package spotifybackup.app;

import lombok.NonNull;

import java.net.URI;

public class Config {
    private static final Property[] properties;
    private static Property<String> clientId = new Property<>("clientId", "");
    private static Property<URI> redirectUri = new Property<>("clientId", null);
    private static Property<String> clientSecret = new Property<>("clientSecret", "");
    private static Property<String> refreshToken = new Property<>("refreshToken", "");

    static {
        properties = new Property[]{};
    }

    public record Property<T>(String key, T value) {
        public Property<T> updateValue(@NonNull T newValue){
            return new Property<>(key, newValue);
        }
    }
}
