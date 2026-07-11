package servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class RequestUtil {
    private RequestUtil() {
    }

    public static Map<String, String> readParams(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, String> params = new HashMap<>();

        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }

        String body = readBody(request);
        if (!body.isBlank()) {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            JsonObject requestParam = root.has("requestParam") && root.get("requestParam").isJsonObject()
                    ? root.getAsJsonObject("requestParam")
                    : root;

            for (Map.Entry<String, com.google.gson.JsonElement> entry : requestParam.entrySet()) {
                if (!entry.getValue().isJsonNull()) {
                    params.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        }

        return params;
    }

    private static String readBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString().trim();
    }
}
