package servlet;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class PasswordUtil {
    private static final int BCRYPT_ROUNDS = 10;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean verify(String password, String storedPassword) {
        if (password == null || storedPassword == null) {
            return false;
        }
        if (!isBcryptHash(storedPassword)) {
            return password.equals(storedPassword);
        }
        try {
            return BCrypt.checkpw(password, storedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static int migrateLegacyPasswords() throws Exception {
        List<AccountPassword> legacyPasswords = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT userId, userPassword FROM account");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String storedPassword = resultSet.getString("userPassword");
                if (!isBcryptHash(storedPassword)) {
                    legacyPasswords.add(new AccountPassword(
                            resultSet.getInt("userId"),
                            storedPassword
                    ));
                }
            }
        }

        if (legacyPasswords.isEmpty()) {
            return 0;
        }

        try (Connection connection = DatabaseUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE account SET userPassword = ? WHERE userId = ?")) {
                for (AccountPassword account : legacyPasswords) {
                    statement.setString(1, hash(account.password()));
                    statement.setInt(2, account.userId());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
        return legacyPasswords.size();
    }

    private static boolean isBcryptHash(String value) {
        return value != null && (value.startsWith("$2a$")
                || value.startsWith("$2b$")
                || value.startsWith("$2y$"));
    }

    private record AccountPassword(int userId, String password) {
    }
}
