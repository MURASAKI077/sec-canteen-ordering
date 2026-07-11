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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String account = params.getOrDefault("account", "").trim();
            String name = params.getOrDefault("name", "").trim();

            try (Connection connection = DatabaseUtil.getConnection()) {
                Integer userId = findUserId(connection, account);
                Integer dishId = findDishId(connection, name);
                if (userId == null || dishId == null) {
                    commonResponse.setResult("1", "用户或菜品不存在");
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO orders (userId, dishId) VALUES (?, ?)")) {
                        statement.setInt(1, userId);
                        statement.setInt(2, dishId);
                        statement.executeUpdate();
                    }
                    commonResponse.setResult("0", "success");
                }
            }
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

    private Integer findDishId(Connection connection, String name) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT dishId FROM dishes WHERE dishName = ? LIMIT 1")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("dishId") : null;
            }
        }
    }
}
