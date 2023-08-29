package spotifybackup.storage;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.NonNull;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class SpotifyObjectRepository {
    private final EntityManagerFactory emf;

    private SpotifyObjectRepository(Properties DB_ACCESS) {
        emf = Persistence.createEntityManagerFactory(DB_ACCESS.getProperty("persistenceUnitName"), DB_ACCESS);
    }

    /**
     * Factory method to create SpotifyObjectRepository.
     * @param dbPath File path of database.
     */
    public static SpotifyObjectRepository factory(@NonNull File dbPath) {
        if (!(dbPath.isFile() && dbPath.exists() && dbPath.canRead() && dbPath.canWrite())) {
            throw new RuntimeException("Supplied filepath to database is unusable: " + dbPath);
        }
        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        final Properties DB_ACCESS = new Properties();
        DB_ACCESS.put("hibernate.hbm2ddl.auto", "validate");
        DB_ACCESS.put("hibernate.show_sql", "false");
        DB_ACCESS.put("persistenceUnitName", "SpotifyObjects");
        var dataSourceUrl = new StringBuilder("jdbc:h2:");
        dataSourceUrl.append(dbPath.getAbsolutePath());
        if (dbPath.getAbsolutePath().endsWith(".mv.db")) {
            dataSourceUrl.append(dbPath.getAbsolutePath(), 0, dbPath.getAbsolutePath().length() - 6);
        } else if (dbPath.getAbsolutePath().endsWith(".db")) {
            dataSourceUrl.append(dbPath.getAbsolutePath(), 0, dbPath.getAbsolutePath().length() - 3);
        } else {
            dataSourceUrl.append(dbPath.getAbsolutePath());
        }
        dataSourceUrl.append(";DB_CLOSE_DELAY=-1");
        DB_ACCESS.put("hibernate.hikari.dataSource.url", dataSourceUrl);
        return new SpotifyObjectRepository(DB_ACCESS);
    }

    public List<SpotifyGenre> findAllGenres() {
        try (var em = emf.createEntityManager()) {
            return SpotifyGenreRepository.findAll(em);
        }
    }
}
