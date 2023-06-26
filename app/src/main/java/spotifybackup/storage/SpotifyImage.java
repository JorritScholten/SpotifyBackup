package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URL;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SpotifyImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private URL uri;

    @Column(nullable = false)
    private int width, height;
}
