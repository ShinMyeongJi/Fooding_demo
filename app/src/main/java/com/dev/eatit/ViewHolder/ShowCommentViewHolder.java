package com.dev.eatit.ViewHolder;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.eatit.R;

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtUserPhone, txtComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(@NonNull View itemView) {
        super(itemView);
        this.ratingBar = itemView.findViewById(R.id.ratingOfComment);
        this.txtUserPhone = itemView.findViewById(R.id.txtUserPhone);
        this.txtComment = itemView.findViewById(R.id.txtComment);
    }
}
