package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;

@Builder
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Getter
public class SpotifyImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private URI url;

    @Column(nullable = false)
    private int width, height;
}
