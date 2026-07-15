package com.example.sec_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RecommendActivity extends AppCompatActivity {
    private LinearLayout messageContainer;
    private ScrollView messageScrollView;
    private EditText inputEditText;
    private TextView sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        TextView backTextView = findViewById(R.id.tv_recommend_back);
        messageContainer = findViewById(R.id.layout_recommend_messages);
        messageScrollView = findViewById(R.id.scroll_recommend_messages);
        inputEditText = findViewById(R.id.et_recommend_input);
        sendButton = findViewById(R.id.btn_recommend_send);

        backTextView.setOnClickListener(v -> finish());
        sendButton.setOnClickListener(v -> sendMessage());

        addBotMessage("你好，我可以根据口味、清淡/辣、减肥、一日三餐计划来推荐菜品。比如：今天想吃清火一点的，或者帮我安排减脂一日三餐。");
    }

    private void sendMessage() {
        String message = inputEditText.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "先说说你想吃什么类型", Toast.LENGTH_SHORT).show();
            return;
        }

        inputEditText.setText("");
        addUserMessage(message);
        addBotMessage("正在从菜品库里匹配，请稍等...");

        CommonRequest request = new CommonRequest();
        request.addRequestParam("message", message);
        new HttpPostTask(request, recommendHandler, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                removeLastLoadingMessage();
                String reply = response.getPropertyMap().get("reply");
                String mode = response.getPropertyMap().get("mode");
                addBotMessage((reply == null ? "暂无推荐结果" : reply) + sourceText(mode));
            }

            @Override
            public void fail(String failCode, String failMsg) {
                removeLastLoadingMessage();
                addBotMessage(TextUtils.isEmpty(failMsg) ? "推荐失败，请换个说法再试试。" : failMsg);
            }
        }).execute(Constant.URL + "RecommendServlet");
    }

    private String sourceText(String mode) {
        if ("DeepSeek".equals(mode)) {
            return "\n\n来源：DeepSeek 智能推荐";
        }
        if ("local".equals(mode)) {
            return "\n\n来源：本地菜品规则推荐";
        }
        if ("empty".equals(mode)) {
            return "\n\n来源：菜品库为空提示";
        }
        return "";
    }

    private void addUserMessage(String message) {
        addMessage(message, true);
    }

    private void addBotMessage(String message) {
        addMessage(message, false);
    }

    private void addMessage(String message, boolean user) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(15);
        textView.setTextColor(getResources().getColor(user ? R.color.ui_text_inverse : R.color.ui_text_main));
        textView.setPadding(18, 14, 18, 14);
        textView.setBackgroundResource(user ? R.drawable.shape_chat_user : R.drawable.shape_chat_bot);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 8, 12, 8);
        params.gravity = user ? Gravity.END : Gravity.START;
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.78);
        textView.setLayoutParams(params);
        messageContainer.addView(textView);
        scrollToBottom();
    }

    private void removeLastLoadingMessage() {
        int count = messageContainer.getChildCount();
        if (count <= 0) {
            return;
        }
        TextView last = (TextView) messageContainer.getChildAt(count - 1);
        if ("正在从菜品库里匹配，请稍等...".contentEquals(last.getText())) {
            messageContainer.removeViewAt(count - 1);
        }
    }

    private void scrollToBottom() {
        messageScrollView.post(() -> messageScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private final Handler recommendHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String errorMsg = msg.obj == null ? "" : msg.obj.toString();
            LogUtil.logErr(errorMsg);
            removeLastLoadingMessage();
            addBotMessage("请求发送失败，请检查服务器或网络。");
        }
    };
}
