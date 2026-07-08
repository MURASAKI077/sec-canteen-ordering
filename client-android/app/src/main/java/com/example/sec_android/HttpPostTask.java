package com.example.sec_android;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpPostTask {

    // 使用线程池
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    // 所有回调都切回主线程，避免在子线程更新界面
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private final CommonRequest request;
    private final Handler mHandler;
    private final ResponseHandler rHandler;

    public HttpPostTask(CommonRequest request, Handler mHandler, ResponseHandler rHandler) {
        this.request = request;
        this.mHandler = mHandler;
        this.rHandler = rHandler;
    }


    public void execute(String urlStr) {
        EXECUTOR.execute(() -> {
            String result = doPost(urlStr);
            MAIN.post(() -> onPostExecute(result));
        });
    }

    private String doPost(String urlStr) {
        StringBuilder resultBuf = new StringBuilder();
        HttpURLConnection connection = null;

        try {
            Log.d("Post发送URL", urlStr);

            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            // 设置 POST + JSON 请求
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            String requestBody = request.getJsonStr();

            // 写入请求体，使用 UTF-8 保证中文参数正常传输
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                out.write(requestBody.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }

            Log.d("Post参数", requestBody);

            int responseCode = connection.getResponseCode();
            Log.d("PostTask返回值", String.valueOf(responseCode));

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                BufferedReader read = new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8)
                );

                String line;
                while ((line = read.readLine()) != null) {
                    resultBuf.append(line);
                }

                read.close();
            } else {
                sendHandlerMessage(
                        Constant.HANDLER_HTTP_RECEIVE_FAIL,
                        "[" + responseCode + "]" + connection.getResponseMessage()
                );
            }
        } catch (IOException e) {
            sendHandlerMessage(
                    Constant.HANDLER_HTTP_SEND_FAIL,
                    e.getClass().getName() + " : " + e.getMessage()
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return resultBuf.toString();
    }

    private void onPostExecute(String result) {
        if (rHandler == null || "".equals(result)) {
            return;
        }

        CommonResponse response = new CommonResponse(result);

        // resCode 为 0 表示业务成功，其余交给 fail 回调处理
        if ("0".equals(response.getResCode())) {
            rHandler.success(response);
        } else {
            rHandler.fail(response.getResCode(), response.getResMsg());
        }
    }

    private void sendHandlerMessage(int what, String message) {
        if (mHandler != null) {
            mHandler.obtainMessage(what, message).sendToTarget();
        }
    }
}
