package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@WebServlet("/ReviewServlet")
public class ReviewServlet extends HttpServlet {
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MAX_CONTENT_LENGTH = 200;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String account = params.getOrDefault("account", "").trim();
            long orderId = Long.parseLong(params.getOrDefault("orderId", "").trim());
            int rating = Integer.parseInt(params.getOrDefault("rating", "").trim());
            String content = params.getOrDefault("content", "").trim();

            if (account.isEmpty()) {
                commonResponse.setResult("1", "account is required");
            } else if (rating < MIN_RATING || rating > MAX_RATING) {
                commonResponse.setResult("1", "rating must be between 1 and 5");
            } else if (content.length() > MAX_CONTENT_LENGTH) {
                commonResponse.setResult("1", "review content is too long");
            } else {
                submitReview(commonResponse, account, orderId, rating, content);
            }
        } catch (NumberFormatException e) {
            commonResponse.setResult("1", "orderId or rating format is invalid");
        } catch (Exception e) {
            commonResponse.setResult("1", e.getMessage());
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    private void submitReview(CommonResponse commonResponse,
                              String account,
                              long orderId,
                              int rating,
                              String content) throws Exception {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String status = findOrderStatus(connection, account, orderId);
            if (status == null) {
                commonResponse.setResult("1", "order does not exist");
                return;
            }
            if ("CANCELLED".equals(status)) {
                commonResponse.setResult("1", "cancelled order cannot be reviewed");
                return;
            }

            upsertReview(connection, orderId, rating, content);
            commonResponse.setResult("0", "review saved");
            commonResponse.addProperty("orderId", String.valueOf(orderId));
            commonResponse.addProperty("reviewed", "1");
            commonResponse.addProperty("rating", String.valueOf(rating));
            commonResponse.addProperty("reviewContent", content);
        }
    }

    private String findOrderStatus(Connection connection, String account, long orderId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT o.status " +
                        "FROM orders o " +
                        "JOIN account a ON o.userId = a.userId " +
                        "WHERE o.orderId = ? AND a.userAccount = ?")) {
            statement.setLong(1, orderId);
            statement.setString(2, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("status") : null;
            }
        }
    }

    private void upsertReview(Connection connection, long orderId, int rating, String content) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO reviews (orderId, rating, content) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE rating = VALUES(rating), content = VALUES(content)")) {
            statement.setLong(1, orderId);
            statement.setInt(2, rating);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }
}
