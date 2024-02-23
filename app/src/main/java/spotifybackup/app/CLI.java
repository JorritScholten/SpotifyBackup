package spotifybackup.app;

import spotifybackup.api_wrapper.ApiWrapper;
import spotifybackup.storage.SpotifyObjectRepository;
import spotifybackup.storage.SpotifyUser;

import java.io.IOException;

public class CLI {
    private final ApiWrapper api;
    private final SpotifyObjectRepository repo;
    private final SpotifyUser user;

    CLI() throws IOException, InterruptedException {
        if (!App.dbFileArg.isPresent()) System.out.println("db file: " + App.dbFileArg.getValue());
        repo = SpotifyObjectRepository.factory(App.dbFileArg.getValue());
        Config.loadFromFile(App.configFileArg.getValue());
        api = new ApiWrapper();
        final var currentUser = api.getCurrentUser().orElseThrow();
        if (App.getMeArg.getValue()) {
            System.out.println("Logged in as: " + currentUser.getId());
            System.out.println("User is already stored: " + repo.exists(currentUser));
        }
        user = repo.persist(currentUser);
    }

    public void save_liked_songs() throws IOException {
        System.out.println("Saving 20 Liked Songs");
        var apiSavedTracks = api.getLikedSongs();
        repo.persist(apiSavedTracks.getItems(), user);
    }
}
