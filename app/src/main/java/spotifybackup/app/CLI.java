package spotifybackup.app;

import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import se.michaelthelin.spotify.model_objects.AbstractModelObject;
import se.michaelthelin.spotify.model_objects.specification.*;
import spotifybackup.api_wrapper.ApiWrapper;
import spotifybackup.app.exception.BlankConfigFieldException;
import spotifybackup.app.exception.ConfigFileException;
import spotifybackup.app.exception.ConfigReferenceLoopException;
import spotifybackup.app.exception.ConfigUsersFieldException;
import spotifybackup.storage.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CLI extends TerminalInteraction {
    private final SpotifyObjectRepository repo;

    CLI() throws IOException, InterruptedException {
        App.dbFileArg.ifNotPresent(path -> verbosePrintln("Database file: " + path));
        App.configFileArg.ifNotPresent(path -> verbosePrintln("Config file: " + path));
        App.sqlOutputFileArg.ifPresent(path -> verbosePrintln("SQL scripts file: " + path));
        App.noBackups.ifPresent(() -> println("Performing no backups."));
        repo = SpotifyObjectRepository.factory(App.dbFileArg.getValue());
        try {
            Config.loadAppConfigFromFile(App.configFileArg.getValue());
            App.setConfigValues.ifPresent(() -> setConfigValues(false));
        } catch (BlankConfigFieldException e) {
            println(e.getMessage());
            setConfigValues(false);
        } catch (ConfigUsersFieldException e) {
            throw new ConfigUsersFieldException("TODO: handle automatic recovery of " + e.getMessage());
        } catch (ConfigFileException e) {
            setConfigValues(true);
        }
        performActions();
    }

    private void performActions() throws IOException, InterruptedException {
        if (App.addAccounts.isPresent()) addAccounts();
        App.configureBackups.ifPresent(this::setAccountsToBackup);
        App.configureCloningTargets.ifPresent(this::setCloningTargets);
        if (!App.noBackups.isPresent()) {
            if (!App.config.getUsers().isEmpty())
                for (var user : App.config.getUsers().stream().filter(Config.UserInfo::getDoBackup).toList())
                    new Backup(user);
            else new Backup(App.config.addEmptyUser(true));
        }
        App.showTotalLibraryDuration.ifPresent(this::printTotalLibraryDurations);
        App.sqlOutputFileArg.ifPresent(repo::outputDatabaseToSQLScript);
        App.listUserAccounts.ifPresent(this::listUserAccounts);
    }

    private void setCloningTargets() {
        if (App.config.getUsers().isEmpty()) {
            println("No accounts in config to configure settings for.");
            return;
        }
        final var accounts = App.config.getUsers().stream().filter(u -> u.getSpotifyId().isPresent()).toList();
        if (accounts.isEmpty()) return;
        do {
            listUserAccountsInConfig("\nCurrent configuration of accounts in config.", accounts);
            final var accountsWithTargets = accounts.stream().filter(Config.UserInfo::hasCloningTargets).toList();
            if (!accountsWithTargets.isEmpty()) {
                listUserAccountsInConfig("\nSelect accounts by the left-most number.", accountsWithTargets);
                removeCloningTargets(accountsWithTargets);
            }
            listUserAccountsInConfig("\nSelect accounts by the left-most number.", accounts);
            addCloningTargets(accounts);
            listUserAccountsInConfig("\nCurrent configuration of accounts in config.", accounts);
        } while (!confirmUsingCharYN("Finished configuring which accounts to clone?", 'y'));
    }

    private void addCloningTargets(List<Config.UserInfo> accounts) {
        for (var account : chooseZeroOrMoreFromList("Specify which accounts should be cloned.", accounts)) {
            final String spotifyId = account.getSpotifyId().orElseThrow();
            if (!account.getDoBackup()) {
                if (account.isCloningTarget()) {
                    println("Account with Spotify user ID " + spotifyId + " can't be cloned because it is not set to " +
                            "perform backups and is a cloning target.");
                    continue;
                } else try {
                    if (confirmUsingCharYN("Configure account with Spotify user ID " + spotifyId +
                            " to do backups?", 'y'))
                        account.setDoBackup(true);
                    else continue;
                } catch (ConfigReferenceLoopException e) {
                    throw new RuntimeException("This shouldn't be capable of happening here: " + e.getMessage());
                }
            }
            final var validTargets = accounts.stream().filter(a -> !a.getDoBackup()).toList();
            if (validTargets.isEmpty()) {
                println("Can't configure cloning targets because there are no accounts not marked for backup.");
                continue;
            }
            listUserAccountsInConfig("Valid cloning targets for " + spotifyId, validTargets);
            chooseZeroOrMoreFromList("Specify which accounts to clone to.", validTargets).forEach(target -> {
                try {
                    account.addCloneTarget(target);
                } catch (ConfigReferenceLoopException e) {
                    throw new RuntimeException("This shouldn't be capable of happening here: " + e.getMessage());
                }
            });
        }
    }

    private void removeCloningTargets(List<Config.UserInfo> accounts) {
        chooseZeroOrMoreFromList("Specify which accounts should have their cloning targets removed.", accounts
                                ).forEach(account -> {
            if (account.hasCloningTargets()) {
                listUserAccountsInConfig("Cloning targets of " + account.getSpotifyId().orElseThrow(),
                        account.getCloneTargets());
                chooseZeroOrMoreFromList("Specify which cloning targets to remove.", account.getCloneTargets())
                        .forEach(account::removeCloneTarget);
            }
        });
    }

    private void setAccountsToBackup() {
        if (App.config.getUsers().isEmpty()) {
            println("No accounts in config to configure settings for.");
            return;
        }
        final var accounts = App.config.getUsers().stream().filter(u -> u.getSpotifyId().isPresent()).toList();
        do {
            listUserAccountsInConfig("\nSelect accounts by the left-most number.", accounts);
            chooseZeroOrMoreFromList("Specify which accounts should have their backup settings toggled.",
                    accounts).forEach(account ->
            {
                try {
                    account.setDoBackup(!account.getDoBackup());
                } catch (ConfigReferenceLoopException e) {
                    if (confirmUsingCharYN(e.getMessage() + " Modify cloning targets?", 'y')) {
                        removeCloningTargets(accounts);
                        if (!account.isCloningTarget() && !account.hasCloningTargets()) try {
                            account.setDoBackup(!account.getDoBackup());
                        } catch (ConfigReferenceLoopException ex) {
                            throw new RuntimeException(
                                    "This shouldn't be capable of happening here: " + ex.getMessage());
                        }
                        listUserAccountsInConfig("\nNew configuration of accounts in config.", accounts);
                    }
                }
            });
            listUserAccountsInConfig("\nNew configuration of accounts in config.", accounts);
        } while (!confirmUsingCharYN("Finished configuring which accounts to backup?", 'y'));
    }

    private void setConfigValues(boolean firstConfig) {
        if (firstConfig || confirmUsingCharYN("Set Spotify client ID?", 'y')) {
            App.config.setClientId(askForNonBlankString("Please enter the Spotify client ID: "));
        }
        if (firstConfig || confirmUsingCharYN("Set Spotify redirect URI?", 'y')) {
            App.config.setRedirectURI(askForRedirectURI("Please enter the Spotify redirect URI: "));
        }
        if (!confirmUsingCharYN("Set Spotify client secret?", 'n')) {
            var secret = askForString("Please enter the Spotify client secret (enter blank to clear value): ");
            if (secret.isBlank()) App.config.clearClientSecret();
            else App.config.setClientSecret(secret);
        }
    }

    private void listUserAccounts() {
        println("");
        listUserAccountsInDb();
        println("");
        listUserAccountsInConfig("User accounts in the config file.",
                App.config.getUsers().stream().filter(u -> u.getSpotifyId().isPresent()).toList());
    }

    private void listUserAccountsInDb() {
        var accounts = repo.getAccountHolders();
        final int spacing = 2;
        final int countMaxWidth = ("" + accounts.size()).length();
        final var idHeading = "Spotify ID";
        final int idMaxWidth = accounts.stream().map(u -> u.getSpotifyUserID().length()).reduce(Integer::max)
                .orElse(idHeading.length());
        println("User accounts in the database.");
        println(countMaxWidth + spacing, idHeading +
                " ".repeat(spacing + idMaxWidth - idHeading.length()) + "Account display name");
        int count = 1;
        for (var account : accounts) {
            final int countWidth = ("" + count).length();
            final int idWidth = account.getSpotifyUserID().length();
            println(countMaxWidth - countWidth, count + " ".repeat(spacing)
                    + account.getSpotifyUserID() + " ".repeat(
                    spacing + (idMaxWidth - idWidth)) + account.getDisplayName().orElse(""));
            count++;
        }
    }

    private void listUserAccountsInConfig(@NonNull String preMessage, @NonNull List<Config.UserInfo> accounts) {
        final int spacing = 2;
        final int countMaxWidth = ("" + accounts.size()).length();
        final var idHeading = "Spotify ID";
        final int idMaxWidth = accounts.stream().map(u -> u.getSpotifyId().orElseThrow().length()).reduce(Integer::max)
                .filter(w -> w > idHeading.length()).orElse(idHeading.length());
        final var nameHeading = "Account display name";
        final int nameMaxWidth = accounts.stream().map(u -> u.getDisplayName().orElse(nameHeading).length())
                .reduce(Integer::max).filter(w -> w > nameHeading.length()).orElse(nameHeading.length());
        final var backupHeading = "Perform backup?";
        println(preMessage);
        println(countMaxWidth + spacing,
                idHeading + " ".repeat(spacing + idMaxWidth - idHeading.length()) +
                        nameHeading + " ".repeat(spacing + nameMaxWidth - nameHeading.length()) +
                        backupHeading + " ".repeat(spacing) +
                        "Cloning target(s)");
        int count = 1;
        for (var account : accounts) {
            final int countWidth = ("" + count).length();
            final int idWidth = account.getSpotifyId().orElseThrow().length();
            final int nameWidth = account.getDisplayName().orElse("").length();
            final int backupWidth = ("" + account.getDoBackup()).length();
            println(countMaxWidth - countWidth, count + " ".repeat(spacing) +
                    account.getSpotifyId().orElseThrow() + " ".repeat(spacing + (idMaxWidth - idWidth)) +
                    account.getDisplayName().orElse("") + " ".repeat(spacing + (nameMaxWidth - nameWidth)) +
                    account.getDoBackup() + " ".repeat(spacing + backupHeading.length() - backupWidth) +
                    String.join(", ",
                            account.getCloneTargets().stream().map(u -> u.getSpotifyId().orElseThrow()).toList()));
            count++;
        }
    }

    private void addAccounts() throws IOException, InterruptedException {
        verbosePrintln("Adding " + App.addAccounts.getValue() + " new account(s)");
        for (int i = 0; i < App.addAccounts.getValue(); i++) {
            var api = new ApiWrapper(App.config.addEmptyUser(
                    confirmUsingCharYN("Perform backups for this account?", 'y')), App.getConfig());
            var currentUser = api.getCurrentUser().orElseThrow();
            var user = repo.persist(currentUser);
            println("Added account: " + user.getDisplayName().orElseThrow());
            if (App.verboseArg.isPresent() && !user.getSpotifyUserID().equals(user.getDisplayName().orElseThrow()))
                user.getDisplayName().ifPresent(name -> println(name + " has user ID: " + user.getSpotifyUserID()));
        }
    }

    private void printTotalLibraryDurations() {
        for (var account : repo.getAccountHolders()) {
            var tracks = repo.getSavedTracks(account);
            long durationMs = tracks.stream().map(s -> s.getTrack().getDurationMs().longValue()).reduce(0L, Long::sum);
            println("Account [" + account.getDisplayName().orElseGet(account::getSpotifyUserID) +
                    "] has a total library duration: " + msToPrettyString(durationMs));
        }
    }

    private String msToPrettyString(final long durationMs) {
        return DurationFormatUtils.formatDurationWords(durationMs, true, true);
    }

    public enum PlaylistFilter {
        ONLY_USER("only user made"),
        ALL_BUT_SPOTIFY("all but generated by spotify"),
        ALL("all");
        final String message;

        PlaylistFilter(String message) {
            this.message = message;
        }
    }

    private class Backup {
        static final String SPOTIFY_USER_ID = "spotify";
        final ApiWrapper api;
        final SpotifyUser user;

        private Backup(final Config.UserInfo account) throws InterruptedException, IOException {
            api = new ApiWrapper(account, App.getConfig());
            final var currentUser = api.getCurrentUser().orElseThrow();
            if (App.verboseArg.isPresent() || App.showDurationOfNew.isPresent())
                println("Logged in as: " + currentUser.getDisplayName());
            user = repo.persist(currentUser);
            performBackup();
            performCloning(account);
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
            final ZonedDateTime newestSavedTrackAddedAt = repo.getNewestSavedTrack(user).isPresent() ?
                    repo.getNewestSavedTrack(user).orElseThrow().getDateAdded() :
                    ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
            var pageItems = getFromApiPaged(2, "Saving all Liked Songs", api::getLikedSongs);
            for (var items : pageItems) newTracks.addAll(repo.persist(items, user));
            var newTrackIds = newTracks.stream().map(t -> t.getTrack().getSpotifyID().getId()).collect(
                    Collectors.toList());
            newTrackIds.removeAll(oldTrackIds);
            if (!newTrackIds.isEmpty()) {
                App.showDurationOfNew.ifPresentOrElse(() -> {
                    var onlyNewTracks = repo.getSavedTracksAfter(user, newestSavedTrackAddedAt);
                    var durationMs = onlyNewTracks.stream().map(s -> s.getTrack().getDurationMs().longValue())
                            .reduce(0L, Long::sum);
                    println(4, "Added " + newTrackIds.size() + " track(s) to Liked songs, duration: "
                            + msToPrettyString(durationMs));
                }, () -> verbosePrintln(4, "Added " + newTrackIds.size() + " track(s) to Liked songs"));
            }
            markRemovedTracks(newTracks);
        }

        private void saveFollowedPlaylists() {
            var oldPlaylistIds = repo.getFollowedPlaylistIds(user);
            List<SpotifyPlaylist> newPlaylists = new ArrayList<>();
            var pageItems = getFromApiPaged(2, "Saving " + App.playlistSaveRestriction.getValue().message
                    + " playlists", api::getCurrentUserPlaylists);
            for (var items : pageItems) {
                newPlaylists.addAll(repo.persist(switch (App.playlistSaveRestriction.getValue()) {
                    case ALL -> items;
                    case ALL_BUT_SPOTIFY -> Arrays.stream(items)
                            .filter(p -> !p.getOwner().getId().equals(SPOTIFY_USER_ID))
                            .toArray(PlaylistSimplified[]::new);
                    case ONLY_USER -> Arrays.stream(items)
                            .filter(p -> p.getOwner().getId().equals(user.getSpotifyUserID()))
                            .toArray(PlaylistSimplified[]::new);
                }));
            }
            var newPlaylistIds = newPlaylists.stream().map(p -> p.getSpotifyID().getId()).collect(Collectors.toList());
            newPlaylistIds.removeAll(oldPlaylistIds);
            if (!newPlaylistIds.isEmpty())
                verbosePrintln(4, "Following " + newPlaylistIds.size() + " new playlist(s)");
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
                verbosePrintln(4, "Following " + newArtistIds.size() + " new artist(s)");
            repo.followArtists(newArtists, user);
            markUnfollowedArtists(newArtists);
        }

        private void saveLikedAlbums() {
            var oldAlbumIds = repo.getSavedAlbumIds(user);
            List<SpotifySavedAlbum> newAlbums = new ArrayList<>();
            var pageItems = getFromApiPaged(2, "Saving all liked albums", api::getCurrentUserSavedAlbums);
            for (var items : pageItems)
                newAlbums.addAll(repo.persist(items, user, App.imageSaveRestriction.getValue()));
            var newAlbumIds = newAlbums.stream().map(a -> a.getAlbum().getSpotifyID().getId())
                    .collect(Collectors.toList());
            newAlbumIds.removeAll(oldAlbumIds);
            if (!newAlbumIds.isEmpty())
                verbosePrintln(4, "Added " + newAlbumIds.size() + " album(s) to liked");
            markUnlikedAlbums(newAlbums);
        }

        private <A extends AbstractModelObject> List<A[]>
        getFromApiPaged(int spaces, String message, BiFunction<Integer, Integer, Paging<A>> getPage) {
            verbosePrint(spaces, message);
            final int limit = 50;
            int offset = 0;
            Paging<A> apiPage;
            List<A[]> apiItems = new ArrayList<>();
            verbosePrint(", requesting data");
            do {
                verbosePrint(".");
                apiPage = getPage.apply(limit, offset);
                apiItems.add(apiPage.getItems());
                offset += limit;
            } while (apiPage.getNext() != null);
            verbosePrintln("");
            return apiItems;
        }

        private <A extends AbstractModelObject> List<A>
        getListFromApiPaged(int spaces, String message, BiFunction<Integer, Integer, Paging<A>> getPage) {
            verbosePrint(spaces, message);
            final int limit = 50;
            int offset = 0;
            Paging<A> apiPage;
            List<A[]> apiItems = new ArrayList<>();
            verbosePrint(", requesting data");
            do {
                verbosePrint(".");
                apiPage = getPage.apply(limit, offset);
                apiItems.add(apiPage.getItems());
                offset += limit;
            } while (apiPage.getNext() != null);
            verbosePrintln("");
            List<A> returnList = new ArrayList<>();
            apiItems.stream().forEach(page -> returnList.addAll(Arrays.stream(page).toList()));
            return returnList;
        }

        private <A extends AbstractModelObject> List<A[]>
        getFromApiPagedCursor(int spaces, String message, BiFunction<Integer, String, PagingCursorbased<A>> getPage) {
            verbosePrint(spaces, message);
            final int limit = 50;
            String after = null;
            PagingCursorbased<A> apiPage;
            List<A[]> apiItems = new ArrayList<>();
            verbosePrint(", requesting data");
            do {
                verbosePrint(".");
                apiPage = getPage.apply(limit, after);
                apiItems.add(apiPage.getItems());
                after = apiPage.getCursors()[0].getAfter();
            } while (apiPage.getNext() != null);
            verbosePrintln("");
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
                verbosePrintln(4, "Removed " + removed.size() + " track(s) from Liked Songs");
            }
        }

        private void markUnfollowedPlaylists(final List<SpotifyPlaylist> newPlaylists) {
            var newPlaylistIds = newPlaylists.stream().map(SpotifyPlaylist::getId).collect(Collectors.toSet());
            var allPlaylists = repo.getFollowedPlaylists(user);
            var removed = allPlaylists.stream().filter(p -> !newPlaylistIds.contains(p.getId())).toList();
            if (!removed.isEmpty()) {
                repo.unfollowPlaylists(removed, user);
                verbosePrintln(4, "Unfollowed " + removed.size() + " playlist(s)");
            }
        }

        private void markUnfollowedArtists(final List<SpotifyArtist> newFollowedArtists) {
            var newArtisIds = newFollowedArtists.stream().map(SpotifyArtist::getId).collect(Collectors.toSet());
            var allArtists = repo.getFollowedArtists(user);
            var removed = allArtists.stream().filter(a -> !newArtisIds.contains(a.getId())).toList();
            if (!removed.isEmpty()) {
                repo.unfollowArtists(removed, user);
                verbosePrintln(4, "Unfollowed " + removed.size() + " artists(s)");
            }
        }

        private void markUnlikedAlbums(final List<SpotifySavedAlbum> newLikedAlbums) {
            var newSavedAlbumIds = newLikedAlbums.stream().map(SpotifySavedAlbum::getId).collect(Collectors.toSet());
            var allSavedAlbums = repo.getSavedAlbums(user);
            var removed = allSavedAlbums.stream().filter(p -> !newSavedAlbumIds.contains(p.getId())).toList();
            if (!removed.isEmpty()) {
                for (var album : removed) repo.removeSavedAlbum(album.getAlbum(), user);
                verbosePrintln(4, "Removed " + removed.size() + " album(s) from Saved Albums");
            }
        }

        private List<String> combineIds(final List<String> separateIds, final int limit) {
            List<String> combined = new ArrayList<>();
            for (int i = 0; i <= separateIds.size() / limit; i++) {
                combined.add(String.join(",", separateIds.subList(i * limit,
                        Math.min(i * limit + limit, separateIds.size()))));
            }
            return combined;
        }

        private void saveDetailedInfo() {
            verbosePrintln(2, "Requesting detailed information for simplified objects");
            saveDetailedPlaylistInfo();
            saveDetailedAlbumInfo();
            saveDetailedArtistInfo();
            saveDetailedTrackInfo();
        }

        private void saveDetailedPlaylistInfo() {
            final var playlists = repo.findAllPlaylists();
            if (playlists.isEmpty()) return;
            if (playlists.stream().anyMatch(SpotifyPlaylist::getIsSimplified)) {
                verbosePrintln(4, playlists.stream().filter(SpotifyPlaylist::getIsSimplified).count() +
                        " new playlist(s)");
            }
            for (var playlist : playlists) {
                Optional<Playlist> apiPlaylist = api.getPlaylistWithoutTracks(playlist.getSpotifyID());
                if (apiPlaylist.isEmpty())
                    println(6, "Couldn't request detailed information for playlist " +
                            playlist.getName());
                else if (playlist.getIsSimplified()) {
                    savePlaylistTracks(playlist, apiPlaylist.get());
                } else if (!apiPlaylist.get().getSnapshotId().equals(playlist.getSnapshotId())) {
                    switch (App.playlistSaveRestriction.getValue()) {
                        case ALL -> savePlaylistTracks(playlist, apiPlaylist.get());
                        case ALL_BUT_SPOTIFY -> {
                            if (!apiPlaylist.get().getOwner().getId().equals(SPOTIFY_USER_ID))
                                savePlaylistTracks(playlist, apiPlaylist.get());
                        }
                        case ONLY_USER -> {
                            if (apiPlaylist.get().getOwner().getId().equals(user.getSpotifyUserID()))
                                savePlaylistTracks(playlist, apiPlaylist.get());
                        }
                    }
                }
            }
        }

        private void savePlaylistTracks(SpotifyPlaylist playlist, Playlist apiPlaylist) {
            List<PlaylistTrack> apiTracks = new ArrayList<>();
            getFromApiPaged(6, "Requesting tracks for " + playlist.getName(),
                    (l, o) -> api.getPlaylistTracks(l, o, playlist.getSpotifyID()))
                    .forEach(a -> apiTracks.addAll(Arrays.asList(a)));
            if (apiTracks.size() == apiPlaylist.getTracks().getTotal()) {
                verbosePrintln(8, "Saving " + apiTracks.size() + " track(s) for " +
                        playlist.getName());
                repo.deletePlaylistItems(playlist);
                repo.persist(apiTracks, playlist);
                if (playlist.getIsSimplified()) repo.persist(apiPlaylist);
                else repo.update(apiPlaylist);
            } else {
                println(6, "Size mismatch between requested track amount and the " +
                        "amount that there should be for playlist " + playlist.getName());
            }
        }

        private void saveDetailedAlbumInfo() {
            final var simpleAlbumIds = repo.getSimplifiedAlbumsSpotifyIDs();
            if (simpleAlbumIds.isEmpty()) return;
            verbosePrint(4, "Requesting data for " + simpleAlbumIds.size() + " album(s)");
            for (var ids : combineIds(simpleAlbumIds, 20)) {
                verbosePrint(".");
                repo.persistWithoutTracks(api.getSeveralAlbums(ids), App.imageSaveRestriction.getValue());
            }
            verbosePrintln("");
        }

        private void saveDetailedArtistInfo() {
            final var simpleArtistIds = repo.getSimplifiedArtistsSpotifyIDs();
            if (simpleArtistIds.isEmpty()) return;
            verbosePrint(4, "Requesting data for " + simpleArtistIds.size() + " artist(s)");
            for (var ids : combineIds(simpleArtistIds, 50)) {
                verbosePrint(".");
                repo.persist(api.getSeveralArtists(ids), App.imageSaveRestriction.getValue());
            }
            verbosePrintln("");
        }

        private void saveDetailedTrackInfo() {
            final var simpleTrackIds = repo.getSimplifiedTracksSpotifyIDs();
            if (simpleTrackIds.isEmpty()) return;
            verbosePrint(4, "Requesting data for " + simpleTrackIds.size() + " track(s)");
            for (var ids : combineIds(simpleTrackIds, 50)) {
                verbosePrint(".");
                repo.persist(api.getSeveralTracks(ids));
            }
            verbosePrintln("");
        }

        private void performCloning(final Config.UserInfo account) throws InterruptedException, IOException {
            if (account.hasCloningTargets()) {
                verbosePrintln(2, user.getDisplayName().orElse(user.getSpotifyUserID()) + " has "
                        + account.getCloneTargets().size() + " cloning target(s).");
                for (var targetInfo : account.getCloneTargets()) {
                    final var target = new ApiWrapper(targetInfo, App.getConfig());
                    final var targetUser = target.getCurrentUser().orElseThrow();
                    verbosePrintln(4, "Cloning to: " + targetUser.getDisplayName());
                    final var targetsPlaylists = getListFromApiPaged(6, "Retrieving targets' playlists.",
                            target::getCurrentUserPlaylists);
                    cloneLikedSongsToPlaylist(target, targetUser, targetsPlaylists.stream().toList());
                    // cloneLikedSongs
                    // cloneFollowedPlaylists
                    // clonePlaylists
                    // cloneLikedAlbums
                    // cloneFollowedArtists
                }
            }
        }

        void cloneLikedSongsToPlaylist(final ApiWrapper target, final User targetUser, final List<PlaylistSimplified> playlists) {
            final String playlistName = user.getDisplayName().orElse(user.getSpotifyUserID()) + " Liked Songs";
            final var possibleTargetPlaylists = playlists.stream()
                                                         .filter(ps -> ps.getOwner().getId().equals(targetUser.getId()))
                                                         .filter(ps -> ps.getName().equals(playlistName)).toList();
            final String likedSongsPlaylistId = switch (possibleTargetPlaylists.size()) {
                case 1 -> possibleTargetPlaylists.getFirst().getId();
                case 0 -> {
                    // create new playlist here and yield its id
                    yield newPlaylistId;
                }
                default -> throw new RuntimeException(
                        "Multiple targets for cloning Liked Songs to a playlist not handled yet.");
            }
        }
    }
}
