package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hibernate.engine.jdbc.BlobProxy;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = true)
public class AvailableMarketsConverter implements AttributeConverter<AvailableMarkets, Blob> {
    private static final List<CountryCode> COUNTRY_CODES = List.of(CountryCode.values());

    @Override
    public Blob convertToDatabaseColumn(AvailableMarkets availableMarkets) {
        byte[] out = new byte[(COUNTRY_CODES.size() / 8) + 1];
        int i = 0;
        for (var code : COUNTRY_CODES) {
            if (availableMarkets.codes.contains(code)) {
                final int offset = i / 8;
                final int position = i % 8;
                out[offset] |= (byte) (0x1 << position);
            }
            i++;
        }
        return BlobProxy.generateProxy(out);
    }

    @Override
    public AvailableMarkets convertToEntityAttribute(Blob blob) {
        try {
            List<CountryCode> out = new ArrayList<>();
            final byte[] in = blob.getBytes(1, (COUNTRY_CODES.size() / 8) + 1);
            int i = 0;
            for (var code : COUNTRY_CODES) {
                final int offset = i / 8;
                final int position = i % 8;
                if (((in[offset] & 0xFF) & (byte) (0x1 << position)) != 0) {
                    out.add(code);
                }
                i++;
            }
            return new AvailableMarkets(out);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
