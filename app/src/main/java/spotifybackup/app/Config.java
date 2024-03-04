package spotifybackup.app;

import com.google.gson.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import spotifybackup.app.exception.BlankConfigFieldException;
import spotifybackup.app.exception.ConfigFileException;
import spotifybackup.storage.exception.ConstructorUsageException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Config {
    public static final RequiredProperty<String> clientId = new RequiredProperty<>("clientId", String.class);
    public static final RequiredProperty<URI> redirectURI = new RequiredProperty<>("redirectURI", URI.class);
    public static final OptionalProperty<String> clientSecret = new OptionalProperty<>("clientSecret", String.class);
    public static final OptionalProperty<String> refreshToken = new OptionalProperty<>("refreshToken", String.class);
    public static final PropertyList refreshTokens = new PropertyList("refreshTokens");
    private static final Property<?>[] properties;
    private static File configFile;
    private static JsonObject serializedConfig;

    static {
        properties = new Property<?>[]{clientId, redirectURI, clientSecret, refreshToken};
    }

    /** @apiNote Should not be used, exists to prevent implicit public constructor. */
    private Config() {
        throw new ConstructorUsageException();
    }

    /**
     * Set all Property values, configFile and serializedConfig to null.
     * @apiNote Only to be used for testing purposes.
     */
    static void reloadFieldsForTesting() {
        for (var property : properties) {
            property.setValue(null);
        }
        refreshTokens.setValues(null);
        configFile = null;
        serializedConfig = null;
    }

    /**
     * Load config properties from a file, the file path is stored to allow for the saving of updated values.
     * @param filePath Path to .json config file.
     * @throws ConfigFileException Thrown when filePath doesn't point to an existing config file, a blank config file is
     *                             created at filePath.
     */
    public static void loadFromFile(@NonNull File filePath) throws IOException, ConfigFileException {
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
            serializedConfig = JsonParser.parseReader(reader).getAsJsonObject();
            for (var property : properties) {
                if (property instanceof RequiredProperty<?>) {
                    if (!serializedConfig.has(property.key))
                        throw new BlankConfigFieldException(file + " has missing " + property.key + " field.");
                    String value = serializedConfig.get(property.key).getAsString();
                    if (value.isBlank())
                        throw new BlankConfigFieldException(property.key + " has blank field.");
                    setPropertyValue(property, value);
                } else if (serializedConfig.has(property.key)) {
                    String value = serializedConfig.get(property.key).getAsString();
                    if (!value.isBlank()) setPropertyValue(property, value);
                }
            }
            if (!serializedConfig.has(refreshTokens.key))
                throw new BlankConfigFieldException(file + " has missing " + refreshTokens.key + " field.");
            else refreshTokens.setValues(serializedConfig.get(refreshTokens.key).getAsJsonArray());
            configFile = file;
        }
    }

    @SuppressWarnings("unchecked")
    private static void setPropertyValue(Property<?> property, String value) {
        try {
            if (property.equalsValueType(String.class)) {
                ((Property<String>) property).setValue(value);
            } else if (property.equalsValueType(URI.class)) {
                ((Property<URI>) property).setValue(new URI(value));
            } else {
                throw new IllegalStateException("Unhandled value type of property field: " + property.valueType);
            }
        } catch (URISyntaxException e) {
            throw new ConfigFileException("Redirect URI has improper syntax.");
        }
    }

    private static void createNewFile(@NonNull File file) throws IOException {
        configFile = file;
        serializedConfig = new JsonObject();
        for (var property : properties) {
            serializedConfig.add(property.key, new JsonPrimitive(""));
        }
        serializedConfig.add(refreshTokens.key, new JsonArray());
        writeSerializedConfigToFile();
    }

    private static void writeSerializedConfigToFile() throws IOException {
        try (var writer = new FileWriter(configFile)) {
            var gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(serializedConfig));
            writer.write('\n');
        }
    }

    public static final class PropertyList {
        @Getter
        private final String key;
        private List<String> values;

        public PropertyList(@NonNull String key) {
            this.key = key;
            this.values = new ArrayList<>();
        }

        public String get(int i) {
            return values.get(i);
        }

        private void setValues(JsonArray array) {
            if (array == null) values = null;
            else {
                values = new ArrayList<>();
                array.forEach(e -> values.add(e.getAsString()));
            }
        }

        public void set(@NonNull List<String> values) {
            this.values = values;
            var jsonArray = new JsonArray();
            for (var value : values) jsonArray.add(value);
            serializedConfig.add(key, jsonArray);
            try {
                writeSerializedConfigToFile();
            } catch (IOException e) {
                throw new RuntimeException("Couldn't write to config file at " + configFile.getAbsolutePath() + " " + e);
            }
        }

        public void add(@NonNull String newValue) {
            values.add(newValue);
            set(values);
        }

        public boolean isPresent() {return values != null && !values.isEmpty();}

        public boolean isEmpty() {return values == null || values.isEmpty();}

        public int size() {return values.size();}
    }

    @Getter
    public abstract static sealed class Property<T> permits RequiredProperty, OptionalProperty {
        private final String key;
        private final Class<T> valueType;
        @Setter(AccessLevel.PRIVATE)
        @Getter(AccessLevel.NONE)
        private T value;

        private Property(@NonNull String key, @NonNull Class<T> valueType) {
            this.key = key;
            this.valueType = valueType;
        }

        private boolean equalsValueType(Class<?> type) {
            return valueType.equals(type);
        }

        public T get() {
            if (value == null) throw new NullPointerException();
            return value;
        }

        public void set(@NonNull T value) {
            this.value = value;
            serializedConfig.add(key, new JsonPrimitive(switch (value) {
                case String s -> s;
                case URI uri -> uri.toString();
                default -> throw new IllegalStateException("Unexpected valueType: " + valueType);
            }));
            try {
                writeSerializedConfigToFile();
            } catch (IOException e) {
                throw new RuntimeException("Couldn't write to config file at " + configFile.getAbsolutePath() + " " + e);
            }
        }
    }

    public static final class RequiredProperty<T> extends Property<T> {
        private RequiredProperty(@NonNull String key, @NonNull Class<T> valueType) {
            super(key, valueType);
        }
    }

    public static final class OptionalProperty<T> extends Property<T> {
        private OptionalProperty(@NonNull String key, @NonNull Class<T> valueType) {
            super(key, valueType);
        }

        public boolean isPresent() {
            return super.value != null;
        }

        public boolean isEmpty() {
            return super.value == null;
        }
    }
}
