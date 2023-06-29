package spotifybackup.storage;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
@RequiredArgsConstructor
@ToString
@Table(name = "genre")
@NamedQueries({
        @NamedQuery(name = "Genre.countBy", query = "select count(g) from Genre g")
})
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(nullable = false, unique = true)
    @NonNull
    private String name;
    @Lob
    private String description;
}
