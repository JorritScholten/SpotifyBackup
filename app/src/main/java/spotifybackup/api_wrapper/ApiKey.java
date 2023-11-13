package spotifybackup.api_wrapper;

import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@ToString
public class ApiKey {
    private static final String CLIENT_ID_NAME = "clientId";
    private static final String REDIRECT_URI_NAME = "redirectUri";
    private static final String CLIENT_SECRET_NAME = "clientSecret";
    @Getter
    private final String clientId;
    @Getter
    private final URI redirectUri;
    private final String clientSecret;

    public ApiKey(@NonNull File file) throws IOException {
        if (!file.isFile())
            throw new IllegalArgumentException("Supplied ApiKey filepath must point to a file, supplied path: " + file);
        if (file.exists()) {
            if (!file.canRead()) throw new IllegalArgumentException("Can't read file at supplied filepath: " + file);
            try (var reader = new FileReader(file)) {
                var parser = JsonParser.parseReader(reader).getAsJsonObject();

                if (!parser.has(CLIENT_ID_NAME)) throw new IllegalArgumentException(file + " missing clientId field.");
                clientId = parser.get(CLIENT_ID_NAME).getAsString();

                if (!parser.has(CLIENT_SECRET_NAME) || parser.get(CLIENT_SECRET_NAME).getAsString().isBlank())
                    clientSecret = null;
                else clientSecret = parser.get(CLIENT_SECRET_NAME).getAsString();

                if (!parser.has(REDIRECT_URI_NAME))
                    throw new IllegalArgumentException(file + " missing redirectUri field.");
                redirectUri = new URI(parser.get(REDIRECT_URI_NAME).getAsString());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Redirect URI has improper syntax.");
            }
        } else {
            createNewFile(file);
            throw new IllegalArgumentException("Created empty ApiKey file, please fill in the fields: " + file);
        }
    }

    private static void createNewFile(File file) throws IOException {
        try (var writer = new FileWriter(file)) {
            writer.write("{\n");
            writer.write("\t\"clientId\":\"\",\n");
            writer.write("\t\"redirectUri\":\"\",\n");
            writer.write("\t\"clientSecret\":\"\"\n");
            writer.write("}");
        }
    }

    public Optional<String> getClientSecret() {
        return Optional.ofNullable(clientSecret);
    }
}
