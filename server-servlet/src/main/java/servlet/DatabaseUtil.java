package servlet;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    public static final String TABLE_ACCOUNT = "account";
    public static final String TABLE_DISHES = "dishes";
    public static final String TABLE_ORDER = "orders";

    private static final String CONFIG_FILE = "db.properties";
    private static HikariDataSource dataSource;

    static {
        initializeDataSource();
    }

    private DatabaseUtil() {
    }

    private static void initializeDataSource() {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("SecCanteenPool");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(requiredProperty(properties, "db.url"));
        config.setUsername(requiredProperty(properties, "db.user"));
        config.setPassword(properties.getProperty("db.password", ""));
        config.setMaximumPoolSize(intProperty(properties, "db.maximumPoolSize", 10));
        config.setMinimumIdle(intProperty(properties, "db.minimumIdle", 2));
        config.setConnectionTimeout(longProperty(properties, "db.connectionTimeoutMs", 10000L));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static int update(String sql, Object... params) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillParams(statement, params);
            return statement.executeUpdate();
        }
    }

    private static void fillParams(PreparedStatement statement, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static String requiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ExceptionInInitializerError(key + " is missing in " + CONFIG_FILE);
        }
        return value.trim();
    }

    private static int intProperty(Properties properties, String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)).trim());
    }

    private static long longProperty(Properties properties, String key, long defaultValue) {
        return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)).trim());
    }
}
