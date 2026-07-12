package servlet;

import java.nio.charset.StandardCharsets;

public final class ValidationUtil {
    private ValidationUtil() {
    }

    public static String validateRegistration(String account, String password) {
        if (account == null || account.length() < 3 || account.length() > 32) {
            return "账号长度必须为3到32个字符";
        }
        if (!account.matches("[\\p{L}\\p{N}_]+")) {
            return "账号只能包含文字、数字和下划线";
        }
        if (password == null || password.length() < 6 || password.length() > 64) {
            return "密码长度必须为6到64个字符";
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return "密码内容过长";
        }
        return null;
    }
}
