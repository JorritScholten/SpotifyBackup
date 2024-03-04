package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.hibernate.query.criteria.CriteriaDefinition;
import se.michaelthelin.spotify.enums.ReleaseDatePrecision;
import spotifybackup.storage.exception.TransactionInactiveException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract sealed class SpotifyObject
        permits SpotifyAlbum, SpotifyArtist, SpotifyGenre, SpotifyID, SpotifyImage, SpotifyPlaylist,
        SpotifyPlaylistItem, SpotifySavedTrack, SpotifyTrack, SpotifyUser {

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

    static <T extends SpotifyObject> Optional<T> getSingleResultOptionally(EntityManager em, CriteriaDefinition<T> query) {
        try {
            return Optional.of(em.createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    static LocalDate convertDate(String date, ReleaseDatePrecision precision) {
        return LocalDate.parse(switch (precision) {
            case DAY -> date;
            case MONTH -> date + "-01";
            case YEAR -> date + "-01-01";
        }, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    static AvailableMarkets convertMarkets(CountryCode[] markets) {
        if (markets == null) return new AvailableMarkets();
        return new AvailableMarkets(Arrays.stream(markets).toList());
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
        USER(SpotifyUser.class),
        SAVED_TRACK(SpotifySavedTrack.class);

        final String name;
        final Class<? extends SpotifyObject> type;

        SubTypes(Class<? extends SpotifyObject> type) {
            this.type = type;
            name = type.getSimpleName();
        }
    }
}