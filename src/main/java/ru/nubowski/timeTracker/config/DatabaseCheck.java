package ru.nubowski.timeTracker.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * A component responsible for checking the connected database at startup.
 * The check includes getting and logging the database product name.
 * A warning message is logged if the connected database is not PostgreSQL.
 */
@Component
public class DatabaseCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCheck.class);
    private final DataSource dataSource;

    /**
     * Constructs a new DatabaseCheck instance.
     *
     * @param databaseSource the data source from which to obtain connections
     */
    @Autowired
    public DatabaseCheck (DataSource databaseSource) {
        this.dataSource = databaseSource;
    }

    /**
     * Checks the connected database at startup.
     * Logs the database product name and a warning if the database is not PostgreSQL.
     */
    @PostConstruct
    public void checkDatabase() {
        LOGGER.info("Requesting the meta data of the connected Database");
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            LOGGER.info("The Database is: " + databaseProductName);
            if (!databaseProductName.equalsIgnoreCase("PostgreSQL")) {
                // LOGGER or EX
                LOGGER.info("WARNING: The app is expected to work with PostgreSQL, but it's currently connected to: " + databaseProductName);
            }
        } catch (SQLException e) {
            LOGGER.info("stack trace or custom exception here");
            e.printStackTrace();
        }
    }

}
