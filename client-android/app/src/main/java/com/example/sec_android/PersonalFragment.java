package com.example.sec_android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

public class PersonalFragment extends Fragment {

    private static final String TAG = "PersonalFragment";

    private TextView accountTextView;
    private ListView orderListView;
    private OrderAdapter orderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal, container, false);

        accountTextView = view.findViewById(R.id.tv_personal_account);
        TextView exitTextView = view.findViewById(R.id.tv_personal_exit);
        TextView emptyOrderTextView = view.findViewById(R.id.tv_order_empty);
        orderListView = view.findViewById(R.id.List_myOrder);

        orderListView.setEmptyView(emptyOrderTextView);
        orderListView.setOnItemClickListener(this::onOrderItemClick);
        exitTextView.setOnClickListener(v -> showLogoutConfirmation());

        refreshContent();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshContent();
    }

    private boolean isLoggedIn() {
        return Constant.landing && !TextUtils.isEmpty(Constant.account);
    }

    public void refreshContent() {
        if (accountTextView == null || orderListView == null || !isAdded()) {
            return;
        }

        if (!isLoggedIn()) {
            accountTextView.setText("");
            orderAdapter = new OrderAdapter(requireActivity(), new ArrayList<HashMap<String, String>>());
            orderListView.setAdapter(orderAdapter);
            return;
        }

        accountTextView.setText(Constant.account);
        loadOrderHistory();
    }

    private void showLogoutConfirmation() {
        if (!isAdded()) {
            return;
        }

        View.OnClickListener cancelListener = v -> DialogUtil.dismissDialog();
        View.OnClickListener confirmListener = v -> {
            DialogUtil.dismissDialog();
            logout();
        };

        DialogUtil.showDecideDialogWithTitle(
                requireContext(),
                getString(R.string.exit_login),
                getString(R.string.exit_login_confirm),
                cancelListener,
                confirmListener
        );
    }

    private void logout() {
        new LoginSessionManager(requireContext()).clear();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadOrderHistory() {
        CommonRequest request = new CommonRequest();
        request.addRequestParam("account", Constant.account);
        sendHttpPostRequest(Constant.URL_OrderRecord, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                if (!isAdded()) {
                    return;
                }

                LoadingDialogUtil.cancelLoading();
                orderAdapter = new OrderAdapter(requireActivity(), response.getDataList());
                orderListView.setAdapter(orderAdapter);
            }

            @Override
            public void fail(String failCode, String failMsg) {
                if (!isAdded()) {
                    return;
                }

                LoadingDialogUtil.cancelLoading();
                Log.e(TAG, "load order history failed: " + failCode + " " + failMsg);
                Toast.makeText(requireContext(), "\u83b7\u53d6\u8ba2\u5355\u8bb0\u5f55\u5931\u8d25", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onOrderItemClick(AdapterView<?> parent, View itemView, int position, long id) {
        if (!isAdded() || !isLoggedIn()) {
            return;
        }

        HashMap<String, String> order = (HashMap<String, String>) parent.getItemAtPosition(position);
        String orderId = order.get("orderId");
        String name = order.get("name");
        if ("CANCELLED".equals(order.get("status"))) {
            DialogUtil.showHintDialog(requireContext(), "\u8be5\u8ba2\u5355\u5df2\u53d6\u6d88", false);
            return;
        }

        View.OnClickListener cancelListener = v -> DialogUtil.dismissDialog();
        View.OnClickListener confirmListener = v -> {
            DialogUtil.dismissDialog();
            cancelOrder(orderId);
        };

        DialogUtil.showDecideDialogWithTitle(
                requireContext(),
                "\u662f\u5426\u53d6\u6d88\u8ba2\u5355",
                name == null ? "" : name,
                cancelListener,
                confirmListener
        );
    }

    private void cancelOrder(String orderId) {
        CommonRequest request = new CommonRequest();
        request.addRequestParam("account", Constant.account);
        request.addRequestParam("orderId", orderId == null ? "" : orderId);
        sendHttpPostRequest(Constant.URL_CancelOrder, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "\u8ba2\u5355\u5df2\u53d6\u6d88", Toast.LENGTH_SHORT).show();
                loadOrderHistory();
            }

            @Override
            public void fail(String failCode, String failMsg) {
                if (!isAdded()) {
                    return;
                }
                DialogUtil.showHintDialog(requireContext(),
                        TextUtils.isEmpty(failMsg) ? "\u53d6\u6d88\u8ba2\u5355\u5931\u8d25" : failMsg,
                        false);
            }
        });
    }

    private void sendHttpPostRequest(String url,
                                     CommonRequest request,
                                     ResponseHandler responseHandler) {
        new HttpPostTask(request, mHandler, responseHandler).execute(url);
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (!isAdded()) {
                return;
            }

            String errorMsg = msg.obj == null ? "" : msg.obj.toString();
            if (msg.what == Constant.HANDLER_HTTP_SEND_FAIL) {
                LogUtil.logErr(errorMsg);
                Toast.makeText(requireContext(), "\u8bf7\u6c42\u53d1\u9001\u5931\u8d25\uff0c\u8bf7\u91cd\u8bd5", Toast.LENGTH_SHORT).show();
            } else if (msg.what == Constant.HANDLER_HTTP_RECEIVE_FAIL) {
                LogUtil.logErr(errorMsg);
                Toast.makeText(requireContext(), "\u83b7\u53d6\u8ba2\u5355\u8bb0\u5f55\u5931\u8d25", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
