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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String account = params.getOrDefault("account", "").trim();
            String password = params.getOrDefault("password", "").trim();

            if (account.isEmpty() || password.isEmpty()) {
                ResponseUtil.writeText(response, "登录失败: 账号或密码为空");
                return;
            }

            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT userId FROM account WHERE userAccount = ? AND userPassword = ?")) {
                statement.setString(1, account);
                statement.setString(2, password);
                try (ResultSet resultSet = statement.executeQuery()) {
                    ResponseUtil.writeText(response, resultSet.next() ? "登录成功" : "登录失败");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ResponseUtil.writeText(response, "登录失败: " + e.getMessage());
        }
    }
}
