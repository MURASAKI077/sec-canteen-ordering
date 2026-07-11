package servlet;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ResponseUtil {
    private static final Gson GSON = new Gson();

    private ResponseUtil() {
    }

    public static void writeJson(HttpServletResponse response, CommonResponse commonResponse) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(GSON.toJson(commonResponse));
    }

    public static void writeText(HttpServletResponse response, String text) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(text);
    }
}
