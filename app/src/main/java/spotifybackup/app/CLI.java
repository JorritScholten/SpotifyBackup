package spotifybackup.app;

import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import spotifybackup.api_wrapper.ApiWrapper;
import spotifybackup.storage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CLI {
    private final SpotifyObjectRepository repo;

    CLI() throws IOException, InterruptedException {
        if (!App.dbFileArg.isPresent()) App.println("db file: " + App.dbFileArg.getValue());
        if (!App.configFileArg.isPresent()) App.println("config file: " + App.configFileArg.getValue());
        repo = SpotifyObjectRepository.factory(App.dbFileArg.getValue());
        Config.loadFromFile(App.configFileArg.getValue());
        performActions();
    }

    private void performActions() throws IOException, InterruptedException {
        if (App.addAccounts.isPresent()) addAccounts();
        if (App.doBackup.isPresent()) {
            if (Config.refreshTokens.isPresent()) for (int i = 0; i < Config.refreshTokens.size(); i++) new Backup(i);
            else new Backup(0);
        }
    }

    private void addAccounts() throws IOException, InterruptedException {
        App.verbosePrintln("Adding " + App.addAccounts.getValue() + " new account(s)");
        for (int i = 0; i < App.addAccounts.getValue(); i++) {
            var api = new ApiWrapper(Config.refreshTokens.size());
            var currentUser = api.getCurrentUser().orElseThrow();
            var user = repo.persist(currentUser);
            App.println("Added account: " + user.getDisplayName().orElseThrow());
            if (App.verboseArg.isPresent() && !user.getSpotifyUserID().equals(user.getDisplayName().orElseThrow()))
                user.getDisplayName().ifPresent(name -> App.println(name + " has user ID: " + user.getSpotifyUserID()));
        }
    }

    private class Backup {
        final ApiWrapper api;
        final SpotifyUser user;

        private Backup(final int accountNumber) throws InterruptedException, IOException {
            api = new ApiWrapper(accountNumber);
            final var currentUser = api.getCurrentUser().orElseThrow();
            App.verbosePrintln("Logged in as: " + currentUser.getDisplayName());
            user = repo.persist(currentUser);
            performBackup();
        }

        /** Perform various backup actions. */
        private void performBackup() throws IOException, InterruptedException {
            var newTrackList = saveLikedSongs();
            markRemovedTracks(newTrackList);
            var newPlaylists = saveFollowedPlaylists();
            markUnfollowedPlaylists(newPlaylists);
            var newFollowedArtists = saveFollowedArtists();
            markUnfollowedArtists(newFollowedArtists);
            var newLikedAlbums = saveLikedAlbums();
            markUnlikedAlbums(newLikedAlbums);
            saveDetailedInfo();
        }

        /** @return List of Liked Songs currently in the users' account as returned from the API. */
        private List<SpotifySavedTrack> saveLikedSongs() throws IOException, InterruptedException {
            App.verbosePrint("  Saving all Liked Songs");
            final int limit = 50;
            int offset = 0;
            Paging<SavedTrack> apiSavedTracks;
            List<SpotifySavedTrack> tracks = new ArrayList<>();
            App.verbosePrint(", requesting data");
            do {
                App.verbosePrint(".");
                apiSavedTracks = api.getLikedSongs(limit, offset);
                tracks.addAll(repo.persist(apiSavedTracks.getItems(), user));
                offset += limit;
            } while (apiSavedTracks.getNext() != null);
            App.verbosePrintln("");
            return tracks;
        }

        /** @return List of playlists currently followed by user as returned from the API. */
        private List<SpotifyPlaylist> saveFollowedPlaylists() throws IOException, InterruptedException {
            App.verbosePrint("  Saving all playlists");
            final int limit = 50;
            int offset = 0;
            Paging<PlaylistSimplified> apiPlaylists;
            List<SpotifyPlaylist> playlists = new ArrayList<>();
            App.verbosePrint(", requesting data");
            do {
                App.verbosePrint(".");
                apiPlaylists = api.getCurrentUserPlaylists(limit, offset);
                playlists.addAll(repo.persist(apiPlaylists.getItems()));
                offset += limit;
            } while (apiPlaylists.getNext() != null);
            repo.followPlaylists(playlists, user);
            App.verbosePrintln("");
            return playlists;
        }

        /** @return List of artists currently followed by user as returned from the API. */
        private List<SpotifyArtist> saveFollowedArtists() {
            throw new UnsupportedOperationException("to be implemented");
        }

        /** @return List of albums currently liked by user as returned from the API. */
        private List<SpotifyAlbum> saveLikedAlbums() {
            throw new UnsupportedOperationException("to be implemented");
        }

        private void markRemovedTracks(final List<SpotifySavedTrack> newSavedTracks) {
            var newSavedTrackIds = newSavedTracks.stream().map(SpotifySavedTrack::getId).collect(Collectors.toSet());
            var oldSavedTracks = repo.getSavedTracks(user);
            // filter using record ids instead of object compare (removeAll calling equalsTo) because SpotifySavedTrack has
            // no equalsTo method that works on internal fields
            var removed = oldSavedTracks.stream().filter(t -> !newSavedTrackIds.contains(t.getId())).toList();
            if (!removed.isEmpty()) {
                for (var track : removed) repo.removeSavedTrack(track.getTrack(), user);
                App.verbosePrintln("  Removed " + removed.size() + " track(s) from Liked Songs");
            }
        }

        private void markUnfollowedPlaylists(final List<SpotifyPlaylist> newPlaylists) {
            var newPlaylistIds = newPlaylists.stream().map(SpotifyPlaylist::getId).collect(Collectors.toSet());
            var oldPlaylists = repo.getFollowedPlaylists(user);
            var removed = oldPlaylists.stream().filter(p -> !newPlaylistIds.contains(p.getId())).toList();
            if (!removed.isEmpty()) {
                repo.unfollowPlaylists(removed, user);
                App.verbosePrintln("  Unfollowed " + removed.size() + " playlist(s)");
            }
        }

        private void markUnfollowedArtists(final List<SpotifyArtist> newFollowedArtists) {
            throw new UnsupportedOperationException("to be implemented");
        }

        private void markUnlikedAlbums(final List<SpotifyAlbum> newLikedAlbums) {
            throw new UnsupportedOperationException("to be implemented");
        }

        private void saveDetailedInfo() {
            throw new UnsupportedOperationException("TODO: iterate over all simplified SpotifyObjects, " +
                    "request detailed information and persist it");
        }
    }
}
