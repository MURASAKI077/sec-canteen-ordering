package com.example.sec_android;

import android.os.Handler;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
//负责下单处理
public final class OrderHelper {

    private OrderHelper() {
    }

    public static void order(final FragmentActivity activity,
                             Handler handler,
                             String room,
                             String window,
                             String name,
                             String price,
                             String account,
                             final Runnable onSuccess) {
        CommonRequest request = new CommonRequest();
        request.addRequestParam("room", room);
        request.addRequestParam("window", window);
        request.addRequestParam("name", name);
        request.addRequestParam("price", price);
        request.addRequestParam("account", account);
        request.addRequestParam("quantity", "1");

        new HttpPostTask(request, handler, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                LoadingDialogUtil.cancelLoading();
                Toast.makeText(activity, "下单成功", Toast.LENGTH_SHORT).show();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void fail(String failCode, String failMsg) {
                LoadingDialogUtil.cancelLoading();
                DialogUtil.showHintDialog(activity,
                        failMsg == null || failMsg.trim().isEmpty() ? "下单失败，请重试" : failMsg,
                        false);
            }
        }).execute(Constant.URL_Order);
    }
}
