package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Map;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setResult("1", "请使用POST请求注册");
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
            String validationError = ValidationUtil.validateRegistration(account, password);

            if (validationError != null) {
                commonResponse.setResult("1", validationError);
            } else {
                createAccount(commonResponse, account, password);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            commonResponse.setResult("1", "账号已存在");
        } catch (Exception e) {
            commonResponse.setResult("1", "注册服务暂时不可用");
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    private void createAccount(CommonResponse response, String account, String password) throws Exception {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO account (userAccount, userPassword) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, account);
            statement.setString(2, PasswordUtil.hash(password));
            statement.executeUpdate();
            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new IllegalStateException("user ID was not generated");
                }
                response.setResult("0", "注册成功");
                response.addProperty("userId", String.valueOf(generatedKeys.getInt(1)));
                response.addProperty("account", account);
            }
        }
    }
}
