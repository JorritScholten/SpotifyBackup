package spotifybackup.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Objects;

@AllArgsConstructor
@Entity
@Getter
@ToString
@NoArgsConstructor
public final class SpotifyID extends SpotifyObject {
    @Id
    @Column(columnDefinition = "VARCHAR")
    private String id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpotifyID spotifyID = (SpotifyID) o;
        return Objects.equals(id, spotifyID.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
