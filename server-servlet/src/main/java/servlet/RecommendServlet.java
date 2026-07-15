package servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@WebServlet("/RecommendServlet")
public class RecommendServlet extends HttpServlet {
    private static final String AI_CONFIG = "ai.properties";
    private static final String PLACEHOLDER_KEY = "YOUR_DEEPSEEK_API_KEY";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            Map<String, String> params = RequestUtil.readParams(request);
            String message = params.getOrDefault("message", "").trim();
            if (message.isEmpty()) {
                commonResponse.setResult("1", "请输入你的口味或饮食需求");
            } else if (message.length() > 300) {
                commonResponse.setResult("1", "需求描述过长，请控制在300字以内");
            } else {
                List<Dish> dishes = queryDishes();
                RecommendationResult result = recommend(message, dishes);
                commonResponse.setResult("0", "success");
                commonResponse.addProperty("reply", result.reply);
                commonResponse.addProperty("mode", result.mode);
            }
        } catch (Exception e) {
            commonResponse.setResult("1", "推荐失败，请稍后重试");
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    private RecommendationResult recommend(String message, List<Dish> dishes) {
        if (dishes.isEmpty()) {
            return new RecommendationResult(
                    "当前菜品库为空，暂无可推荐菜品。请先补充菜品数据后再使用智能推荐功能。",
                    "empty");
        }

        AiConfig config = AiConfig.load();
        if (config.enabled && config.hasApiKey()) {
            try {
                return new RecommendationResult(callDeepSeek(config, message, dishes), "DeepSeek");
            } catch (Exception ignored) {
                return new RecommendationResult(localRecommend(message, dishes)
                        + "\n\n注：DeepSeek 暂时调用失败，已使用本地规则推荐。", "local");
            }
        }
        return new RecommendationResult(localRecommend(message, dishes)
                + "\n\n注：尚未配置有效 DeepSeek API Key，当前使用本地规则推荐。", "local");
    }

    private String callDeepSeek(AiConfig config, String message, List<Dish> dishes) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", config.model);
        body.addProperty("temperature", 0.3);
        body.addProperty("stream", false);

        JsonArray messages = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content",
                "你是高校食堂智能推荐助手。只能从用户提供的菜品库中推荐，不要编造不存在的菜。" +
                        "如果用户想要一日三餐计划，请按早餐、午餐、晚餐输出。" +
                        "回答要简洁，包含菜名、餐厅/窗口、价格和推荐理由。");
        messages.add(system);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", "用户需求：" + message + "\n\n菜品库：\n" + formatDishes(dishes));
        messages.add(user);
        body.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.baseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(config.timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("DeepSeek HTTP " + response.statusCode());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            throw new IOException("DeepSeek response has no choices");
        }
        return choices.get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString()
                .trim();
    }

    private String localRecommend(String message, List<Dish> dishes) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (message.contains("一日三餐") || message.contains("减肥") || message.contains("计划")) {
            Dish breakfast = pick(dishes, "粥", "包", "卷", "牛奶", "早点");
            Dish lunch = pick(dishes, "鸡", "豆腐", "饭", "盖饭");
            Dish dinner = pick(dishes, "素", "粉", "面", "小面");
            return "给你一份偏清爽的一日三餐：\n"
                    + formatMeal("早餐", breakfast, "早餐先选轻一点的，负担小。") + "\n"
                    + formatMeal("午餐", lunch, "午餐补充主食和蛋白质，下午不容易饿。") + "\n"
                    + formatMeal("晚餐", dinner, "晚餐少油一点，更适合控制热量。");
        }
        if (message.contains("辣") || lower.contains("spicy")) {
            return singleDishReply(pick(dishes, "麻辣", "酸辣", "小面", "酸菜"), "你提到想吃辣的，这个口味更贴近。");
        }
        if (message.contains("清火") || message.contains("清淡") || message.contains("上火")) {
            return singleDishReply(pick(dishes, "粥", "素", "豆腐", "牛奶"), "你提到清火/清淡，这个相对温和。");
        }
        if (message.contains("甜") || message.contains("饮品") || message.contains("奶")) {
            return singleDishReply(pick(dishes, "牛奶", "波波", "花生", "粥"), "你提到甜口或饮品，这个更合适。");
        }
        return singleDishReply(pick(dishes, "招牌", "鸡", "饭", "面"), "综合菜名和食堂常见选择，先推荐这个。");
    }

    private String singleDishReply(Dish dish, String reason) {
        if (dish == null) {
            return "暂时没有可推荐的菜品。";
        }
        return "推荐：" + dish.name + "\n"
                + "位置：" + dish.room + " · " + dish.window + "\n"
                + "价格：" + dish.price + "元\n"
                + "理由：" + reason;
    }

    private String formatMeal(String title, Dish dish, String reason) {
        if (dish == null) {
            return title + "：暂无合适菜品";
        }
        return title + "：" + dish.name + "（" + dish.room + " · " + dish.window + "，" + dish.price + "元）" + reason;
    }

    private Dish pick(List<Dish> dishes, String... keywords) {
        for (String keyword : keywords) {
            for (Dish dish : dishes) {
                String text = dish.name + dish.window + dish.room;
                if (text.contains(keyword)) {
                    return dish;
                }
            }
        }
        return dishes.isEmpty() ? null : dishes.get(0);
    }

    private String formatDishes(List<Dish> dishes) {
        StringBuilder builder = new StringBuilder();
        for (Dish dish : dishes) {
            builder.append(dish.id)
                    .append(". ")
                    .append(dish.name)
                    .append(" | ")
                    .append(dish.room)
                    .append(" | ")
                    .append(dish.window)
                    .append(" | ")
                    .append(dish.price)
                    .append("元\n");
        }
        return builder.toString();
    }

    private List<Dish> queryDishes() throws Exception {
        List<Dish> dishes = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT dishId, dishName, dishWindow, dishRoom, price FROM dishes ORDER BY dishId");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                dishes.add(new Dish(
                        resultSet.getInt("dishId"),
                        resultSet.getString("dishName"),
                        resultSet.getString("dishWindow"),
                        resultSet.getString("dishRoom"),
                        resultSet.getFloat("price")
                ));
            }
        }
        return dishes;
    }

    private static class Dish {
        final int id;
        final String name;
        final String window;
        final String room;
        final float price;

        Dish(int id, String name, String window, String room, float price) {
            this.id = id;
            this.name = name;
            this.window = window;
            this.room = room;
            this.price = price;
        }
    }

    private static class RecommendationResult {
        final String reply;
        final String mode;

        RecommendationResult(String reply, String mode) {
            this.reply = reply;
            this.mode = mode;
        }
    }

    private static class AiConfig {
        boolean enabled;
        String apiKey;
        String baseUrl;
        String model;
        int timeoutSeconds;

        static AiConfig load() {
            AiConfig config = new AiConfig();
            config.enabled = false;
            config.apiKey = "";
            config.baseUrl = "https://api.deepseek.com";
            config.model = "deepseek-chat";
            config.timeoutSeconds = 20;

            Properties properties = new Properties();
            try (InputStream inputStream = RecommendServlet.class.getClassLoader().getResourceAsStream(AI_CONFIG)) {
                if (inputStream == null) {
                    return config;
                }
                properties.load(inputStream);
                config.enabled = Boolean.parseBoolean(properties.getProperty("ai.enabled", "false").trim());
                config.apiKey = properties.getProperty("deepseek.apiKey", "").trim();
                config.baseUrl = trimTrailingSlash(properties.getProperty("deepseek.baseUrl", config.baseUrl).trim());
                config.model = properties.getProperty("deepseek.model", config.model).trim();
                config.timeoutSeconds = Integer.parseInt(properties.getProperty("deepseek.timeoutSeconds", "20").trim());
            } catch (Exception ignored) {
                config.enabled = false;
            }
            return config;
        }

        boolean hasApiKey() {
            return !apiKey.isBlank() && !PLACEHOLDER_KEY.equals(apiKey);
        }

        private static String trimTrailingSlash(String value) {
            while (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            }
            return value;
        }
    }
}
