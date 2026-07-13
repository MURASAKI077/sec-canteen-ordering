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
    private final OnOrderActionListener actionListener;

    public OrderAdapter(Activity activity, ArrayList<HashMap<String, String>> orders) {
        this(activity, orders, null);
    }

    public OrderAdapter(Activity activity,
                        ArrayList<HashMap<String, String>> orders,
                        OnOrderActionListener actionListener) {
        this.activity = activity;
        this.orders = orders;
        this.actionListener = actionListener;
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
            holder.review = convertView.findViewById(R.id.tv_order_review);
            holder.cancelButton = convertView.findViewById(R.id.btn_order_cancel);
            holder.reviewButton = convertView.findViewById(R.id.btn_order_review);
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
        holder.review.setText(buildReviewText(order, status));
        bindActionButtons(holder, order, status);
        return convertView;
    }

    private void bindActionButtons(ViewHolder holder, HashMap<String, String> order, String status) {
        boolean canOperate = actionListener != null && !"CANCELLED".equals(status);
        holder.cancelButton.setEnabled(canOperate);
        holder.reviewButton.setEnabled(canOperate);
        holder.cancelButton.setAlpha(canOperate ? 1f : 0.45f);
        holder.reviewButton.setAlpha(canOperate ? 1f : 0.45f);
        holder.reviewButton.setText("评价");

        holder.cancelButton.setOnClickListener(canOperate ? v -> actionListener.onCancelOrder(order) : null);
        holder.reviewButton.setOnClickListener(canOperate ? v -> actionListener.onReviewOrder(order) : null);
    }

    private String buildReviewText(HashMap<String, String> order, String status) {
        if ("CANCELLED".equals(status)) {
            return "已取消订单不可评价";
        }
        if ("1".equals(order.get("reviewed"))) {
            String rating = value(order, "rating");
            String content = value(order, "reviewContent");
            return "已评价：" + rating + "星" + (content.isEmpty() ? "" : " · " + content);
        }
        return "未评价，点击订单可评价";
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
        TextView review;
        TextView cancelButton;
        TextView reviewButton;
    }

    public interface OnOrderActionListener {
        void onCancelOrder(HashMap<String, String> order);

        void onReviewOrder(HashMap<String, String> order);
    }
}
