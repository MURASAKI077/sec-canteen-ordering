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

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
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
                ResponseUtil.writeText(response, "注册失败：账号或密码为空");
                return;
            }

            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement checkStatement = connection.prepareStatement(
                         "SELECT userId FROM account WHERE userAccount = ?")) {
                checkStatement.setString(1, account);
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        ResponseUtil.writeText(response, "注册失败: 用户已存在");
                        return;
                    }
                }
            }

            int rows = DatabaseUtil.update(
                    "INSERT INTO account (userAccount, userPassword) VALUES (?, ?)",
                    account,
                    password
            );
            ResponseUtil.writeText(response, rows > 0 ? "注册成功" : "注册失败");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ResponseUtil.writeText(response, "注册失败: " + e.getMessage());
        }
    }
}
