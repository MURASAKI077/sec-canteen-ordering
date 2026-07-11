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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;

@WebServlet("/OrderServlet")
public class OrderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String account = params.getOrDefault("account", "").trim();
            String name = params.getOrDefault("name", "").trim();
            String window = params.getOrDefault("window", "").trim();
            String room = params.getOrDefault("room", "").trim();
            String price = params.getOrDefault("price", "").trim();
            int quantity = parseQuantity(params.getOrDefault("quantity", "1"));

            if (account.isEmpty()) {
                commonResponse.setResult("1", "account is required");
                ResponseUtil.writeJson(response, commonResponse);
                return;
            }
            if (name.isEmpty()) {
                commonResponse.setResult("1", "dish name is required");
                ResponseUtil.writeJson(response, commonResponse);
                return;
            }

            try (Connection connection = DatabaseUtil.getConnection()) {
                Integer userId = findUserId(connection, account);
                if (userId == null) {
                    commonResponse.setResult("1", "account does not exist");
                    ResponseUtil.writeJson(response, commonResponse);
                    return;
                }

                Integer dishId = findDishId(connection, name, window, room, price);
                if (dishId == null) {
                    commonResponse.setResult("1", "dish does not exist");
                } else {
                    createOrderInTransaction(connection, commonResponse, userId, dishId, quantity);
                }
            }
        } catch (NumberFormatException e) {
            commonResponse.setResult("1", e.getMessage());
        } catch (Exception e) {
            commonResponse.setResult("1", e.getMessage());
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    private Integer findUserId(Connection connection, String account) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT userId FROM account WHERE userAccount = ?")) {
            statement.setString(1, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("userId") : null;
            }
        }
    }

    private Integer findDishId(Connection connection,
                               String name,
                               String window,
                               String room,
                               String price) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT dishId FROM dishes WHERE dishName = ?");
        if (!window.isEmpty()) {
            sql.append(" AND dishWindow = ?");
        }
        if (!room.isEmpty()) {
            sql.append(" AND dishRoom = ?");
        }
        if (!price.isEmpty()) {
            sql.append(" AND price = ?");
        }
        sql.append(" LIMIT 1");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setString(index++, name);
            if (!window.isEmpty()) {
                statement.setString(index++, window);
            }
            if (!room.isEmpty()) {
                statement.setString(index++, room);
            }
            if (!price.isEmpty()) {
                statement.setFloat(index, Float.parseFloat(price));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("dishId") : null;
            }
        }
    }

    private void createOrderInTransaction(Connection connection,
                                          CommonResponse commonResponse,
                                          int userId,
                                          int dishId,
                                          int quantity) throws Exception {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            long orderId = createOrder(connection, userId, dishId, quantity);
            Timestamp orderTime = findOrderTime(connection, orderId);
            connection.commit();

            commonResponse.setResult("0", "success");
            commonResponse.addProperty("orderId", String.valueOf(orderId));
            commonResponse.addProperty("userId", String.valueOf(userId));
            commonResponse.addProperty("dishId", String.valueOf(dishId));
            commonResponse.addProperty("quantity", String.valueOf(quantity));
            commonResponse.addProperty("status", "PLACED");
            commonResponse.addProperty("orderTime", orderTime.toString());
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private long createOrder(Connection connection, int userId, int dishId, int quantity) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO orders (userId, dishId, quantity, status) VALUES (?, ?, ?, 'PLACED')",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setInt(2, dishId);
            statement.setInt(3, quantity);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("order ID was not generated");
                }
                return generatedKeys.getLong(1);
            }
        }
    }

    private Timestamp findOrderTime(Connection connection, long orderId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT orderTime FROM orders WHERE orderId = ?")) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("created order was not found");
                }
                return resultSet.getTimestamp("orderTime");
            }
        }
    }

    private int parseQuantity(String value) {
        try {
            int quantity = Integer.parseInt(value.trim());
            if (quantity < 1 || quantity > 99) {
                throw new NumberFormatException("quantity must be between 1 and 99");
            }
            return quantity;
        } catch (NumberFormatException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("quantity")) {
                throw e;
            }
            throw new NumberFormatException("quantity format is invalid");
        }
    }
}
