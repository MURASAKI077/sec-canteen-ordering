package com.example.sec_android;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ProductAdapter extends BaseAdapter implements Filterable {

    private final Activity activity;
    private ArrayList<HashMap<String, String>> list;

    // 保存原始列表，搜索过滤时需要从原始数据中筛选
    private ArrayList<HashMap<String, String>> originalValues;

    private final Object lock = new Object();
    private ArrayFilter filter;

    public ProductAdapter(Activity activity, ArrayList<HashMap<String, String>> list) {
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 把一条菜品/订单数据绑定到 item_dish.xml
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_dish, parent, false);

            holder = new ViewHolder();
            holder.tvRoom = convertView.findViewById(R.id.tv_room);
            holder.tvWindow = convertView.findViewById(R.id.tv_window);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            holder.tvPrice = convertView.findViewById(R.id.tv_price);

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
        if (filter == null) {
            filter = new ArrayFilter();
        }
        return filter;
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (originalValues == null) {
                synchronized (lock) {
                    originalValues = new ArrayList<>(list);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<HashMap<String, String>> values;
                synchronized (lock) {
                    values = new ArrayList<>(originalValues);
                }

                results.values = values;
                results.count = values.size();
                return results;
            }

            String keyword = prefix.toString().toLowerCase(Locale.ROOT);
            ArrayList<HashMap<String, String>> values;

            synchronized (lock) {
                values = new ArrayList<>(originalValues);
            }

            ArrayList<HashMap<String, String>> newValues = new ArrayList<>();

            // 按菜品名称做本地搜索
            for (HashMap<String, String> value : values) {
                String name = value.get("name");
                if (name == null) {
                    continue;
                }

                String lowerName = name.toLowerCase(Locale.ROOT);
                if (lowerName.startsWith(keyword) || lowerName.contains(keyword)) {
                    newValues.add(value);
                }
            }

            results.values = newValues;
            results.count = newValues.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
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
