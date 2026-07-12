package com.example.sec_android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private String realCode;
    private Button registerButton;
    private ImageView backButton;
    private EditText accountInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private EditText captchaInput;
    private ImageView captchaImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        refreshCaptcha();
    }

    private void initView() {
        registerButton = findViewById(R.id.bt_registeractivity_register);
        backButton = findViewById(R.id.iv_registeractivity_back);
        accountInput = findViewById(R.id.et_registeractivity_account);
        passwordInput = findViewById(R.id.et_registeractivity_password1);
        confirmPasswordInput = findViewById(R.id.et_registeractivity_password2);
        captchaInput = findViewById(R.id.et_registeractivity_phoneCodes);
        captchaImage = findViewById(R.id.iv_registeractivity_showCode);

        backButton.setOnClickListener(this);
        captchaImage.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.iv_registeractivity_back) {
            finish();
        } else if (viewId == R.id.iv_registeractivity_showCode) {
            refreshCaptcha();
        } else if (viewId == R.id.bt_registeractivity_register) {
            register();
        }
    }

    private void refreshCaptcha() {
        captchaImage.setImageBitmap(Code.getInstance().createBitmap());
        realCode = Code.getInstance().getCode().toLowerCase();
    }

    private void register() {
        String account = accountInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String captcha = captchaInput.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password) || TextUtils.isEmpty(captcha)) {
            showMessage("\u8bf7\u5b8c\u6574\u586b\u5199\u6ce8\u518c\u4fe1\u606f");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showMessage("\u4e24\u6b21\u8f93\u5165\u7684\u5bc6\u7801\u4e0d\u4e00\u81f4");
            return;
        }
        if (!captcha.equals(realCode)) {
            showMessage("\u9a8c\u8bc1\u7801\u9519\u8bef");
            refreshCaptcha();
            return;
        }

        LoadingDialogUtil.showLoadingDialog(this);
        CommonRequest request = new CommonRequest();
        request.addRequestParam("account", account);
        request.addRequestParam("password", password);
        new HttpPostTask(request, handler, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                LoadingDialogUtil.cancelLoading();
                Constant.landing = true;
                Constant.account = account;
                showMessage(response.getResMsg());
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void fail(String failCode, String failMsg) {
                LoadingDialogUtil.cancelLoading();
                showMessage(TextUtils.isEmpty(failMsg) ? "\u6ce8\u518c\u5931\u8d25" : failMsg);
                refreshCaptcha();
            }
        }).execute(Constant.URL_Register);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            LoadingDialogUtil.cancelLoading();
            showMessage("\u7f51\u7edc\u8bf7\u6c42\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u670d\u52a1\u5668");
        }
    };
}
