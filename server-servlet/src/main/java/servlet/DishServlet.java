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

@WebServlet("/DishServlet")
public class DishServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDishList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDishList(request, response);
    }

    private void handleDishList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String keyword = params.getOrDefault("keyword", "").trim();
            if (keyword.length() > 50) {
                commonResponse.setResult("1", "搜索关键词过长");
            } else {
                queryDishes(commonResponse, keyword);
                commonResponse.setResult("0", "success");
                commonResponse.addProperty("keyword", keyword);
            }
        } catch (Exception e) {
            commonResponse.setResult("1", "获取菜单失败");
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    private void queryDishes(CommonResponse response, String keyword) throws Exception {
        boolean hasKeyword = !keyword.isEmpty();
        String sql = "SELECT dishName, dishWindow, dishRoom, price FROM dishes";
        if (hasKeyword) {
            sql += " WHERE dishName LIKE ? OR dishWindow LIKE ? OR dishRoom LIKE ?";
        }
        sql += " ORDER BY dishId";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (hasKeyword) {
                String pattern = "%" + keyword + "%";
                statement.setString(1, pattern);
                statement.setString(2, pattern);
                statement.setString(3, pattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    HashMap<String, String> item = new HashMap<>();
                    item.put("name", resultSet.getString("dishName"));
                    item.put("window", resultSet.getString("dishWindow"));
                    item.put("room", resultSet.getString("dishRoom"));
                    item.put("price", String.valueOf(resultSet.getFloat("price")));
                    response.addListItem(item);
                }
            }
        }
    }
}
