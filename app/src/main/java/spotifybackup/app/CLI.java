package spotifybackup.app;

import se.michaelthelin.spotify.model_objects.AbstractModelObject;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PagingCursorbased;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import spotifybackup.api_wrapper.ApiWrapper;
import spotifybackup.storage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CLI {
    private final SpotifyObjectRepository repo;

    CLI() throws IOException, InterruptedException {
        if (!App.dbFileArg.isPresent()) App.println("db file: " + App.dbFileArg.getValue());
        if (!App.configFileArg.isPresent()) App.println("config file: " + App.configFileArg.getValue());
        if (App.sqlOutputFileArg.isPresent()) App.println("sql scripts file: " + App.sqlOutputFileArg.getValue());
        repo = SpotifyObjectRepository.factory(App.dbFileArg.getValue());
        App.config = Config.loadFromFile(App.configFileArg.getValue());
        performActions();
    }

    private void performActions() throws IOException, InterruptedException {
        if (App.addAccounts.isPresent()) addAccounts();
        if (App.doBackup.isPresent()) {
            if (App.config.getUsers().length > 0) for (var user : App.config.getUsers()) new Backup(user);
            else new Backup(App.config.addEmptyUser());
        }
        if (App.sqlOutputFileArg.isPresent()) repo.outputDatabaseToSQLScript(App.sqlOutputFileArg.getValue());
    }

    private void addAccounts() throws IOException, InterruptedException {
        App.verbosePrintln("Adding " + App.addAccounts.getValue() + " new account(s)");
        for (int i = 0; i < App.addAccounts.getValue(); i++) {
            var api = new ApiWrapper(App.config.addEmptyUser(), App.getConfig());
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

        private Backup(final Config.UserInfo account) throws InterruptedException, IOException {
            api = new ApiWrapper(account, App.getConfig());
            final var currentUser = api.getCurrentUser().orElseThrow();
            App.verbosePrintln("Logged in as: " + currentUser.getDisplayName());
            user = repo.persist(currentUser);
            performBackup();
        }

        /** Perform various backup actions. */
        private void performBackup() {
            saveLikedSongs();
            saveFollowedPlaylists();
            saveFollowedArtists();
            saveLikedAlbums();
            saveDetailedInfo();
        }

        private void saveLikedSongs() {
            var oldTrackIds = repo.getSavedTrackIds(user);
            List<SpotifySavedTrack> newTracks = new ArrayList<>();
            var pageItems = getFromApiPaged(2, "Saving all Liked Songs", api::getLikedSongs);
            for (var items : pageItems) newTracks.addAll(repo.persist(items, user));
            var newTrackIds = newTracks.stream().map(t -> t.getTrack().getSpotifyID().getId()).collect(Collectors.toList());
            newTrackIds.removeAll(oldTrackIds);
            if (!newTrackIds.isEmpty())
                App.verbosePrintln(4, "Added " + newTrackIds.size() + " track(s) to Liked songs");
            markRemovedTracks(newTracks);
        }

        private void saveFollowedPlaylists() {
            var oldPlaylistIds = repo.getFollowedPlaylistIds(user);
            List<SpotifyPlaylist> newPlaylists = new ArrayList<>();
            var pageItems = getFromApiPaged(2, "Saving all playlists", api::getCurrentUserPlaylists);
            for (var items : pageItems) newPlaylists.addAll(repo.persist(items));
            var newPlaylistIds = newPlaylists.stream().map(p -> p.getSpotifyID().getId()).collect(Collectors.toList());
            newPlaylistIds.removeAll(oldPlaylistIds);
            if (!newPlaylistIds.isEmpty())
                App.verbosePrintln(4, "Following " + newPlaylistIds.size() + " new playlist(s)");
            repo.followPlaylists(newPlaylists, user);
            markUnfollowedPlaylists(newPlaylists);
        }

        private void saveFollowedArtists() {
            var oldArtistIds = repo.getFollowedArtistIds(user);
            List<SpotifyArtist> newArtists = new ArrayList<>();
            var pageItems = getFromApiPagedCursor(2, "Saving followed artists", api::getCurrentUserFollowedArtists);
            for (var items : pageItems) newArtists.addAll(repo.persist(items, App.imageSaveRestriction.getValue()));
            var newArtistIds = newArtists.stream().map(a -> a.getSpotifyID().getId()).collect(Collectors.toList());
            newArtistIds.removeAll(oldArtistIds);
            if (!newArtistIds.isEmpty())
                App.verbosePrintln(4, "Following " + newArtistIds.size() + " new artist(s)");
            repo.followArtists(newArtists, user);
            markUnfollowedArtists(newArtists);
        }

        private void saveLikedAlbums() {
            var oldAlbumIds = repo.getSavedAlbumIds(user);
            List<SpotifySavedAlbum> newAlbums = new ArrayList<>();
            var pageItems = getFromApiPaged(2, "Saving all liked albums", api::getCurrentUserSavedAlbums);
            for (var items : pageItems)
                newAlbums.addAll(repo.persist(items, user, App.imageSaveRestriction.getValue()));
            var newAlbumIds = newAlbums.stream().map(a -> a.getAlbum().getSpotifyID().getId()).collect(Collectors.toList());
            newAlbumIds.removeAll(oldAlbumIds);
            if (!newAlbumIds.isEmpty())
                App.verbosePrintln(4, "Added " + newAlbumIds.size() + " album(s) to liked");
            markUnlikedAlbums(newAlbums);
        }

        private <A extends AbstractModelObject> List<A[]>
        getFromApiPaged(int spaces, String message, BiFunction<Integer, Integer, Paging<A>> getPage) {
            App.verbosePrint(spaces, message);
            final int limit = 50;
            int offset = 0;
            Paging<A> apiPage;
            List<A[]> apiItems = new ArrayList<>();
            App.verbosePrint(", requesting data");
            do {
                App.verbosePrint(".");
                apiPage = getPage.apply(limit, offset);
                apiItems.add(apiPage.getItems());
                offset += limit;
            } while (apiPage.getNext() != null);
            App.println("");
            return apiItems;
        }

        private <A extends AbstractModelObject> List<A[]>
        getFromApiPagedCursor(int spaces, String message, BiFunction<Integer, String, PagingCursorbased<A>> getPage) {
            App.verbosePrint(spaces, message);
            final int limit = 50;
            String after = null;
            PagingCursorbased<A> apiPage;
            List<A[]> apiItems = new ArrayList<>();
            App.verbosePrint(", requesting data");
            do {
                App.verbosePrint(".");
                apiPage = getPage.apply(limit, after);
                apiItems.add(apiPage.getItems());
                after = apiPage.getCursors()[0].getAfter();
            } while (apiPage.getNext() != null);
            App.println("");
            return apiItems;
        }

        private void markRemovedTracks(final List<SpotifySavedTrack> newSavedTracks) {
            var newSavedTrackIds = newSavedTracks.stream().map(SpotifySavedTrack::getId).collect(Collectors.toSet());
            var allSavedTracks = repo.getSavedTracks(user);
            // filter using record ids instead of object compare (removeAll calling equalsTo) because SpotifySavedTrack has
            // no equalsTo method that works on internal fields
            var removed = allSavedTracks.stream().filter(t -> !newSavedTrackIds.contains(t.getId())).toList();
            if (!removed.isEmpty()) {
                for (var track : removed) repo.removeSavedTrack(track.getTrack(), user);
                App.verbosePrintln(4, "Removed " + removed.size() + " track(s) from Liked Songs");
            }
        }

        private void markUnfollowedPlaylists(final List<SpotifyPlaylist> newPlaylists) {
            var newPlaylistIds = newPlaylists.stream().map(SpotifyPlaylist::getId).collect(Collectors.toSet());
            var allPlaylists = repo.getFollowedPlaylists(user);
            var removed = allPlaylists.stream().filter(p -> !newPlaylistIds.contains(p.getId())).toList();
            if (!removed.isEmpty()) {
                repo.unfollowPlaylists(removed, user);
                App.verbosePrintln(4, "Unfollowed " + removed.size() + " playlist(s)");
            }
        }

        private void markUnfollowedArtists(final List<SpotifyArtist> newFollowedArtists) {
            var newArtisIds = newFollowedArtists.stream().map(SpotifyArtist::getId).collect(Collectors.toSet());
            var allArtists = repo.getFollowedArtists(user);
            var removed = allArtists.stream().filter(a -> !newArtisIds.contains(a.getId())).toList();
            if (!removed.isEmpty()) {
                repo.unfollowArtists(removed, user);
                App.verbosePrintln(4, "Unfollowed " + removed.size() + " artists(s)");
            }
        }

        private void markUnlikedAlbums(final List<SpotifySavedAlbum> newLikedAlbums) {
            var newSavedAlbumIds = newLikedAlbums.stream().map(SpotifySavedAlbum::getId).collect(Collectors.toSet());
            var allSavedAlbums = repo.getSavedAlbums(user);
            var removed = allSavedAlbums.stream().filter(p -> !newSavedAlbumIds.contains(p.getId())).toList();
            if (!removed.isEmpty()) {
                for (var album : removed) repo.removeSavedAlbum(album.getAlbum(), user);
                App.verbosePrintln(4, "Removed " + removed.size() + " album(s) from Saved Albums");
            }
        }

        private List<String> combineIds(final List<String> separateIds, final int limit) {
            List<String> combined = new ArrayList<>();
            for (int i = 0; i <= separateIds.size() / limit; i++) {
                combined.add(String.join(",",
                        separateIds.subList(i * limit, Math.min(i * limit + limit, separateIds.size()))));
            }
            return combined;
        }

        private void saveDetailedInfo() {
            App.verbosePrintln(2, "Requesting detailed information for simplified objects");
            saveDetailedPlaylistInfo();
            saveDetailedAlbumInfo();
            saveDetailedArtistInfo();
            saveDetailedTrackInfo();
        }

        private void saveDetailedPlaylistInfo() {
            final var playlists = repo.findAllPlaylists();
            if (playlists.isEmpty()) return;
            if (playlists.stream().anyMatch(SpotifyPlaylist::getIsSimplified)) {
                App.verbosePrintln(4, playlists.stream().filter(SpotifyPlaylist::getIsSimplified).count() +
                        " new playlist(s)");
            }
            for (var playlist : playlists) {
                Optional<Playlist> apiPlaylist = api.getPlaylistWithoutTracks(playlist.getSpotifyID());
                if (apiPlaylist.isEmpty())
                    App.println(6, "Couldn't request detailed information for playlist " +
                            playlist.getName());
                else if (playlist.getIsSimplified() ||
                        !apiPlaylist.get().getSnapshotId().equals(playlist.getSnapshotId())) {
                    savePlaylistTracks(playlist, apiPlaylist.get());
                }
            }
        }

        private void savePlaylistTracks(SpotifyPlaylist playlist, Playlist apiPlaylist) {
            List<PlaylistTrack> apiTracks = new ArrayList<>();
            getFromApiPaged(6, "Requesting tracks for " + playlist.getName(),
                    (l, o) -> api.getPlaylistTracks(l, o, playlist.getSpotifyID()))
                    .forEach(a -> apiTracks.addAll(Arrays.asList(a)));
            if (apiTracks.size() == apiPlaylist.getTracks().getTotal()) {
                App.verbosePrintln(8, "Saving " + apiTracks.size() + " track(s) for " +
                        playlist.getName());
                repo.deletePlaylistItems(playlist);
                repo.persist(apiTracks, playlist);
                if (playlist.getIsSimplified()) repo.persist(apiPlaylist);
                else repo.update(apiPlaylist);
            } else {
                App.println(6, "Size mismatch between requested track amount and the " +
                        "amount that there should be for playlist " + playlist.getName());
            }
        }

        private void saveDetailedAlbumInfo() {
            final var simpleAlbumIds = repo.getSimplifiedAlbumsSpotifyIDs();
            if (simpleAlbumIds.isEmpty()) return;
            App.verbosePrint(4, "Requesting data for " + simpleAlbumIds.size() + " album(s)");
            for (var ids : combineIds(simpleAlbumIds, 20)) {
                App.verbosePrint(".");
                repo.persistWithoutTracks(api.getSeveralAlbums(ids), App.imageSaveRestriction.getValue());
            }
            App.verbosePrintln("");
        }

        private void saveDetailedArtistInfo() {
            final var simpleArtistIds = repo.getSimplifiedArtistsSpotifyIDs();
            if (simpleArtistIds.isEmpty()) return;
            App.verbosePrint(4, "Requesting data for " + simpleArtistIds.size() + " artist(s)");
            for (var ids : combineIds(simpleArtistIds, 50)) {
                App.verbosePrint(".");
                repo.persist(api.getSeveralArtists(ids), App.imageSaveRestriction.getValue());
            }
            App.verbosePrintln("");
        }

        private void saveDetailedTrackInfo() {
            final var simpleTrackIds = repo.getSimplifiedTracksSpotifyIDs();
            if (simpleTrackIds.isEmpty()) return;
            App.verbosePrint(4, "Requesting data for " + simpleTrackIds.size() + " track(s)");
            for (var ids : combineIds(simpleTrackIds, 50)) {
                App.verbosePrint(".");
                repo.persist(api.getSeveralTracks(ids));
            }
            App.verbosePrintln("");
        }
    }
}
