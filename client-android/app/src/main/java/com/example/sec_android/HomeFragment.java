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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private Button BTsearch;
    private EditText ETsearch;
    private ListView homeList;
    private ProductAdapter dishAdapter;

    public HomeFragment() {
        // Fragment 必须保留空构造函数
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initView(view);
        getListData();

        return view;
    }

    private void initView(View view) {
        BTsearch = view.findViewById(R.id.BTsearch);
        ETsearch = view.findViewById(R.id.ETsearch);
        homeList = view.findViewById(R.id.home_list);

        // 搜索框内容变化时，直接触发 ProductAdapter 的本地过滤
        ETsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 不需要处理
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (dishAdapter != null) {
                    dishAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 不需要处理
            }
        });

        // 保留搜索按钮，点击时也执行一次过滤
        BTsearch.setOnClickListener(v -> {
            if (dishAdapter != null) {
                dishAdapter.getFilter().filter(ETsearch.getText().toString());
            }
        });
    }

    private void getListData() {
        CommonRequest request = new CommonRequest();

        sendHttpPostRequest(Constant.URL_Dish, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                if (!isAdded()) {
                    return;
                }

                if (response.getDataList().size() > 0) {
                    dishAdapter = new ProductAdapter(requireActivity(), response.getDataList());
                    homeList.setAdapter(dishAdapter);
                } else {
                    Toast.makeText(requireContext(), "列表数据为空", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(String failCode, String failMsg) {
                if (!isAdded()) {
                    return;
                }

                Log.e("HomeFragment", "获取菜单失败：" + failCode + " " + failMsg);
                Toast.makeText(requireContext(), "获取菜单失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendHttpPostRequest(String url,
                                     CommonRequest request,
                                     ResponseHandler responseHandler) {
        new HttpPostTask(request, mHandler, responseHandler).execute(url);
        Log.d("HomeFragment", "正在请求菜单数据");
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
                Toast.makeText(requireContext(), "请求发送失败，请重试", Toast.LENGTH_SHORT).show();
            } else if (msg.what == Constant.HANDLER_HTTP_RECEIVE_FAIL) {
                LogUtil.logErr(errorMsg);
                Toast.makeText(requireContext(), "获取菜单失败", Toast.LENGTH_SHORT).show();
            }
        }
    };
}