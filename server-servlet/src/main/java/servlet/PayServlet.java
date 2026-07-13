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
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

/**
 * 模拟支付接口（M-Pay）
 * 契约：POST/GET，参数 orderId、account、payMethod(CAMPUS_CARD/WECHAT/ALIPAY)、amount；
 *      成功返回 resCode=0，property 含 payId/orderId/payMethod/amount/status/payTime。
 * 业务校验：订单必须存在且属于该账号；状态必须为 PLACED（已支付/已取消拒绝）；
 *      金额由服务端按 orders.quantity * dishes.price 重算并与客户端提交值比对；
 *      payments.orderId 唯一索引 + 事务内条件更新，防止重复支付。
 * 数据表：payments 由本类首次调用时自动创建（CREATE TABLE IF NOT EXISTS）
 */
@WebServlet("/PayServlet")
public class PayServlet extends HttpServlet {

    private static final Set<String> SUPPORTED_METHODS = Set.of("CAMPUS_CARD", "WECHAT", "ALIPAY");
    private static volatile boolean tableReady = false;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        try {
            ensurePaymentTable();

            Map<String, String> params = RequestUtil.readParams(request);
            String account = params.getOrDefault("account", "").trim();
            String payMethod = params.getOrDefault("payMethod", "").trim();
            long orderId = Long.parseLong(params.getOrDefault("orderId", "").trim());
            double amount = Double.parseDouble(params.getOrDefault("amount", "").trim());

            if (account.isEmpty()) {
                commonResponse.setResult("1", "account is required");
            } else if (!SUPPORTED_METHODS.contains(payMethod)) {
                commonResponse.setResult("1", "payMethod is not supported");
            } else if (amount <= 0) {
                commonResponse.setResult("1", "amount must be positive");
            } else {
                try (Connection connection = DatabaseUtil.getConnection()) {
                    PayableOrder order = findPayableOrder(connection, orderId, account);
                    if (order == null) {
                        commonResponse.setResult("1", "order does not exist or does not belong to this account");
                    } else if (!"PLACED".equals(order.status)) {
                        commonResponse.setResult("1", "order is already paid or cancelled");
                    } else if (Math.abs(order.expectedAmount - amount) > 0.005) {
                        commonResponse.setResult("1", "amount does not match the order");
                    } else {
                        payInTransaction(connection, commonResponse, orderId, order, payMethod);
                    }
                }
            }
        } catch (NumberFormatException e) {
            commonResponse.setResult("1", "orderId or amount format is invalid");
        } catch (Exception e) {
            commonResponse.setResult("1", e.getMessage());
        }
        ResponseUtil.writeJson(response, commonResponse);
    }

    /** 查询待支付订单：校验归属并由服务端重算应付金额 */
    private PayableOrder findPayableOrder(Connection connection, long orderId, String account) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT o.userId, o.status, o.quantity * d.price AS expectedAmount " +
                        "FROM orders o " +
                        "JOIN account a ON o.userId = a.userId " +
                        "JOIN dishes d ON o.dishId = d.dishId " +
                        "WHERE o.orderId = ? AND a.userAccount = ?")) {
            statement.setLong(1, orderId);
            statement.setString(2, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                PayableOrder order = new PayableOrder();
                order.userId = resultSet.getInt("userId");
                order.status = resultSet.getString("status");
                order.expectedAmount = resultSet.getDouble("expectedAmount");
                return order;
            }
        }
    }

    /** 写入支付流水 + 订单状态 PLACED -> PAID，任一步失败整体回滚 */
    private void payInTransaction(Connection connection,
                                  CommonResponse commonResponse,
                                  long orderId,
                                  PayableOrder order,
                                  String payMethod) throws Exception {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            int rows;
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE orders SET status = 'PAID' WHERE orderId = ? AND status = 'PLACED'")) {
                statement.setLong(1, orderId);
                rows = statement.executeUpdate();
            }
            if (rows != 1) {
                connection.rollback();
                commonResponse.setResult("1", "order is already paid or cancelled");
                return;
            }

            long payId;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO payments (orderId, userId, amount, payMethod, payStatus) " +
                            "VALUES (?, ?, ?, ?, 'SUCCESS')",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, orderId);
                statement.setInt(2, order.userId);
                statement.setDouble(3, order.expectedAmount);
                statement.setString(4, payMethod);
                statement.executeUpdate();
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new IllegalStateException("payment ID was not generated");
                    }
                    payId = generatedKeys.getLong(1);
                }
            }

            Timestamp payTime = findPayTime(connection, payId);
            connection.commit();

            commonResponse.setResult("0", "success");
            commonResponse.addProperty("payId", String.valueOf(payId));
            commonResponse.addProperty("orderId", String.valueOf(orderId));
            commonResponse.addProperty("payMethod", payMethod);
            commonResponse.addProperty("amount", String.valueOf(order.expectedAmount));
            commonResponse.addProperty("status", "PAID");
            commonResponse.addProperty("payTime", payTime.toString());
        } catch (SQLIntegrityConstraintViolationException e) {
            connection.rollback();
            commonResponse.setResult("1", "order has already been paid");
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private Timestamp findPayTime(Connection connection, long payId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT payTime FROM payments WHERE payId = ?")) {
            statement.setLong(1, payId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("created payment was not found");
                }
                return resultSet.getTimestamp("payTime");
            }
        }
    }

    /** 首次调用时自动创建 payments 流水表 */
    private static synchronized void ensurePaymentTable() throws Exception {
        if (tableReady) {
            return;
        }
        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS payments (" +
                            "  payId BIGINT NOT NULL AUTO_INCREMENT," +
                            "  orderId BIGINT NOT NULL," +
                            "  userId INT NOT NULL," +
                            "  amount DECIMAL(8,2) NOT NULL," +
                            "  payMethod VARCHAR(20) NOT NULL," +
                            "  payStatus VARCHAR(20) NOT NULL DEFAULT 'SUCCESS'," +
                            "  payTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "  PRIMARY KEY (payId)," +
                            "  UNIQUE KEY uk_payments_orderId (orderId)," +
                            "  KEY idx_payments_user_time (userId, payTime)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
        }
        tableReady = true;
    }

    /** 待支付订单的内部载体 */
    private static class PayableOrder {
        int userId;
        String status;
        double expectedAmount;
    }
}