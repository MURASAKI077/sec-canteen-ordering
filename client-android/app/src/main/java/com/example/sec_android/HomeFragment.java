package com.example.sec_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private Button searchButton;
    private EditText searchInput;
    private ListView dishListView;
    private ProductAdapter dishAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initView(view);
        loadDishList();
        return view;
    }

    private void initView(View view) {
        searchButton = view.findViewById(R.id.BTsearch);
        searchInput = view.findViewById(R.id.ETsearch);
        dishListView = view.findViewById(R.id.home_list);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (dishAdapter != null) {
                    dishAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchButton.setOnClickListener(v -> {
            if (dishAdapter != null) {
                dishAdapter.getFilter().filter(searchInput.getText().toString());
            }
        });

        dishListView.setOnItemClickListener((parent, itemView, position, id) -> showOrderConfirmation(itemView));
    }

    private void loadDishList() {
        CommonRequest request = new CommonRequest();
        sendHttpPostRequest(Constant.URL_Dish, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                if (!isAdded()) {
                    return;
                }

                LoadingDialogUtil.cancelLoading();
                dishAdapter = new ProductAdapter(requireActivity(), response.getDataList());
                dishListView.setAdapter(dishAdapter);

                if (response.getDataList().isEmpty()) {
                    Toast.makeText(requireContext(), "\u5217\u8868\u6682\u65e0\u6570\u636e", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(String failCode, String failMsg) {
                if (!isAdded()) {
                    return;
                }

                LoadingDialogUtil.cancelLoading();
                Log.e(TAG, "load dishes failed: " + failCode + " " + failMsg);
                Toast.makeText(requireContext(), "\u83b7\u53d6\u83dc\u5355\u5931\u8d25", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOrderConfirmation(View itemView) {
        if (!isAdded()) {
            return;
        }
        if (!Constant.landing) {
            DialogUtil.showHintDialog(requireContext(), "\u8bf7\u5148\u767b\u5f55", false);
            return;
        }

        String room = getText(itemView, R.id.tv_room);
        String window = getText(itemView, R.id.tv_window);
        String name = getText(itemView, R.id.tv_name);
        String price = getText(itemView, R.id.tv_price);

        View.OnClickListener cancelListener = v -> DialogUtil.dismissDialog();
        View.OnClickListener confirmListener = v -> {
            DialogUtil.dismissDialog();
            OrderHelper.order(requireActivity(), mHandler, room, window, name, price, Constant.account, null);
        };

        DialogUtil.showDecideDialogWithTitle(
                requireContext(),
                "\u662f\u5426\u786e\u8ba4\u4e0b\u5355",
                name + "\uff0c" + price + "\u5143",
                cancelListener,
                confirmListener
        );
    }

    private String getText(View itemView, int viewId) {
        TextView textView = itemView.findViewById(viewId);
        return textView == null ? "" : textView.getText().toString();
    }

    private void sendHttpPostRequest(String url,
                                     CommonRequest request,
                                     ResponseHandler responseHandler) {
        new HttpPostTask(request, mHandler, responseHandler).execute(url);
        Log.d(TAG, "requesting dish list");
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
                Toast.makeText(requireContext(), "\u83b7\u53d6\u83dc\u5355\u5931\u8d25", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
