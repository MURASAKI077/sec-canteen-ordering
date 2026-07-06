package com.example.sec_android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int UPDATE_TEXT1= 1;
    public static final int UPDATE_TEXT2= 2;


    private String realCode;
    private Button mBtRegisteractivityRegister;
    private ImageView mIvRegisteractivityBack;
    private EditText mEtRegisteractivityAccount;
    private EditText mEtRegisteractivityPassword1;
    private EditText mEtRegisteractivityPassword2;
    private EditText mEtRegisteractivityPhonecodes;
    private ImageView mIvRegisteractivityShowcode;

    private String account;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();

        //将验证码用图片的形式显示出来
        mIvRegisteractivityShowcode.setImageBitmap(Code.getInstance().createBitmap());
        realCode = Code.getInstance().getCode().toLowerCase();
    }

    private void initView(){
        mBtRegisteractivityRegister = findViewById(R.id.bt_registeractivity_register);
        mIvRegisteractivityBack = findViewById(R.id.iv_registeractivity_back);
        mEtRegisteractivityAccount = findViewById(R.id.et_registeractivity_account);
        mEtRegisteractivityPassword1 = findViewById(R.id.et_registeractivity_password1);
        mEtRegisteractivityPassword2 = findViewById(R.id.et_registeractivity_password2);
        mEtRegisteractivityPhonecodes = findViewById(R.id.et_registeractivity_phoneCodes);
        mIvRegisteractivityShowcode = findViewById(R.id.iv_registeractivity_showCode);

        /**
         * 注册页面能点击的就三个地方
         * top处返回箭头、刷新验证码图片、注册按钮
         */
        mIvRegisteractivityBack.setOnClickListener(this);
        mIvRegisteractivityShowcode.setOnClickListener(this);
        mBtRegisteractivityRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_registeractivity_back: //返回登录页面
                Intent intent1 = new Intent(this, LoginActivity.class);
                startActivity(intent1);
                finish();
                break;
            case R.id.iv_registeractivity_showCode:    //改变随机验证码的生成
                mIvRegisteractivityShowcode.setImageBitmap(Code.getInstance().createBitmap());
                realCode = Code.getInstance().getCode().toLowerCase();
                break;
            case R.id.bt_registeractivity_register:    //注册按钮
                Log.d("RegisterActivity","注册按钮被点击");
                //获取用户输入的用户名、密码、验证码
                account = mEtRegisteractivityAccount.getText().toString().trim();
                password = mEtRegisteractivityPassword2.getText().toString().trim();
                String phoneCode = mEtRegisteractivityPhonecodes.getText().toString().toLowerCase();
                //注册验证
                if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(phoneCode) ) {
                    if(!mEtRegisteractivityPassword1.getText().toString().equals(mEtRegisteractivityPassword2.getText().toString())){
                        Toast.makeText(this, "两次密码不同,注册失败", Toast.LENGTH_SHORT).show();
                    }
                    else if (phoneCode.equals(realCode)) {
                        //将用户名和密码加入到数据库中
                        //****************
                        String registerUrlStr = Constant.URL_Register + "?account=" + account + "&password=" + password;
                        new MyAsyncTask().execute(registerUrlStr);
                    } else {
                        Toast.makeText(this, "验证码错误,注册失败", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this, "未完善信息，注册失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    protected class MyAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            Log.w("WangXG", "task onPreExecute()");
        }

        /**
         * @param params 这里的params是一个数组，即AsyncTask在激活运行是调用execute()方法传入的参数
         */
        @Override
        protected String doInBackground(String... params) {
            Log.w("WangXG", "task doInBackground()");
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();
            try {
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
            Log.w("WangXG", "task onProgressUpdate()");
            // 如果在doInBackground方法，那么就会立刻执行本方法
            // 本方法在UI线程中执行，可以更新UI元素，典型的就是更新进度条进度，一般是在下载时候使用
        }

        /**
         * 运行在UI线程中，所以可以直接操作UI元素
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            Log.w("WangXG", "task onPostExecute()");
            Toast.makeText(RegisterActivity.this, s, Toast.LENGTH_SHORT).show();
            Log.w("WangXG", s);
            myReMethod(s);

        }
    }

    private void myReMethod(String reValue) {
        if(reValue.contains("成功")){
            Constant.landing = true;
            Log.d("LoginActivity","landing="+Constant.landing);
            Constant.account=account;
            Log.d("LoginActivity","account="+Constant.account);
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}