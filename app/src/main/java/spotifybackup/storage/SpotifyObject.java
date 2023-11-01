package spotifybackup.storage;

import jakarta.persistence.EntityManager;
import se.michaelthelin.spotify.enums.ReleaseDatePrecision;
import spotifybackup.storage.exception.TransactionInactiveException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public abstract sealed class SpotifyObject permits SpotifyGenre, SpotifyImage, SpotifyID, SpotifyArtist, SpotifyAlbum, SpotifyTrack {
    static Consumer<EntityManager> ensureTransactionActive = entityManager -> {
        if (!entityManager.getTransaction().isActive()) throw new TransactionInactiveException();
    };

    static LocalDate convertDate(String date, ReleaseDatePrecision precision) {
        return LocalDate.parse(switch (precision) {
            case DAY -> date;
            case MONTH -> date + "01";
            case YEAR -> date + "01-01";
        }, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}