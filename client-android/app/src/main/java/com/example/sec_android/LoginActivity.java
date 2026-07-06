package com.example.sec_android;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "LoginActivity";


    private SharedPreferences pref;
    private ImageView mIvLoginactivityBack;
    private TextView mTvLoginactivityRegister;
    private EditText mEtLoginactivityAccount;
    private EditText mEtLoginactivityPassword;
    private Button mBtLoginactivityLogin;

    private CheckBox rememberPass;
    private String account;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MainActivity.viewPager.setCurrentItem(0);

        initView();


        pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember = pref.getBoolean("remember_password", false);


        if (isRemember) {
            // 将账号和密码都设置到文本框中
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            mEtLoginactivityAccount.setText(account);
            mEtLoginactivityPassword.setText(password);
            rememberPass.setChecked(true);
        }


    }

    private void initView() {
        // 初始化控件
        mIvLoginactivityBack=findViewById(R.id.iv_loginactivity_back);
        mBtLoginactivityLogin = findViewById(R.id.bt_loginactivity_login);
        mTvLoginactivityRegister = findViewById(R.id.tv_loginactivity_register);
        mEtLoginactivityAccount = findViewById(R.id.et_loginactivity_account);
        mEtLoginactivityPassword = findViewById(R.id.et_loginactivity_password);

        // 设置点击事件监听器
        mIvLoginactivityBack.setOnClickListener(this);
        mBtLoginactivityLogin.setOnClickListener(this);
        mTvLoginactivityRegister.setOnClickListener(this);

        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_loginactivity_back:
                finish();
                break;

            // 跳转到注册界面
            case R.id.tv_loginactivity_register:
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                break;
            /**
             * 登录验证：
             *
             * 从EditText的对象上获取文本编辑框输入的数据，并把左右两边的空格去掉
             *  String name = mEtLoginactivityUsername.getText().toString().trim();
             *  String password = mEtLoginactivityPassword.getText().toString().trim();
             *  进行匹配验证,先判断一下用户名密码是否为空，
             *  if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password))
             *  再进而for循环判断是否与数据库中的数据相匹配
             *  if (name.equals(user.getName()) && password.equals(user.getPassword()))
             *  一旦匹配，立即将match = true；break；
             *  否则 一直匹配到结束 match = false；
             *
             *  登录成功之后，进行页面跳转：
             *
             *  Intent intent = new Intent(this, MainActivity.class);
             *  startActivity(intent);
             *  finish();//销毁此Activity
             */
            case R.id.bt_loginactivity_login:
                mEtLoginactivityAccount.setFocusable(true);
                mEtLoginactivityAccount.setFocusableInTouchMode(true);
                mEtLoginactivityAccount.requestFocus();
                account = mEtLoginactivityAccount.getText().toString().trim();
                password = mEtLoginactivityPassword.getText().toString().trim();
                if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {

                    Log.d(TAG,"用户名和密码不为空，尝试登陆");
                    String loginUrlStr = Constant.URL_Login + "?account=" + account + "&password=" + password;
                    new MyAsyncTask().execute(loginUrlStr);
                }

                else {
                    Toast.makeText(this, "请输入你的用户名或密码", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    public  class MyAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            Log.w("LoginActivity", "task onPreExecute()");
        }

        /**
         * @param params 这里的params是一个数组，即AsyncTask在激活运行是调用execute()方法传入的参数
         */
        @Override
        protected String doInBackground(String... params) {
            Log.w("LoginActivity", "task doInBackground()");
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();
            try {
                Log.d("登录发送URL",params[0]);
                URL url = new URL(params[0]); // 声明一个URL,注意如果用百度首页实验，请使用https开头，否则获取不到返回报文
                connection = (HttpURLConnection) url.openConnection(); // 打开该URL连接
                connection.setRequestMethod("GET"); // 设置请求方法，“POST或GET”，我们这里用GET，在说到POST的时候再用POST
                connection.setConnectTimeout(80000); // 设置连接建立的超时时间
                connection.setReadTimeout(80000); // 设置网络报文收发超时时间
                InputStream in = connection.getInputStream();  // 通过连接的输入流获取下发报文，然后就是Java的流处理
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response.toString(); // 这里返回的结果就作为onPostExecute方法的入参
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.w("LoginActivity", "task onProgressUpdate()");
            // 如果在doInBackground方法，那么就会立刻执行本方法
            // 本方法在UI线程中执行，可以更新UI元素，典型的就是更新进度条进度，一般是在下载时候使用
        }

        /**
         * 运行在UI线程中，所以可以直接操作UI元素
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            Log.w("LoginActivity", "task onPostExecute()");
            Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();
            Log.w("LoginActivity", s);
            myReMethod(s);
        }
    }

    private void myReMethod(String reValue) {
        if(reValue.contains("成功")){
            Constant.landing = true;
            Log.d("LoginActivity","landing="+Constant.landing);
            Constant.account=account;
            Log.d("LoginActivity","account="+Constant.account);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
