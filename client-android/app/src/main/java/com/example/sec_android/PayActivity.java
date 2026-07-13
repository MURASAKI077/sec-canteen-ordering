package com.example.sec_android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 模拟支付页（M-Pay）
 * 进入页面先调用现有 OrderServlet 创建订单(status=PLACED)并取得 orderId，
 * 用户选择支付方式后调用 PayServlet 完成模拟支付：服务端校验订单归属/状态/金额，
 * 在事务中写入 payments 流水并将订单状态更新为 PAID。
 * 选择"暂不支付"则订单保留 PLACED，可在个人中心继续查看或取消。
 */
public class PayActivity extends AppCompatActivity {

    private static final String EXTRA_ROOM = "extra_room";
    private static final String EXTRA_WINDOW = "extra_window";
    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_PRICE = "extra_price";

    private TextView tvDishName;
    private TextView tvLocation;
    private TextView tvAmount;
    private TextView tvOrderId;
    private TextView tvHint;
    private RadioGroup rgMethod;
    private Button btConfirm;
    private Button btLater;

    private String room;
    private String window;
    private String name;
    private String price;

    private long orderId = -1;
    private boolean paying = false;

    /** 供下单确认流程跳转进入支付页 */
    public static void start(Context context, String room, String window, String name, String price) {
        Intent intent = new Intent(context, PayActivity.class);
        intent.putExtra(EXTRA_ROOM, room);
        intent.putExtra(EXTRA_WINDOW, window);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_PRICE, price);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        room = getIntent().getStringExtra(EXTRA_ROOM);
        window = getIntent().getStringExtra(EXTRA_WINDOW);
        name = getIntent().getStringExtra(EXTRA_NAME);
        price = getIntent().getStringExtra(EXTRA_PRICE);

        initView();
        createOrder();
    }

    private void initView() {
        tvDishName = findViewById(R.id.tv_pay_dish_name);
        tvLocation = findViewById(R.id.tv_pay_location);
        tvAmount = findViewById(R.id.tv_pay_amount);
        tvOrderId = findViewById(R.id.tv_pay_order_id);
        tvHint = findViewById(R.id.tv_pay_hint);
        rgMethod = findViewById(R.id.rg_pay_method);
        btConfirm = findViewById(R.id.bt_pay_confirm);
        btLater = findViewById(R.id.bt_pay_later);

        tvDishName.setText(name == null ? "" : name);
        tvLocation.setText((room == null ? "" : room) + " · " + (window == null ? "" : window));
        tvAmount.setText((price == null ? "" : price) + " 元");
        tvOrderId.setText("订单创建中…");
        tvHint.setText("正在为你创建订单，请稍候");

        btConfirm.setEnabled(false);
        btConfirm.setOnClickListener(v -> pay());
        btLater.setOnClickListener(v -> {
            if (orderId > 0) {
                Toast.makeText(this, "订单已保存为未支付，可在个人中心查看", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    /** 调用现有 OrderServlet 创建订单，拿到 orderId 后才允许支付 */
    private void createOrder() {
        LoadingDialogUtil.showLoadingDialog(this);
        CommonRequest request = new CommonRequest();
        request.addRequestParam("room", room);
        request.addRequestParam("window", window);
        request.addRequestParam("name", name);
        request.addRequestParam("price", price);
        request.addRequestParam("account", Constant.account);
        request.addRequestParam("quantity", "1");

        new HttpPostTask(request, mHandler, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                LoadingDialogUtil.cancelLoading();
                try {
                    orderId = Long.parseLong(response.getPropertyMap().get("orderId"));
                } catch (Exception e) {
                    orderId = -1;
                }
                if (orderId > 0) {
                    tvOrderId.setText("订单 #" + orderId);
                    tvHint.setText("订单已创建，请选择支付方式完成支付");
                    btConfirm.setEnabled(true);
                } else {
                    Toast.makeText(PayActivity.this, "订单创建异常，请重试", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void fail(String failCode, String failMsg) {
                LoadingDialogUtil.cancelLoading();
                Toast.makeText(PayActivity.this,
                        failMsg == null || failMsg.trim().isEmpty() ? "创建订单失败，请重试" : failMsg,
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }).execute(Constant.URL_Order);
    }

    /** 调用 PayServlet 完成模拟支付 */
    private void pay() {
        if (orderId <= 0 || paying) {
            return;
        }
        paying = true;
        btConfirm.setEnabled(false);
        LoadingDialogUtil.showLoadingDialog(this);

        CommonRequest request = new CommonRequest();
        request.addRequestParam("orderId", String.valueOf(orderId));
        request.addRequestParam("account", Constant.account);
        request.addRequestParam("payMethod", selectedMethod());
        request.addRequestParam("amount", price);

        new HttpPostTask(request, mHandler, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                LoadingDialogUtil.cancelLoading();
                String payId = response.getPropertyMap().get("payId");
                Toast.makeText(PayActivity.this,
                        "支付成功，流水号 #" + (payId == null ? "-" : payId), Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void fail(String failCode, String failMsg) {
                LoadingDialogUtil.cancelLoading();
                paying = false;
                btConfirm.setEnabled(true);
                DialogUtil.showHintDialog(PayActivity.this,
                        failMsg == null || failMsg.trim().isEmpty() ? "支付失败，请重试" : failMsg,
                        false);
            }
        }).execute(Constant.URL_Pay);
    }

    private String selectedMethod() {
        int checkedId = rgMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_pay_wechat) {
            return "WECHAT";
        }
        if (checkedId == R.id.rb_pay_alipay) {
            return "ALIPAY";
        }
        return "CAMPUS_CARD";
    }

    /** 网络通信层错误的统一处理（与 HomeFragment 的 Handler 风格一致） */
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            LoadingDialogUtil.cancelLoading();
            String errorMsg = msg.obj == null ? "" : msg.obj.toString();
            if (msg.what == Constant.HANDLER_HTTP_SEND_FAIL) {
                LogUtil.logErr(errorMsg);
                Toast.makeText(PayActivity.this, "请求发送失败，请重试", Toast.LENGTH_SHORT).show();
            } else if (msg.what == Constant.HANDLER_HTTP_RECEIVE_FAIL) {
                LogUtil.logErr(errorMsg);
                Toast.makeText(PayActivity.this, "响应接收失败，请重试", Toast.LENGTH_SHORT).show();
            }
            paying = false;
            if (btConfirm != null && orderId > 0) {
                btConfirm.setEnabled(true);
            }
        }
    };
}