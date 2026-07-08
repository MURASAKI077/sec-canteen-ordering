package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    public static final String TABLE_ACCOUNT = "account";
    public static final String TABLE_DISHES = "dishes";
    public static final String TABLE_ORDER = "orders";

    private static final String CONFIG_FILE = "db.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DatabaseUtil() {
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        url = properties.getProperty("db.url");
        user = properties.getProperty("db.user");
        password = properties.getProperty("db.password");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static ResultSet query(String sql, Object... params) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        fillParams(statement, params);
        return statement.executeQuery();
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
}
