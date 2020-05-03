package com.dev.eatit.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.eatit.ItemClickListener;
import com.dev.eatit.R;

public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtRestaurantName;
    public ImageView restaurantImage;
    private ItemClickListener itemClickListener;

    public RestaurantViewHolder(@NonNull View itemView) {
        super(itemView);
        txtRestaurantName = itemView.findViewById(R.id.restaurant_name);
        restaurantImage = itemView.findViewById(R.id.restaurant_image);
        itemView.setOnClickListener(this);
    }


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}


