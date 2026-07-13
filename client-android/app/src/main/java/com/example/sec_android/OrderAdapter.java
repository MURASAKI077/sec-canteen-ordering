package com.example.sec_android;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
//负责把订单数据显示出来
public class OrderAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<HashMap<String, String>> orders;

    public OrderAdapter(Activity activity, ArrayList<HashMap<String, String>> orders) {
        this.activity = activity;
        this.orders = orders;
    }

    @Override
    public int getCount() {
        return orders == null ? 0 : orders.size();
    }

    @Override
    public HashMap<String, String> getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        String orderId = getItem(position).get("orderId");
        try {
            return Long.parseLong(orderId);
        } catch (Exception ignored) {
            return position;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_order, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.tv_order_name);
            holder.priceQuantity = convertView.findViewById(R.id.tv_order_price_quantity);
            holder.location = convertView.findViewById(R.id.tv_order_location);
            holder.status = convertView.findViewById(R.id.tv_order_status);
            holder.time = convertView.findViewById(R.id.tv_order_time);
            holder.orderId = convertView.findViewById(R.id.tv_order_id);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> order = getItem(position);
        String status = order.get("status");
        holder.name.setText(value(order, "name"));
        holder.priceQuantity.setText(value(order, "price") + " 元  x" + value(order, "quantity"));
        holder.location.setText(value(order, "room") + " · " + value(order, "window"));
        holder.status.setText("CANCELLED".equals(status) ? "已取消" : "已下单");
        holder.status.setAlpha("CANCELLED".equals(status) ? 0.55f : 1f);
        holder.time.setText(value(order, "orderTime"));
        holder.orderId.setText("订单 #" + value(order, "orderId"));
        return convertView;
    }

    private String value(HashMap<String, String> order, String key) {
        String value = order.get(key);
        return value == null ? "" : value;
    }

    private static class ViewHolder {
        TextView name;
        TextView priceQuantity;
        TextView location;
        TextView status;
        TextView time;
        TextView orderId;
    }
}
