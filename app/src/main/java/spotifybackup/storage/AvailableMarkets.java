package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class AvailableMarkets {
    final HashSet<CountryCode> codes = new HashSet<>();

    public AvailableMarkets() {}

    public AvailableMarkets(Set<CountryCode> codes) {
        if (codes != null) this.codes.addAll(Set.copyOf(codes));
    }

    public AvailableMarkets(CountryCode[] codes) {
        if (codes != null) this.codes.addAll(Set.copyOf(Arrays.stream(codes).toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvailableMarkets that = (AvailableMarkets) o;
        if (Objects.equals(codes, that.codes)) return true;
        return codes.size() == that.codes.size() && codes.containsAll(that.codes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codes);
    }
}
