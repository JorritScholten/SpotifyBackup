package spotifybackup.app;

import spotifybackup.api_wrapper.ApiKey;
import spotifybackup.api_wrapper.ApiWrapper;

import java.io.IOException;

public class CLI {
    private final ApiWrapper api;

    CLI() throws IOException, InterruptedException {
        var key = new ApiKey(App.apiKeyFileArg.getValue());
        api = new ApiWrapper(key);
    }
}
