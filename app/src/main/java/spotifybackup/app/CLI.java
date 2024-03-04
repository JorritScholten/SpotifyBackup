package spotifybackup.app;

import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import spotifybackup.api_wrapper.ApiWrapper;
import spotifybackup.storage.SpotifyObjectRepository;
import spotifybackup.storage.SpotifySavedTrack;
import spotifybackup.storage.SpotifyUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CLI {
    private final SpotifyObjectRepository repo;

    CLI() throws IOException, InterruptedException {
        if (!App.dbFileArg.isPresent()) App.println("db file: " + App.dbFileArg.getValue());
        repo = SpotifyObjectRepository.factory(App.dbFileArg.getValue());
        Config.loadFromFile(App.configFileArg.getValue());
        new Backup();
    }

    private class Backup {
        final ApiWrapper api;
        final SpotifyUser user;

        private Backup() throws InterruptedException, IOException {
            api = new ApiWrapper();
            final var currentUser = api.getCurrentUser().orElseThrow();
            if (App.verboseArg.isPresent()) App.println("Logged in as: " + currentUser.getId());
            user = repo.persist(currentUser);
            performBackup();
        }

        /** Perform various backup actions. */
        private void performBackup() throws IOException, InterruptedException {
            var newTrackList = saveLikedSongs();
            markRemovedTracks(newTrackList);
            savePlaylists();
            saveDetailedInfo();
        }

        /** @return List of Liked Songs currently in the users' account. */
        private List<SpotifySavedTrack> saveLikedSongs() throws IOException, InterruptedException {
            if (App.verboseArg.isPresent()) App.println("Saving all Liked Songs");
            final int limit = 50;
            int offset = 0;
            Paging<SavedTrack> apiSavedTracks;
            List<SpotifySavedTrack> tracks = new ArrayList<>();
            if (App.verboseArg.isPresent()) App.print("Requesting data");
            do {
                if (App.verboseArg.isPresent()) App.print(".");
                apiSavedTracks = api.getLikedSongs(limit, offset);
                tracks.addAll(repo.persist(apiSavedTracks.getItems(), user));
                offset += limit;
            } while (apiSavedTracks.getNext() != null);
            if (App.verboseArg.isPresent()) App.println("");
            return tracks;
        }

        private void markRemovedTracks(List<SpotifySavedTrack> newSavedTracks) {
            var newSavedTrackIds = newSavedTracks.stream().map(SpotifySavedTrack::getId).collect(Collectors.toSet());
            var oldSavedTracks = repo.getSavedTracks(user);
            // filter using record ids instead of object compare (removeAll calling equalsTo) because SpotifySavedTrack has
            // no equalsTo method that works on internal fields
            var removed = oldSavedTracks.stream().filter(t -> !newSavedTrackIds.contains(t.getId())).toList();
            if (!removed.isEmpty()) {
                for (var track : removed) repo.removeSavedTrack(track.getTrack(), user);
                if (App.verboseArg.isPresent()) App.println("Removed " + removed.size() + " from Liked Songs");
            }
        }

        private void savePlaylists() throws IOException, InterruptedException {
            if (App.verboseArg.isPresent()) App.println("Saving all playlists of current user");
            final int limit = 50;
            int offset = 0;
            Paging<PlaylistSimplified> apiPlaylists;
            if (App.verboseArg.isPresent()) App.print("Requesting data");
            do {
                if (App.verboseArg.isPresent()) App.print(".");
                apiPlaylists = api.getCurrentUserPlaylists(limit, offset);
                repo.persist(apiPlaylists.getItems());
                offset += limit;
            } while (apiPlaylists.getNext() != null);
            if (App.verboseArg.isPresent()) App.println("");
        }

        private void saveDetailedInfo() {
            //TODO: iterate over all simplified SpotifyObjects, request detailed information and persist it
        }
    }
}
