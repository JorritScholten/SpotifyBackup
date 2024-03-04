package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Getter
public class AvailableMarkets {
    List<CountryCode> codes = new ArrayList<>();

    public AvailableMarkets() {}

    public AvailableMarkets(List<CountryCode> codes) {
        this.codes = codes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvailableMarkets that = (AvailableMarkets) o;
        if (Objects.equals(codes, that.codes)) return true;
        return codes.size() == that.codes.size() && new HashSet<>(codes).containsAll(that.codes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codes);
    }
}
