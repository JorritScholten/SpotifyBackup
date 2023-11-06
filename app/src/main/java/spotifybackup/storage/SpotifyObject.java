package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import se.michaelthelin.spotify.enums.ReleaseDatePrecision;
import spotifybackup.storage.exception.TransactionInactiveException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract sealed class SpotifyObject
        permits SpotifyGenre, SpotifyImage, SpotifyID, SpotifyArtist, SpotifyAlbum, SpotifyTrack, SpotifyPlaylist
        , SpotifyPlaylistItem, SpotifyUser {

    private static final Map<Class<? extends SpotifyObject>, SubTypes> mapSubtypeByClass = new HashMap<>();

    /** Helper method to get enum by class type. */
    static Function<Class<? extends SpotifyObject>, SubTypes> accessSubTypeByClass = t -> {
        if (t.equals(SpotifyObject.class)) throw new IllegalArgumentException("SpotifyObject.class not a valid value.");
        else return mapSubtypeByClass.get(t);
    };

    /** Checks entityManager to ensure transaction is active, throws exception if it isn't. */
    static Consumer<EntityManager> ensureTransactionActive = (EntityManager entityManager) -> {
        if (!entityManager.getTransaction().isActive()) throw new TransactionInactiveException();
    };

    static {
        for (var subType : SubTypes.values()) {
            mapSubtypeByClass.put(subType.type, subType);
        }
    }

    static LocalDate convertDate(String date, ReleaseDatePrecision precision) {
        return LocalDate.parse(switch (precision) {
            case DAY -> date;
            case MONTH -> date + "01";
            case YEAR -> date + "01-01";
        }, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * @param markets Array of CountryCode objects.
     * @return Array of Strings containing ISO 3166-1 alpha-2 market codes.
     */
    static String[] convertMarkets(@NonNull CountryCode[] markets) {
        Set<String> stringifiedMarkets = new HashSet<>();
        for (var market : markets) {
            stringifiedMarkets.add(market.getAlpha2());
        }
        return stringifiedMarkets.toArray(String[]::new);
    }

    public enum SubTypes {
        GENRE(SpotifyGenre.class),
        IMAGE(SpotifyImage.class),
        ID(SpotifyID.class),
        ARTIST(SpotifyArtist.class),
        ALBUM(SpotifyAlbum.class),
        TRACK(SpotifyTrack.class),
        PLAYLIST(SpotifyPlaylist.class),
        PLAYLIST_ITEM(SpotifyPlaylistItem.class),
        USER(SpotifyUser.class);

        final String name;
        final Class<? extends SpotifyObject> type;

        SubTypes(Class<? extends SpotifyObject> type) {
            this.type = type;
            name = type.getSimpleName();
        }
    }
}