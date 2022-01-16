package com.obeidareda37.foodapp.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.model.CommentModel;

import java.util.List;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    Context context;
    List<CommentModel> commentModels;

    public CommentAdapter(Context context, List<CommentModel> commentModels) {
        this.context = context;
        this.commentModels = commentModels;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_comment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        Long timeStamp = Long.valueOf(commentModels.get(position).getServerTimeStamp().get("timeStamp").toString());
        holder.textCommentDate.setText(DateUtils.getRelativeTimeSpanString(timeStamp));
        holder.textCommentName.setText(commentModels.get(position).getName());
        holder.textComment.setText(commentModels.get(position).getComment());
        holder.ratingBar.setRating(commentModels.get(position).getRatingValue());

    }

    @Override
    public int getItemCount() {
        return commentModels.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView textCommentName;
        TextView textCommentDate;
        TextView textComment;
        RatingBar ratingBar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textCommentName = itemView.findViewById(R.id.text_comment_name);
            textCommentDate = itemView.findViewById(R.id.text_comment_date);
            textComment = itemView.findViewById(R.id.text_comment);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
