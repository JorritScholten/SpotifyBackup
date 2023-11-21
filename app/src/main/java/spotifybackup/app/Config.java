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
import java.util.Optional;

@Getter
public class Config {
    public static final RequiredProperty<String> clientId = new RequiredProperty<>("clientId", String.class);
    public static final RequiredProperty<URI> redirectURI = new RequiredProperty<>("redirectURI", URI.class);
    public static final OptionalProperty<String> clientSecret = new OptionalProperty<>("clientSecret", String.class);
    public static final OptionalProperty<String> refreshToken = new OptionalProperty<>("refreshToken", String.class);
    private static final Property<?>[] properties;

    static {
        properties = new Property<?>[]{clientId, redirectURI, clientSecret, refreshToken};
    }

    private Config() {
    }

    public static void loadFromFile(@NonNull File file) throws IOException {
        if (file.isDirectory())
            throw new IllegalArgumentException("Supplied filepath must point to a file, supplied path: " + file);
        if (file.exists()) {
            if (!file.canRead()) throw new IllegalArgumentException("Can't read file at supplied filepath: " + file);
            try (var reader = new FileReader(file)) {
                var parser = JsonParser.parseReader(reader).getAsJsonObject();
                for (var property : properties) {
                    if (property instanceof RequiredProperty<?>) {
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
            for (var iter = Arrays.stream(properties).filter(p -> p instanceof RequiredProperty<?>).iterator();
                 iter.hasNext(); ) {
                var property = iter.next();
                writer.write("    \"" + property.key + "\":\"\"");
                if (iter.hasNext()) writer.write(',');
                writer.write('\n');
            }
            writer.write("}\n");
        }
    }

    @Getter
    public abstract static class Property<T> {
        private final String key;
        private final Class<T> valueType;
        @Setter(AccessLevel.PRIVATE)
        @Getter(AccessLevel.NONE)
        private T value;

        Property(@NonNull String key, Class<T> valueType) {
            this.key = key;
            this.valueType = valueType;
        }

        boolean equalsValueType(Class<?> type) {
            return valueType.equals(type);
        }

        abstract Object get();
    }

    public static class RequiredProperty<T> extends Property<T> {
        RequiredProperty(@NonNull String key, Class<T> valueType) {
            super(key, valueType);
        }

        public T get() {
            return super.value;
        }
    }

    public static class OptionalProperty<T> extends Property<T> {
        OptionalProperty(@NonNull String key, Class<T> valueType) {
            super(key, valueType);
        }

        public Optional<T> get() {
            return Optional.ofNullable(super.value);
        }
    }
}
