package spotifybackup.storage;

import jakarta.persistence.*;

@Entity
@Table(name = "genre")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
}
