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
                    createOrder(connection, userId, dishId);
                    commonResponse.setResult("0", "success");
                    commonResponse.addProperty("userId", String.valueOf(userId));
                    commonResponse.addProperty("dishId", String.valueOf(dishId));
                }
            }
        } catch (NumberFormatException e) {
            commonResponse.setResult("1", "price format is invalid");
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

    private void createOrder(Connection connection, int userId, int dishId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO orders (userId, dishId) VALUES (?, ?)")) {
            statement.setInt(1, userId);
            statement.setInt(2, dishId);
            statement.executeUpdate();
        }
    }
}
