package com.example.sec_android;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 基本请求体封装类
 */
public class CommonRequest {
    private String requestCode;
    /**
     * 请求参数，保存当前接口需要提交的键值对
     */
    private HashMap<String, String> requestParam;

    public CommonRequest() {
        requestCode = "";
        requestParam = new HashMap<>();
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    /**
     * 为请求报文设置参数
     * @param paramKey 参数名
     * @param paramValue 参数值
     */
    public void addRequestParam(String paramKey, String paramValue) {
        requestParam.put(paramKey, paramValue);
    }

    /**
     * 将请求报文体组装成json形式的字符串，以便进行网络发送
     * @return 请求报文的json字符串
     */
    public String getJsonStr() {
        JSONObject object = new JSONObject();
        JSONObject param = new JSONObject(requestParam);
        try {
            // 组装统一请求体，字段名需要和服务端接口规范保持一致
            object.put("requestCode", requestCode);
            object.put("requestParam", param);
        } catch (JSONException e) {
            Log.e("CommonRequest","请求报文组装异常：" + e.getMessage());
        }
        // 打印原始请求报文
        Log.d("Request原始报文：",object.toString());
        return object.toString();
    }
}
