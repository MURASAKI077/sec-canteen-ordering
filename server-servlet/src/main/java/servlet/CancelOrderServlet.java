package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

@WebServlet("/CancelOrderServlet")
public class CancelOrderServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String account = params.getOrDefault("account", "").trim();
            long orderId = Long.parseLong(params.getOrDefault("orderId", "").trim());

            if (account.isEmpty()) {
                commonResponse.setResult("1", "account is required");
            } else {
                int rows = cancelOrder(account, orderId);
                if (rows == 1) {
                    commonResponse.setResult("0", "success");
                    commonResponse.addProperty("orderId", String.valueOf(orderId));
                    commonResponse.addProperty("status", "CANCELLED");
                } else {
                    commonResponse.setResult("1", "order does not exist or cannot be cancelled");
                }
            }
        } catch (NumberFormatException e) {
            commonResponse.setResult("1", "orderId format is invalid");
        } catch (Exception e) {
            commonResponse.setResult("1", e.getMessage());
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    private int cancelOrder(String account, long orderId) throws Exception {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE orders o " +
                             "JOIN account a ON o.userId = a.userId " +
                             "SET o.status = 'CANCELLED' " +
                             "WHERE o.orderId = ? AND a.userAccount = ? AND o.status = 'PLACED'")) {
            statement.setLong(1, orderId);
            statement.setString(2, account);
            return statement.executeUpdate();
        }
    }
}
