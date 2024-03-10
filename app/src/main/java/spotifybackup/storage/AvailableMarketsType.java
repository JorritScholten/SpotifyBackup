package spotifybackup.storage;

import com.neovisionaries.i18n.CountryCode;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

public class AvailableMarketsType implements UserType<AvailableMarkets> {
    private static final List<CountryCode> COUNTRY_CODES = List.of(CountryCode.values());
    private static final int SQL_ROW_LENGTH = (COUNTRY_CODES.size() / 8) + 1;

    @Override
    public int getSqlType() {
        return SqlTypes.VARBINARY;
    }

    @Override
    public long getDefaultSqlLength(Dialect dialect, JdbcType jdbcType) {
        return SQL_ROW_LENGTH;
    }

    @Override
    public Class<AvailableMarkets> returnedClass() {
        return AvailableMarkets.class;
    }

    @Override
    public boolean equals(AvailableMarkets x, AvailableMarkets y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(AvailableMarkets x) {
        return x.hashCode();
    }

    @Override
    public AvailableMarkets nullSafeGet(ResultSet rs, int index, SharedSessionContractImplementor ssci, Object o)
            throws SQLException {
        if(rs.getBytes(index) == null) return new AvailableMarkets();
        Set<CountryCode> out = new HashSet<>();
        final byte[] in = rs.getBytes(index);
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
    }

    @Override
    public void nullSafeSet(PreparedStatement ps, AvailableMarkets am, int index, SharedSessionContractImplementor ssci)
            throws SQLException {
        if (Objects.isNull(am)) ps.setNull(index, Types.VARBINARY);
        else {
            byte[] out = new byte[SQL_ROW_LENGTH];
            int i = 0;
            for (var code : COUNTRY_CODES) {
                if (am.codes.contains(code)) {
                    final int offset = i / 8;
                    final int position = i % 8;
                    out[offset] |= (byte) (0x1 << position);
                }
                i++;
            }
            ps.setBytes(index, out);
        }
    }

    @Override
    public AvailableMarkets deepCopy(AvailableMarkets availableMarkets) {
        return new AvailableMarkets(availableMarkets.codes);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(AvailableMarkets availableMarkets) {
        return (Serializable) deepCopy(availableMarkets);
    }

    @Override
    public AvailableMarkets assemble(Serializable serializable, Object o) {
        return deepCopy((AvailableMarkets) serializable);
    }
}
