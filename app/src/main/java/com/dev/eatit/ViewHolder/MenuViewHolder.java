package com.dev.eatit.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.eatit.ItemClickListener;
import com.dev.eatit.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView txtMenuName;
    private ImageView imageView;
    private ItemClickListener itemClickListener;

    public TextView getTxtMenuName() {
        return txtMenuName;
    }

    public void setTxtMenuName(TextView txtMenuName) {
        this.txtMenuName = txtMenuName;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        txtMenuName = itemView.findViewById(R.id.menu_name);
        imageView = itemView.findViewById(R.id.menu_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}


