package com.obeidareda37.foodapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.eventbus.CategoryClick;
import com.obeidareda37.foodapp.callback.IRecyclerClickListener;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.model.CategoryModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;



public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {
    Context context;
    List<CategoryModel> categoryList;

    public CategoriesAdapter(Context context, List<CategoryModel> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoriesViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder holder, int position) {
        Glide.with(context).load(categoryList.get(position).getImage()).into(holder.imageCategory);
        holder.nameCategory.setText(new StringBuilder(categoryList.get(position).getName()));

        //Event
        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                Common.categorySelected = categoryList.get(pos);
                EventBus.getDefault().postSticky(new CategoryClick(true, categoryList.get(pos)));
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class CategoriesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageCategory;
        TextView nameCategory;
        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public CategoriesViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCategory = itemView.findViewById(R.id.image_category);
            nameCategory = itemView.findViewById(R.id.text_category);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            listener.onItemClickListener(v, getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (categoryList.size() == 1) {
            return Common.DEFAULT_COLUMN_COUNT;
        } else {
            if (categoryList.size() % 2 == 0) {
                return Common.DEFAULT_COLUMN_COUNT;
            } else {
                return (position > 1 && position == categoryList.size() - 1) ? Common.FULL_WIDTH_COLUMN : Common.DEFAULT_COLUMN_COUNT;
            }
        }

    }
}
