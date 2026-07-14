package com.example.sec_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String CATEGORY_ALL = "全部";
    private static final String[] CATEGORIES = {
            CATEGORY_ALL, "主食套餐", "粉面小吃", "饮品甜点", "清真风味", "家常小炒"
    };

    private Button searchButton;
    private EditText searchInput;
    private LinearLayout categoryLayout;
    private ListView dishListView;
    private ProductAdapter dishAdapter;
    private java.util.ArrayList<java.util.HashMap<String, String>> allDishes = new java.util.ArrayList<>();
    private String selectedCategory = CATEGORY_ALL;

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
        categoryLayout = view.findViewById(R.id.layout_dish_categories);
        dishListView = view.findViewById(R.id.home_list);

        searchButton.setOnClickListener(v -> searchDishes());
        searchInput.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                searchDishes();
                return true;
            }
            return false;
        });

        buildCategoryTabs();
    }

    private void loadDishList() {
        loadDishList("");
    }

    private void searchDishes() {
        applyDishFilters();
    }

    private void loadDishList(String keyword) {
        LoadingDialogUtil.showLoadingDialog(requireContext());
        CommonRequest request = new CommonRequest();
        request.addRequestParam("keyword", keyword);
        sendHttpPostRequest(Constant.URL_Dish, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                if (!isAdded()) {
                    return;
                }

                LoadingDialogUtil.cancelLoading();
                allDishes = response.getDataList();
                applyDishFilters();

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

    private void buildCategoryTabs() {
        if (categoryLayout == null || !isAdded()) {
            return;
        }

        categoryLayout.removeAllViews();
        for (String category : CATEGORIES) {
            TextView tab = new TextView(requireContext());
            tab.setText(category);
            tab.setGravity(android.view.Gravity.CENTER);
            tab.setTextSize(15);
            tab.setMinWidth(dp(84));
            tab.setPadding(dp(14), 0, dp(14), 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(34)
            );
            params.setMarginEnd(dp(8));
            tab.setLayoutParams(params);
            tab.setOnClickListener(v -> {
                selectedCategory = category;
                buildCategoryTabs();
                applyDishFilters();
            });
            styleCategoryTab(tab, category.equals(selectedCategory));
            categoryLayout.addView(tab);
        }
    }

    private void styleCategoryTab(TextView tab, boolean selected) {
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(17));
        background.setColor(selected ? Color.rgb(76, 175, 80) : Color.argb(220, 255, 255, 255));
        background.setStroke(dp(1), selected ? Color.rgb(76, 175, 80) : Color.rgb(205, 230, 210));
        tab.setBackground(background);
        tab.setTextColor(selected ? Color.WHITE : Color.rgb(46, 125, 50));
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void applyDishFilters() {
        if (dishListView == null || !isAdded()) {
            return;
        }

        String keyword = searchInput == null ? "" : searchInput.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        java.util.ArrayList<java.util.HashMap<String, String>> filtered = new java.util.ArrayList<>();
        for (java.util.HashMap<String, String> dish : allDishes) {
            if (!CATEGORY_ALL.equals(selectedCategory) && !selectedCategory.equals(categoryOf(dish))) {
                continue;
            }
            if (!keyword.isEmpty()
                    && !value(dish, "name").toLowerCase(java.util.Locale.ROOT).contains(keyword)
                    && !value(dish, "window").toLowerCase(java.util.Locale.ROOT).contains(keyword)
                    && !value(dish, "room").toLowerCase(java.util.Locale.ROOT).contains(keyword)) {
                continue;
            }
            filtered.add(dish);
        }

        dishAdapter = new ProductAdapter(requireActivity(), filtered, new ProductAdapter.OnDishActionListener() {
            @Override
            public void onOrderDish(java.util.HashMap<String, String> dish) {
                showOrderConfirmation(dish);
            }

            @Override
            public void onViewReviews(java.util.HashMap<String, String> dish) {
                loadDishReviews(dish);
            }
        });
        dishListView.setAdapter(dishAdapter);
    }

    private String categoryOf(java.util.HashMap<String, String> dish) {
        String text = value(dish, "name") + " " + value(dish, "window") + " " + value(dish, "room");
        if (containsAny(text, "牛奶", "鲜果", "甜点", "蛋糕", "菠萝包", "肉松卷")) {
            return "饮品甜点";
        }
        if (text.contains("清真")) {
            return "清真风味";
        }
        if (containsAny(text, "烤冷面", "小面", "米粉", "素粉", "水饺", "煎包", "煎饺", "粥")) {
            return "粉面小吃";
        }
        if (containsAny(text, "土豆丝", "毛血旺", "馒头", "花卷", "窝头", "绿豆芽", "包菜", "油菜", "基本伙", "大碗炖菜")) {
            return "家常小炒";
        }
        return "主食套餐";
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showOrderConfirmation(java.util.HashMap<String, String> dish) {
        if (!isAdded()) {
            return;
        }
        if (!Constant.landing) {
            DialogUtil.showHintDialog(requireContext(), "\u8bf7\u5148\u767b\u5f55", false);
            return;
        }

        String room = value(dish, "room");
        String window = value(dish, "window");
        String name = value(dish, "name");
        String price = value(dish, "price");

        View.OnClickListener cancelListener = v -> DialogUtil.dismissDialog();
        View.OnClickListener confirmListener = v -> {
            DialogUtil.dismissDialog();
            PayActivity.start(requireActivity(), room, window, name, price);
        };

        DialogUtil.showDecideDialogWithTitle(
                requireContext(),
                "\u662f\u5426\u786e\u8ba4\u4e0b\u5355",
                name + "\uff0c" + price + "\u5143",
                cancelListener,
                confirmListener
        );
    }

    private void loadDishReviews(java.util.HashMap<String, String> dish) {
        if (!isAdded()) {
            return;
        }

        CommonRequest request = new CommonRequest();
        request.addRequestParam("dishId", value(dish, "dishId"));
        sendHttpPostRequest(Constant.URL + "DishReviewServlet", request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                if (!isAdded()) {
                    return;
                }
                DialogUtil.showHintDialogWithTitle(
                        requireContext(),
                        value(dish, "name") + "的评价",
                        buildReviewText(response),
                        false
                );
            }

            @Override
            public void fail(String failCode, String failMsg) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "获取评价失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildReviewText(CommonResponse response) {
        if (response.getDataList().isEmpty()) {
            return "暂无评价";
        }

        StringBuilder builder = new StringBuilder();
        for (java.util.HashMap<String, String> item : response.getDataList()) {
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append("评价 #")
                    .append(value(item, "reviewId"))
                    .append("  ")
                    .append(value(item, "account"))
                    .append("：")
                    .append(value(item, "rating"))
                    .append("星")
                    .append("\n")
                    .append(value(item, "content"));
        }
        return builder.toString();
    }

    private String value(java.util.HashMap<String, String> map, String key) {
        String value = map.get(key);
        return value == null ? "" : value;
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
