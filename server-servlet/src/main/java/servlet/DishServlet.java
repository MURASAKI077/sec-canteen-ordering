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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(response);
    }

    private void handle(HttpServletResponse response) throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT dishName, dishWindow, dishRoom, price FROM dishes ORDER BY dishId");
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
            commonResponse.setResult("1", e.getMessage());
        }
        ResponseUtil.writeJson(response, commonResponse);
    }
}
