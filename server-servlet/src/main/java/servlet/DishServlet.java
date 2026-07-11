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

@WebServlet("/DishServlet")
public class DishServlet extends HttpServlet {

    private static final String QUERY_DISHES_SQL =
            "SELECT dishName, dishWindow, dishRoom, price FROM dishes ORDER BY dishId";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDishList(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDishList(response);
    }

    // 查询全部菜品，并封装成客户端首页列表需要的字段
    private void handleDishList(HttpServletResponse response) throws IOException {
        CommonResponse commonResponse = new CommonResponse();

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(QUERY_DISHES_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                HashMap<String, String> item = new HashMap<>();
                item.put("name", resultSet.getString("dishName"));
                item.put("window", resultSet.getString("dishWindow"));
                item.put("room", resultSet.getString("dishRoom"));
                item.put("price", String.valueOf(resultSet.getFloat("price")));

                commonResponse.addListItem(item);
            }

            commonResponse.setResult("0", "success");
        } catch (Exception e) {
            commonResponse.setResult("1", "获取菜单失败");
        }

        ResponseUtil.writeJson(response, commonResponse);
    }
}
