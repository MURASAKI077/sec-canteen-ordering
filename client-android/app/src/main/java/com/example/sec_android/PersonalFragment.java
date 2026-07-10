package com.example.sec_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * M5 个人中心：显示当前账号、退出登录并承载历史订单列表。
 */
public class PersonalFragment extends Fragment {

    private TextView accountTextView;
    private ListView orderListView;
    private boolean redirectingToLogin;

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

        if (!isLoggedIn()) {
            view.post(this::redirectToLogin);
            return view;
        }

        refreshAccount();
        loadOrderHistory();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accountTextView == null) {
            return;
        }

        if (!isLoggedIn()) {
            redirectToLogin();
            return;
        }

        redirectingToLogin = false;
        refreshAccount();
    }

    private boolean isLoggedIn() {
        return Constant.landing && !TextUtils.isEmpty(Constant.account);
    }

    private void refreshAccount() {
        accountTextView.setText(Constant.account);
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
        Constant.landing = false;
        Constant.account = "";
        redirectToLogin();
    }

    private void redirectToLogin() {
        if (!isAdded() || redirectingToLogin) {
            return;
        }

        redirectingToLogin = true;
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        requireActivity().finish();
    }

    private void loadOrderHistory() {
        /*
         * D 的并行开发分支尚未提供 OrderHistoryHelper。
         * Helper 合入后在这里接入：
         * OrderHistoryHelper.getOrderData(Constant.account, orderListView);
         */
    }
}
