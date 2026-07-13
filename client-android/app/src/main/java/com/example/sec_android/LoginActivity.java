package com.example.sec_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences preferences;
    private ImageView backButton;
    private TextView registerButton;
    private EditText accountInput;
    private EditText passwordInput;
    private Button loginButton;
    private CheckBox rememberAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean shouldRemember = preferences.getBoolean("remember_account", false);
        if (shouldRemember) {
            accountInput.setText(preferences.getString("account", ""));
            rememberAccount.setChecked(true);
        }
        preferences.edit()
                .remove("password")
                .remove("remember_password")
                .apply();
    }

    private void initView() {
        backButton = findViewById(R.id.iv_loginactivity_back);
        loginButton = findViewById(R.id.bt_loginactivity_login);
        registerButton = findViewById(R.id.tv_loginactivity_register);
        accountInput = findViewById(R.id.et_loginactivity_account);
        passwordInput = findViewById(R.id.et_loginactivity_password);
        rememberAccount = findViewById(R.id.remember_pass);
        accountInput.setHint("请输入账号");
        passwordInput.setHint("请输入密码");

        backButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.iv_loginactivity_back) {
            finish();
        } else if (viewId == R.id.tv_loginactivity_register) {
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (viewId == R.id.bt_loginactivity_login) {
            login();
        }
    }

    private void login() {
        String account = accountInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (TextUtils.isEmpty(account)) {
            accountInput.setError("请输入账号");
            accountInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("请输入密码");
            passwordInput.requestFocus();
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
                saveRememberedAccount(account);
                Constant.landing = true;
                Constant.account = account;
                Toast.makeText(LoginActivity.this, response.getResMsg(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void fail(String failCode, String failMsg) {
                LoadingDialogUtil.cancelLoading();
                Toast.makeText(LoginActivity.this,
                        TextUtils.isEmpty(failMsg) ? "\u767b\u5f55\u5931\u8d25" : failMsg,
                        Toast.LENGTH_SHORT).show();
            }
        }).execute(Constant.URL_Login);
    }

    private void saveRememberedAccount(String account) {
        boolean remember = rememberAccount.isChecked();
        SharedPreferences.Editor editor = preferences.edit()
                .putBoolean("remember_account", remember)
                .remove("password")
                .remove("remember_password");
        if (remember) {
            editor.putString("account", account);
        } else {
            editor.remove("account");
        }
        editor.apply();
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            LoadingDialogUtil.cancelLoading();
            Toast.makeText(LoginActivity.this,
                    "\u7f51\u7edc\u8bf7\u6c42\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u670d\u52a1\u5668",
                    Toast.LENGTH_SHORT).show();
        }
    };
}
