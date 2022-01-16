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
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.database.CartItem;
import com.obeidareda37.foodapp.eventbus.UpdateItemInCart;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartItemList;

    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CartViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_cart_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Glide.with(context).load(cartItemList.get(position).getFoodImage()).into(holder.imageCart);
        holder.textFoodName.setText(new StringBuilder(cartItemList.get(position).getFoodName()));
        holder.textFoodPrice.setText(new StringBuilder("")
                .append(cartItemList.get(position).getFoodPrice() +
                        cartItemList.get(position).getFoodExtraPrice()));
        holder.numberButton.setNumber(String.valueOf(cartItemList.get(position).getFoodQuantity()));

        //Event
        holder.numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                //when user click this button , we will update database
                cartItemList.get(position).setFoodQuantity(newValue);
                EventBus.getDefault().postSticky(new UpdateItemInCart(cartItemList.get(position)));
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public CartItem getItemAtPosition(int pos) {
        return cartItemList.get(pos);
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCart;
        TextView textFoodName;
        TextView textFoodPrice;
        ElegantNumberButton numberButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCart = itemView.findViewById(R.id.img_cart);
            textFoodName = itemView.findViewById(R.id.text_food_name);
            textFoodPrice = itemView.findViewById(R.id.text_food_price1);
            numberButton = itemView.findViewById(R.id.number_button);
        }
    }
}
