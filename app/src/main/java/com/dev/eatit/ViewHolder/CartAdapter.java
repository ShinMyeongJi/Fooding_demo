package com.dev.eatit.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.dev.eatit.Cart;
import com.dev.eatit.ItemClickListener;
import com.dev.eatit.R;
import com.dev.eatit.common.Common;
import com.dev.eatit.database.Database;
import com.dev.eatit.model.Order;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener{

    public TextView text_cart_name, text_cart_price;
    public ImageView cart_image;
    public ElegantNumberButton btnQuantity;

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

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView = inflater.inflate(R.layout.cart_layout, parent, false);

        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        //TextDrawable drawable = TextDrawable.builder()
        //        .buildRound("" + listData.get(position).getQuantity(), Color.RED);
        //holder.img_cart_count.setImageDrawable(drawable);
        Locale locale = new Locale("ko", "KO");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.KOREA);

        holder.btnQuantity.setNumber(listData.get(position).getQuantity());
        holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                int total = 0;
                List<Order> orders = new Database(cart).getCart(Common.currentUser.getPhone());
                for(Order orderItem : orders){
                    total += (Integer.parseInt(orderItem.getPrice()) * Integer.parseInt(orderItem.getQuantity()));
                }

                int price = (Integer.parseInt(listData.get(position).getPrice()))*(Integer.parseInt(listData.get(position).getQuantity()));

                holder.text_cart_price.setText(fmt.format(price));
                cart.txtTotal.setText(fmt.format(total));
            }
        });


        holder.text_cart_price.setText(fmt.format((Integer.parseInt(listData.get(position).getPrice()))*(Integer.parseInt(listData.get(position).getQuantity()))));
        holder.text_cart_name.setText(listData.get(position).getProductName());
        Picasso.get()
                .load(listData.get(position).getImage())
                .resize(70, 70)
                .centerCrop()
                .into(holder.cart_image);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}