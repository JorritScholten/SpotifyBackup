package spotifybackup.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.Type;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class AvailableMarketsBitset {
    @Id
    long ordinal;

    @NonNull
    @Column(unique = true, nullable = false, length = 2)
    private String alpha2;

    @Column(unique = false, nullable = true, length = 3)
    private String alpha3;

    @Column(unique = false, nullable = true)
    private Integer numeric;

    @NonNull
    @Column(nullable = false)
    private String country;

    @NonNull
    @Column(unique = true, nullable = false)
    @Type(AvailableMarketsType.class)
    private AvailableMarkets bitset;
}
