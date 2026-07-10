package com.example.sec_android;

/**
 * 网络请求结果回调接口。
 * HttpPostTask 在收到服务端响应后，通过这个接口把结果交给业务代码处理。
 */
public interface ResponseHandler {
    /**
     * 服务端返回成功结果时调用。
     *
     * @param response 已解析好的统一响应对象
     */
    void success(CommonResponse response);

    /**
     * 请求发送和接收成功，但服务端返回业务失败结果时调用。
     *
     * @param failCode 服务端返回的错误码
     * @param failMsg 服务端返回的错误信息
     */
    void fail(String failCode, String failMsg);
}
