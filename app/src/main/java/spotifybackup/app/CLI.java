package spotifybackup.app;

import spotifybackup.api_wrapper.ApiKey;
import spotifybackup.api_wrapper.ApiWrapper;
import spotifybackup.storage.SpotifyObjectRepository;

import java.io.IOException;

public class CLI {
    private final ApiWrapper api;
    private final SpotifyObjectRepository repo;

    CLI() throws IOException, InterruptedException {
        repo = SpotifyObjectRepository.factory(App.dbFileArg.getValue());
        var key = new ApiKey(App.apiKeyFileArg.getValue());
        api = new ApiWrapper(key);
    }
}
