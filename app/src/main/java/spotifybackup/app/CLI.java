package spotifybackup.app;

import spotifybackup.api_wrapper.ApiWrapper;
import spotifybackup.storage.SpotifyObjectRepository;

import java.io.IOException;

public class CLI {
    private final ApiWrapper api;
    private final SpotifyObjectRepository repo;

    CLI() throws IOException, InterruptedException {
        repo = SpotifyObjectRepository.factory(App.dbFileArg.getValue());
        Config.loadFromFile(App.configFileArg.getValue());
        api = new ApiWrapper();
        final var currentUser = api.getCurrentUser().orElseThrow();
        System.out.println("Logged in as: " + currentUser.getId());
        System.out.println("User is already stored: " + repo.exists(currentUser));
    }
}
