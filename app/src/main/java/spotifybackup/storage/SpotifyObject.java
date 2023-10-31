package spotifybackup.storage;

import se.michaelthelin.spotify.enums.ReleaseDatePrecision;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public sealed abstract class SpotifyObject permits SpotifyGenre, SpotifyImage, SpotifyID, SpotifyArtist, SpotifyAlbum, SpotifyTrack {
    static LocalDate convertDate(String date, ReleaseDatePrecision precision) {
        return LocalDate.parse(switch (precision) {
            case DAY -> date;
            case MONTH -> date + "01";
            case YEAR -> date + "01-01";
        }, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}