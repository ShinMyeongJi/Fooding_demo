package com.dev.eatit.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import com.dev.eatit.ItemClickListener;
import com.dev.eatit.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView order_id, order_status, order_phone, order_address;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        order_id = itemView.findViewById(R.id.order_id);
        order_status = itemView.findViewById(R.id.order_status);
        order_phone = itemView.findViewById(R.id.order_phone);
        order_address = itemView.findViewById(R.id.order_address);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }

    public TextView getOrder_id() {
        return order_id;
    }

    public void setOrder_id(TextView order_id) {
        this.order_id = order_id;
    }

    public TextView getOrder_status() {
        return order_status;
    }

    public void setOrder_status(TextView order_status) {
        this.order_status = order_status;
    }

    public TextView getOrder_phone() {
        return order_phone;
    }

    public void setOrder_phone(TextView order_phone) {
        this.order_phone = order_phone;
    }

    public TextView getOrder_address() {
        return order_address;
    }

    public void setOrder_address(TextView order_address) {
        this.order_address = order_address;
    }
}
