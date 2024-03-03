package spotifybackup.app;

import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import spotifybackup.api_wrapper.ApiWrapper;
import spotifybackup.storage.SpotifyObjectRepository;
import spotifybackup.storage.SpotifyUser;

import java.io.IOException;

public class CLI {
    private final ApiWrapper api;
    private final SpotifyObjectRepository repo;
    private final SpotifyUser user;

    CLI() throws IOException, InterruptedException {
        if (!App.dbFileArg.isPresent()) App.println("db file: " + App.dbFileArg.getValue());
        repo = SpotifyObjectRepository.factory(App.dbFileArg.getValue());
        Config.loadFromFile(App.configFileArg.getValue());
        api = new ApiWrapper();
        final var currentUser = api.getCurrentUser().orElseThrow();
        if (App.getMeArg.isPresent()) {
            App.println("Logged in as: " + currentUser.getId());
            App.println("User is already stored: " + repo.exists(currentUser));
        }
        user = repo.persist(currentUser);
        performActions();
    }

    /**
     * Perform various actions based on program arguments.
     */
    private void performActions() throws IOException, InterruptedException {
//        save20LikedSongs();
        saveLikedSongs();
        savePlaylists();
        saveDetailedInfo();
    }

    private void save20LikedSongs() throws IOException, InterruptedException {
        if (App.verboseArg.isPresent()) App.println("Saving 20 Liked Songs");
        var apiSavedTracks = api.getLikedSongs(20, 0);
        repo.persist(apiSavedTracks.getItems(), user);
    }

    private void saveLikedSongs() throws IOException, InterruptedException {
        if (App.verboseArg.isPresent()) App.println("Saving all Liked Songs");
        final int limit = 50;
        int offset = 0;
        Paging<SavedTrack> apiSavedTracks;
        if (App.verboseArg.isPresent()) App.print("Requesting data");
        do {
            if (App.verboseArg.isPresent()) App.print(".");
            apiSavedTracks = api.getLikedSongs(limit, offset);
            repo.persist(apiSavedTracks.getItems(), user);
            offset += limit;
        } while (apiSavedTracks.getNext() != null);
        if (App.verboseArg.isPresent()) App.println("");
    }

    private void savePlaylists() {
        //TODO: persist all playlists and their contents
    }

    private void saveDetailedInfo() {
        //TODO: iterate over all simplified SpotifyObjects, request detailed information and persist it
    }
}
