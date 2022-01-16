package com.obeidareda37.foodapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.callback.IRecyclerClickListener;
import com.obeidareda37.foodapp.eventbus.PopularCategoryClick;
import com.obeidareda37.foodapp.model.PopularCategory;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PopularCategoryAdapter extends RecyclerView.Adapter<PopularCategoryAdapter.popularCategory> {

    Context context;
    List<PopularCategory> popularCategories;

    public PopularCategoryAdapter(Context context, List<PopularCategory> popularCategories) {
        this.context = context;
        this.popularCategories = popularCategories;
    }

    @NonNull
    @Override
    public popularCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new popularCategory(LayoutInflater.from(context).inflate(R.layout.layout_populer_categories_item
                , parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull popularCategory holder, int position) {
        Glide.with(context).load(popularCategories.get(position).getImage())
                .into(holder.circleImageView);
        holder.txt_category_name.setText(popularCategories.get(position).getName());
        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                EventBus.getDefault().postSticky(new PopularCategoryClick(popularCategories.get(pos)));
              //  Toast.makeText(context, "" + popularCategories.get(pos).getName(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return popularCategories.size();
    }

    public class popularCategory extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_category_name;
        CircleImageView circleImageView;
        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public popularCategory(@NonNull View itemView) {
            super(itemView);
            txt_category_name = itemView.findViewById(R.id.txt_category_name);
            circleImageView = itemView.findViewById(R.id.category_image);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());
        }
    }
}
