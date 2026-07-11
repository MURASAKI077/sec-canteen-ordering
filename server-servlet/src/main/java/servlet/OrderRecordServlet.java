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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String account = params.getOrDefault("account", "").trim();

            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT d.dishName, d.dishWindow, d.dishRoom, d.price " +
                                 "FROM orders o " +
                                 "JOIN account a ON o.userId = a.userId " +
                                 "JOIN dishes d ON o.dishId = d.dishId " +
                                 "WHERE a.userAccount = ? " +
                                 "ORDER BY d.dishId")) {
                statement.setString(1, account);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        HashMap<String, String> item = new HashMap<>();
                        item.put("name", resultSet.getString("dishName"));
                        item.put("window", resultSet.getString("dishWindow"));
                        item.put("room", resultSet.getString("dishRoom"));
                        item.put("price", String.valueOf(resultSet.getFloat("price")));
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
