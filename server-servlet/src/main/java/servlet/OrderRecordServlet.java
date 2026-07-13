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
import java.util.HashMap;
import java.util.Map;

@WebServlet("/OrderRecordServlet")
public class OrderRecordServlet extends HttpServlet {
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

            if (account.isEmpty()) {
                commonResponse.setResult("1", "account is required");
                ResponseUtil.writeJson(response, commonResponse);
                return;
            }

            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT o.orderId, o.quantity, o.status, o.orderTime, " +
                                 "d.dishName, d.dishWindow, d.dishRoom, d.price, " +
                                 "r.rating, r.content AS reviewContent " +
                                 "FROM orders o " +
                                 "JOIN account a ON o.userId = a.userId " +
                                 "JOIN dishes d ON o.dishId = d.dishId " +
                                 "LEFT JOIN reviews r ON o.orderId = r.orderId " +
                                 "WHERE a.userAccount = ? " +
                                 "ORDER BY o.orderTime DESC, o.orderId DESC")) {
                statement.setString(1, account);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        HashMap<String, String> item = new HashMap<>();
                        item.put("orderId", String.valueOf(resultSet.getLong("orderId")));
                        item.put("name", resultSet.getString("dishName"));
                        item.put("window", resultSet.getString("dishWindow"));
                        item.put("room", resultSet.getString("dishRoom"));
                        item.put("price", String.valueOf(resultSet.getFloat("price")));
                        item.put("quantity", String.valueOf(resultSet.getInt("quantity")));
                        item.put("status", resultSet.getString("status"));
                        item.put("orderTime", resultSet.getTimestamp("orderTime").toString());
                        int rating = resultSet.getInt("rating");
                        boolean reviewed = !resultSet.wasNull();
                        item.put("reviewed", reviewed ? "1" : "0");
                        item.put("rating", reviewed ? String.valueOf(rating) : "");
                        item.put("reviewContent", reviewed ? resultSet.getString("reviewContent") : "");
                        commonResponse.addListItem(item);
                    }
                }
            }
            commonResponse.setResult("0", "success");
        } catch (Exception e) {
            commonResponse.setResult("1", e.getMessage());
        }
        ResponseUtil.writeJson(response, commonResponse);
    }
}
