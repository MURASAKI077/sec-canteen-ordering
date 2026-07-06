package com.example.sec_android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.sec_android.DialogUtil.showDecideDialogNoTitle;
import static com.example.sec_android.DialogUtil.showDecideDialogWithTitle;

public class ViewPagerFragment extends Fragment {

    private Button BTsearch;
    private EditText ETsearch;
    private TextView PersonalExit;
    private TextView PersonalAccount;
    private FragmentActivity FAct;

    private ListView homeList;
    private ListView orderList;

    private ProductAdapter dishAdapter;
    private ProductAdapter orderAdapter;


    private static final String KEY = "extra";
    private String mMessage;
    public ViewPagerFragment() { }
    public static ViewPagerFragment newInstance(String extra) {
        Bundle args = new Bundle();
        args.putString(KEY, extra);
        ViewPagerFragment fragment = new ViewPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMessage = bundle.getString(KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        //PersonalAccount.setText(Constant.account);

        View view=null;
        if(mMessage=="菜单首页"){
            //Toast.makeText(getActivity(),"显示文字="+mMessage,Toast.LENGTH_LONG).show();
            view = inflater.inflate(R.layout.fragment_home, container, false);
            BTsearch = view.findViewById(R.id.BTsearch);
            ETsearch = view.findViewById(R.id.ETsearch);
            homeList = view.findViewById(R.id.home_list);
            BTsearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(String.valueOf(getActivity()),"搜素");
                    String searchText = ETsearch.getText().toString();
                    Toast.makeText(getActivity(),searchText,Toast.LENGTH_SHORT).show();
                }
            });
            getListData();



        }
        else if(mMessage=="个人中心"){
            //Toast.makeText(getActivity(),"显示文字="+mMessage,Toast.LENGTH_LONG).show();
            view = inflater.inflate(R.layout.fragment_personal, container, false);
            PersonalAccount = view.findViewById(R.id.tv_personal_account);
            PersonalExit = view.findViewById(R.id.tv_personal_exit);
            orderList = view.findViewById(R.id.List_myOrder);
            Log.d("个人中心",""+Constant.account);
            PersonalAccount.setText(Constant.account);
            PersonalExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(String.valueOf(getActivity()),"退出登录");
                    Constant.landing = false;
                    Constant.account = "";
                    Intent intent = new Intent(FAct,LoginActivity.class);
                    startActivity(intent);
                }
            });
            getOrderData();
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("Fragment创建","message="+mMessage);
        FAct = getActivity();
    }

    private void getOrderData(){
        final CommonRequest request = new CommonRequest();
        request.addRequestParam("account",Constant.account);
        sendHttpPostRequest(Constant.URL_OrderRecord, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                LoadingDialogUtil.cancelLoading();
                if (response.getDataList().size() > 0) {
                    orderAdapter = new ProductAdapter(FAct, response.getDataList());
                    orderList.setAdapter(orderAdapter);
                    orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DialogUtil dialog = new DialogUtil();
                            View.OnClickListener cancel = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismissDialog();
                                }
                            };

                            TextView tvRoom= (TextView) view.findViewById(R.id.tv_room);
                            String room = tvRoom.getText().toString();
                            TextView tvWindow= (TextView) view.findViewById(R.id.tv_window);
                            String window = tvWindow.getText().toString();
                            TextView tvName= (TextView) view.findViewById(R.id.tv_name);
                            String name = tvName.getText().toString();
                            TextView tvPrice= (TextView) view.findViewById(R.id.tv_price);
                            String price = tvPrice.getText().toString();

                            View.OnClickListener confirm = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismissDialog();
                                    if(Constant.landing){
                                        order(room,window,name,price);
                                    }
                                    else{
                                        DialogUtil.showHintDialog(FAct, "请先登录", false);
                                    }


                                }
                            };

                            dialog.showDecideDialogWithTitle(FAct, "是否确认下单", name+"："+price+"元", cancel, confirm);

                        }
                    });

                } else {

                }
            }

            @Override
            public void fail(String failCode, String failMsg) {
                Log.e("getListData","获取订单失败");
            }
        }, true);
    }

    private void getListData() {
        CommonRequest request = new CommonRequest();
        sendHttpPostRequest(Constant.URL_Dish, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                LoadingDialogUtil.cancelLoading();
                if (response.getDataList().size() > 0) {
                    dishAdapter = new ProductAdapter(FAct, response.getDataList());
                    homeList.setAdapter(dishAdapter);

                    ETsearch.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                            // When user change the text
                            Log.d("搜索框改变",cs.toString());
                            dishAdapter.getFilter().filter(cs);
                        }
                        @Override
                        public void beforeTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                            //
                        }
                        @Override
                        public void afterTextChanged(Editable arg0) {
                            //
                        }
                    });

                    homeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DialogUtil dialog = new DialogUtil();
                            View.OnClickListener cancel = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismissDialog();
                                }
                            };

                            TextView tvRoom= (TextView) view.findViewById(R.id.tv_room);
                            String room = tvRoom.getText().toString();
                            TextView tvWindow= (TextView) view.findViewById(R.id.tv_window);
                            String window = tvWindow.getText().toString();
                            TextView tvName= (TextView) view.findViewById(R.id.tv_name);
                            String name = tvName.getText().toString();
                            TextView tvPrice= (TextView) view.findViewById(R.id.tv_price);
                            String price = tvPrice.getText().toString();

                            View.OnClickListener confirm = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismissDialog();
                                    if(Constant.landing){
                                        order(room,window,name,price);
                                    }
                                    else{
                                        DialogUtil.showHintDialog(FAct, "请先登录", false);
                                    }


                                }
                            };

                            dialog.showDecideDialogWithTitle(FAct, "是否确认下单", name+"："+price+"元", cancel, confirm);

                        }
                    });

                } else {
                    DialogUtil.showHintDialog(FAct, "列表数据为空", false);
                }

            }

            @Override
            public void fail(String failCode, String failMsg) {
                Log.e("getListData","获取菜单失败");
            }
        }, true);


    }

    protected void sendHttpPostRequest(String url, CommonRequest request, ResponseHandler responseHandler, boolean showLoadingDialog) {
        new HttpPostTask(request, mHandler, responseHandler).execute(url);
        if(showLoadingDialog) {
            Log.d("sendPostRequest","正在发送……");
        }
    }

    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == Constant.HANDLER_HTTP_SEND_FAIL) {
                LogUtil.logErr(msg.obj.toString());

                Log.d("mHandler", "请求发送失败，请重试");
            } else if (msg.what == Constant.HANDLER_HTTP_RECEIVE_FAIL) {
                LogUtil.logErr(msg.obj.toString());

                Log.d("mHandler", "请求接受失败，请重试");
            }
        }
    };

    static class ProductAdapter extends BaseAdapter implements Filterable {
        private Context context;
        private ArrayList<HashMap<String, String>> list;
        private final Object mLock = new Object();
        private ArrayList<HashMap<String, String>> mOriginalValues;
        private ArrayFilter mFilter;


        public ProductAdapter(Context context, ArrayList<HashMap<String, String>> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_dish, parent, false);
                holder = new ViewHolder();
                holder.tvRoom = (TextView) convertView.findViewById(R.id.tv_room);
                holder.tvWindow = (TextView) convertView.findViewById(R.id.tv_window);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.tvPrice = (TextView) convertView.findViewById(R.id.tv_price);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, String> map = list.get(position);
            holder.tvRoom.setText(map.get("room"));
            holder.tvWindow.setText(map.get("window"));
            holder.tvName.setText(map.get("name"));
            holder.tvPrice.setText(map.get("price"));

            return convertView;
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new ArrayFilter();
            }
            return mFilter;
        }
        private class ArrayFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();

                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        mOriginalValues = new ArrayList<> (list);
                    }
                }

                if (prefix == null || prefix.length() == 0) {
                    ArrayList<HashMap<String, String>> list1;
                    synchronized (mLock) {
                        list1 = new ArrayList<>(mOriginalValues);
                    }
                    results.values = list1;
                    results.count = list1.size();
                } else {
                    String prefixString = prefix.toString().toLowerCase();

                    ArrayList<HashMap<String, String>> values;
                    synchronized (mLock) {
                        values = new ArrayList<>(mOriginalValues);
                    }

                    final int count = values.size();
                    final ArrayList<HashMap<String, String>> newValues = new ArrayList<HashMap<String, String>>();

                    for (int i = 0; i < count; i++) {
                        final HashMap<String, String> value = values.get(i);
                        final String valueText = value.get("name").toString().toLowerCase();//User对象的name属性作为过滤的参数

                        // First match against the whole, non-splitted value
                        if (valueText.startsWith(prefixString) || valueText.indexOf(prefixString.toString()) != -1) {//第一个字符是否匹配
                            newValues.add(value);//将这个item加入到数组对象中
                        } else {//处理首字符是空格
                            final String[] words = valueText.split(" ");
                            final int wordCount = words.length;

                            // Start at index 0, in case valueText starts with space(s)
                            for (int k = 0; k < wordCount; k++) {
                                if (words[k].startsWith(prefixString)) {//一旦找到匹配的就break，跳出for循环
                                    newValues.add(value);
                                    break;
                                }
                            }
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                list = (ArrayList<HashMap<String, String>>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }

        private static class ViewHolder {
            private TextView tvRoom;
            private TextView tvWindow;
            private TextView tvName;
            private TextView tvPrice;
        }
    }

    private void order(String room,String window,String name, String price){

        final CommonRequest request = new CommonRequest();
        request.addRequestParam("room", room);
        request.addRequestParam("window", window);
        request.addRequestParam("name", name);
        request.addRequestParam("price", price);
        request.addRequestParam("account",Constant.account);

        sendHttpPostRequest(Constant.URL_Order, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                LoadingDialogUtil.cancelLoading();
                Log.d("order", "接收response成功！");

            }

            @Override
            public void fail(String failCode, String failMsg) {
                LoadingDialogUtil.cancelLoading();
                Log.d("order", "接收response失败，请重试");
            }
        }, true);
        FAct.recreate();
    }





}