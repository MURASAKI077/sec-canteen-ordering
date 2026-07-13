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

@WebServlet("/DishReviewServlet")
public class DishReviewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            int dishId = Integer.parseInt(params.getOrDefault("dishId", "").trim());
            queryReviews(commonResponse, dishId);
            commonResponse.setResult("0", "success");
            commonResponse.addProperty("dishId", String.valueOf(dishId));
        } catch (NumberFormatException e) {
            commonResponse.setResult("1", "dishId format is invalid");
        } catch (Exception e) {
            commonResponse.setResult("1", "获取评价失败");
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    private void queryReviews(CommonResponse response, int dishId) throws Exception {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT r.rating, r.content, r.createTime, a.userAccount " +
                             "FROM reviews r " +
                             "JOIN orders o ON r.orderId = o.orderId " +
                             "JOIN account a ON o.userId = a.userId " +
                             "WHERE o.dishId = ? " +
                             "ORDER BY r.createTime DESC, r.reviewId DESC " +
                             "LIMIT 20")) {
            statement.setInt(1, dishId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    HashMap<String, String> item = new HashMap<>();
                    item.put("rating", String.valueOf(resultSet.getInt("rating")));
                    item.put("content", resultSet.getString("content"));
                    item.put("account", maskAccount(resultSet.getString("userAccount")));
                    item.put("createTime", resultSet.getTimestamp("createTime").toString());
                    response.addListItem(item);
                }
            }
        }
    }

    private String maskAccount(String account) {
        if (account == null || account.length() <= 2) {
            return "用户";
        }
        return account.charAt(0) + "***" + account.charAt(account.length() - 1);
    }
}
