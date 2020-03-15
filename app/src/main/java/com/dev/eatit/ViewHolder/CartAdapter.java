package com.dev.eatit.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.dev.eatit.ItemClickListener;
import com.dev.eatit.R;
import com.dev.eatit.model.Order;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView text_cart_name, text_cart_price;
    public ImageView img_cart_count;

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

    public ImageView getImg_cart_count() {
        return img_cart_count;
    }

    public void setImg_cart_count(ImageView img_cart_count) {
        this.img_cart_count = img_cart_count;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        text_cart_name = itemView.findViewById(R.id.cart_item_name);
        text_cart_price = itemView.findViewById(R.id.cart_item_price);
        img_cart_count = itemView.findViewById(R.id.cart_item_count);
    }

    @Override
    public void onClick(View v) {

    }
}

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData = new ArrayList<>();
    private Context context;

    public CartAdapter(List<Order> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.cart_layout, parent, false);

        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        TextDrawable drawable = TextDrawable.builder()
                .buildRound("" + listData.get(position).getQuantity(), Color.RED);
        holder.img_cart_count.setImageDrawable(drawable);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.KOREA);
        int price = (Integer.parseInt(listData.get(position).getPrice()))*(Integer.parseInt(listData.get(position).getQuantity()));

        holder.text_cart_price.setText(format.format(price));
        holder.text_cart_name.setText(listData.get(position).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}