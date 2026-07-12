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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setResult("1", "请使用POST请求登录");
        ResponseUtil.writeJson(response, commonResponse);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String account = params.getOrDefault("account", "").trim();
            String password = params.getOrDefault("password", "");

            if (account.isEmpty() || password.isEmpty()) {
                commonResponse.setResult("1", "账号和密码不能为空");
            } else {
                authenticate(commonResponse, account, password);
            }
        } catch (Exception e) {
            commonResponse.setResult("1", "登录服务暂时不可用");
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    private void authenticate(CommonResponse response, String account, String password) throws Exception {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT userId, userPassword FROM account WHERE userAccount = ?")) {
            statement.setString(1, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()
                        || !PasswordUtil.verify(password, resultSet.getString("userPassword"))) {
                    response.setResult("1", "账号或密码错误");
                    return;
                }
                response.setResult("0", "登录成功");
                response.addProperty("userId", String.valueOf(resultSet.getInt("userId")));
                response.addProperty("account", account);
            }
        }
    }
}
