package spotifybackup.app;

import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import spotifybackup.app.exception.BlankConfigFieldException;
import spotifybackup.app.exception.ConfigFileException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@Getter
public class Config {
    private static final Property<?>[] properties;
    private static final Property<String> clientId = new Property<>("clientId", true, String.class);
    private static final Property<URI> redirectUri = new Property<>("redirectURI", true, URI.class);
    private static final Property<String> clientSecret = new Property<>("clientSecret", false, String.class);
    private static final Property<String> refreshToken = new Property<>("refreshToken", false, String.class);

    static {
        properties = new Property<?>[]{clientId, redirectUri, clientSecret, refreshToken};
    }

    public Config(@NonNull File file) throws IOException {
        if (file.isDirectory())
            throw new IllegalArgumentException("Supplied filepath must point to a file, supplied path: " + file);
        if (file.exists()) {
            if (!file.canRead()) throw new IllegalArgumentException("Can't read file at supplied filepath: " + file);
            try (var reader = new FileReader(file)) {
                var parser = JsonParser.parseReader(reader).getAsJsonObject();
                for (var property : properties) {
                    if (property.isRequired) {
                        if (!parser.has(property.key))
                            throw new BlankConfigFieldException(file + " has missing " + property.key + " field.");
                        String value = parser.get(property.key).getAsString();
                        if (value.isBlank())
                            throw new BlankConfigFieldException(property.key + " has blank field.");
                        if (property.equalsValueType(String.class)) {
                            ((Property<String>) property).setValue(value);
                        } else if (property.equalsValueType(URI.class)) {
                            ((Property<URI>) property).setValue(new URI(value));
                        } else {
                            throw new ConfigFileException("Unhandled value type of property field.");
                        }
                    }
                }
            } catch (URISyntaxException e) {
                throw new ConfigFileException("Redirect URI has improper syntax.");
            }
        } else {
            createNewFile(file);
            throw new ConfigFileException("Created empty config file, please fill in the fields: " + file);
        }
    }

    private static void createNewFile(File file) throws IOException {
        try (var writer = new FileWriter(file)) {
            writer.write("{\n");
            for (var iter = Arrays.stream(properties).filter(p -> p.isRequired).iterator(); iter.hasNext(); ) {
                var property = iter.next();
                writer.write("    \"" + property.key + "\":\"\"");
                if (iter.hasNext()) writer.write(',');
                writer.write('\n');
            }
            writer.write("}\n");
        }
    }

    @Getter
    private static class Property<T> {
        private final String key;
        private final boolean isRequired;
        @Setter(AccessLevel.PRIVATE)
        private T value;
        private final Class<T> valueType;

        Property(@NonNull String key, boolean isRequired, Class<T> valueType) {
            this.key = key;
            this.isRequired = isRequired;
            this.valueType = valueType;
        }

        boolean equalsValueType(Class<?> type){
            return valueType.equals(type);
        }
    }
}
