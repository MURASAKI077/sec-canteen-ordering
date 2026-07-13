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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
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
        sendHttpPostRequest(Constant.URL_OrderRecord, request, orderHistoryErrorHandler, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                if (!isAdded()) {
                    return;
                }

                LoadingDialogUtil.cancelLoading();
                orderAdapter = new OrderAdapter(requireActivity(), response.getDataList(), new OrderAdapter.OnOrderActionListener() {
                    @Override
                    public void onCancelOrder(HashMap<String, String> order) {
                        showCancelOrderConfirmation(order.get("orderId"), order.get("name"));
                    }

                    @Override
                    public void onReviewOrder(HashMap<String, String> order) {
                        showReviewDialog(order);
                    }
                });
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

    private void showCancelOrderConfirmation(String orderId, String name) {
        if (!isAdded()) {
            return;
        }

        View.OnClickListener backListener = v -> DialogUtil.dismissDialog();
        View.OnClickListener confirmListener = v -> {
            DialogUtil.dismissDialog();
            cancelOrder(orderId);
        };
        String dishName = TextUtils.isEmpty(name) ? "该订单" : name;
        DialogUtil.showDecideDialogWithTitle(
                requireContext(),
                "确认取消订单",
                dishName + "\n取消后将不能评价，是否继续？",
                "返回",
                backListener,
                "确认取消",
                confirmListener
        );
    }

    private void showReviewDialog(HashMap<String, String> order) {
        if (!isAdded()) {
            return;
        }

        View reviewView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_review, null);
        TextView dishTextView = reviewView.findViewById(R.id.tv_review_dish);
        RatingBar ratingBar = reviewView.findViewById(R.id.rb_review_rating);
        EditText contentEditText = reviewView.findViewById(R.id.et_review_content);
        Button cancelButton = reviewView.findViewById(R.id.btn_review_cancel);
        Button submitButton = reviewView.findViewById(R.id.btn_review_submit);

        String name = order.get("name");
        dishTextView.setText(name == null ? "" : name);
        ratingBar.setRating(parseRating(order.get("rating")));
        contentEditText.setText(order.get("reviewContent"));

        cancelButton.setOnClickListener(v -> DialogUtil.dismissDialog());
        submitButton.setOnClickListener(v -> {
            int rating = Math.round(ratingBar.getRating());
            String content = contentEditText.getText().toString().trim();
            if (rating < 1) {
                Toast.makeText(requireContext(), "\u8bf7\u9009\u62e9\u8bc4\u5206", Toast.LENGTH_SHORT).show();
                return;
            }
            showReviewSubmissionConfirmation(order.get("orderId"), name, rating, content);
        });

        DialogUtil.showCustomDialog(requireContext(), reviewView, true);
    }

    private void showReviewSubmissionConfirmation(String orderId,
                                                  String name,
                                                  int rating,
                                                  String content) {
        if (!isAdded()) {
            return;
        }

        View.OnClickListener cancelListener = v -> DialogUtil.dismissDialog();
        View.OnClickListener confirmListener = v -> {
            DialogUtil.dismissDialog();
            submitReview(orderId, rating, content);
        };
        String dishName = TextUtils.isEmpty(name) ? "该订单" : name;
        DialogUtil.showDecideDialogWithTitle(
                requireContext(),
                "确认提交评价",
                dishName + "\n评分：" + rating + " 星，是否继续？",
                "取消",
                cancelListener,
                "确认提交",
                confirmListener
        );
    }

    private int parseRating(String ratingText) {
        try {
            int rating = Integer.parseInt(ratingText);
            return rating < 1 || rating > 5 ? 5 : rating;
        } catch (Exception ignored) {
            return 5;
        }
    }

    private void submitReview(String orderId, int rating, String content) {
        CommonRequest request = new CommonRequest();
        request.addRequestParam("account", Constant.account);
        request.addRequestParam("orderId", orderId == null ? "" : orderId);
        request.addRequestParam("rating", String.valueOf(rating));
        request.addRequestParam("content", content == null ? "" : content);
        sendHttpPostRequest(Constant.URL_Review, request, reviewErrorHandler, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                if (!isAdded()) {
                    return;
                }
                DialogUtil.dismissDialog();
                Toast.makeText(requireContext(), "\u8bc4\u4ef7\u5df2\u4fdd\u5b58", Toast.LENGTH_SHORT).show();
                loadOrderHistory();
            }

            @Override
            public void fail(String failCode, String failMsg) {
                if (!isAdded()) {
                    return;
                }
                DialogUtil.showHintDialog(requireContext(),
                        TextUtils.isEmpty(failMsg) ? "\u8bc4\u4ef7\u63d0\u4ea4\u5931\u8d25" : failMsg,
                        false);
            }
        });
    }

    private void cancelOrder(String orderId) {
        CommonRequest request = new CommonRequest();
        request.addRequestParam("account", Constant.account);
        request.addRequestParam("orderId", orderId == null ? "" : orderId);
        sendHttpPostRequest(Constant.URL_CancelOrder, request, cancelErrorHandler, new ResponseHandler() {
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
                                     Handler errorHandler,
                                     ResponseHandler responseHandler) {
        new HttpPostTask(request, errorHandler, responseHandler).execute(url);
    }

    private Handler createErrorHandler(String message) {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (!isAdded()) {
                    return;
                }
                String errorMsg = msg.obj == null ? "" : msg.obj.toString();
                LogUtil.logErr(errorMsg);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private final Handler orderHistoryErrorHandler = createErrorHandler("获取订单记录失败");
    private final Handler reviewErrorHandler = createErrorHandler("评价提交失败，请检查服务器");
    private final Handler cancelErrorHandler = createErrorHandler("取消订单失败，请重试");
}
