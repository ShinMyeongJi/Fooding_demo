package com.dev.eatit.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.dev.eatit.ItemClickListener;
import com.dev.eatit.R;
import com.dev.eatit.common.Common;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener{

    public TextView text_cart_name, text_cart_price;
    public ImageView cart_image;
    public ElegantNumberButton btnQuantity;

    public RelativeLayout view_background;
    public LinearLayout view_foreground;

    private ItemClickListener itemClickListener;

    public TextView getText_cart_name() {
        return text_cart_name;
    }

    public void setText_cart_name(TextView text_cart_name) {
        this.text_cart_name = text_cart_name;
    }

    public TextView getText_cart_price() {
        return text_cart_price;
    }

    public void setText_cart_price(TextView text_cart_price) {
        this.text_cart_price = text_cart_price;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        text_cart_name = itemView.findViewById(R.id.cart_item_name);
        text_cart_price = itemView.findViewById(R.id.cart_item_price);
        btnQuantity = itemView.findViewById(R.id.btnQuantity);
        cart_image = itemView.findViewById(R.id.cart_image);
        view_background = itemView.findViewById(R.id.view_bg);
        view_foreground = itemView.findViewById(R.id.foreground);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, 0, getAdapterPosition(), Common.Companion.getDELETE());
    }
}

