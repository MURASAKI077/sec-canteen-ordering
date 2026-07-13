package com.example.sec_android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int ACCOUNT_MIN_LENGTH = 3;
    private static final int ACCOUNT_MAX_LENGTH = 20;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 20;
    private static final int CAPTCHA_LENGTH = 4;
    private static final String ACCOUNT_PATTERN = "^[A-Za-z0-9_]+$";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$";

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
        accountInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ACCOUNT_MAX_LENGTH)});
        passwordInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(PASSWORD_MAX_LENGTH)});
        confirmPasswordInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(PASSWORD_MAX_LENGTH)});
        captchaInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CAPTCHA_LENGTH)});
        accountInput.setHint("3-20 位字母/数字/下划线");
        passwordInput.setHint("6-20 位字母+数字");
        confirmPasswordInput.setHint("请再次输入密码");
        captchaInput.setHint("请输入 4 位验证码");

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

        if (!validateRegisterInput(account, password, confirmPassword, captcha)) {
            return;
        }

        if (!captcha.equals(realCode)) {
            captchaInput.setError("验证码错误");
            captchaInput.requestFocus();
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

    private boolean validateRegisterInput(String account, String password, String confirmPassword, String captcha) {
        if (TextUtils.isEmpty(account)) {
            accountInput.setError("请输入账号");
            accountInput.requestFocus();
            return false;
        }
        if (account.length() < ACCOUNT_MIN_LENGTH) {
            accountInput.setError("账号至少需要 3 位");
            accountInput.requestFocus();
            return false;
        }
        if (!account.matches(ACCOUNT_PATTERN)) {
            accountInput.setError("账号只能包含字母、数字或下划线");
            accountInput.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("请输入密码");
            passwordInput.requestFocus();
            return false;
        }
        if (password.length() < PASSWORD_MIN_LENGTH) {
            passwordInput.setError("密码至少需要 6 位");
            passwordInput.requestFocus();
            return false;
        }
        if (!password.matches(PASSWORD_PATTERN)) {
            passwordInput.setError("密码需同时包含字母和数字");
            passwordInput.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("请再次输入密码");
            confirmPasswordInput.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("两次密码不一致");
            confirmPasswordInput.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(captcha)) {
            captchaInput.setError("请输入验证码");
            captchaInput.requestFocus();
            return false;
        }
        if (captcha.length() != CAPTCHA_LENGTH) {
            captchaInput.setError("验证码为 4 位");
            captchaInput.requestFocus();
            return false;
        }
        return true;
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
