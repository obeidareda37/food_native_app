package com.obeidareda37.foodapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.callback.IRecyclerClickListener;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.database.CartDataSource;
import com.obeidareda37.foodapp.database.CartDatabase;
import com.obeidareda37.foodapp.database.CartItem;
import com.obeidareda37.foodapp.database.LocalCartDataSource;
import com.obeidareda37.foodapp.eventbus.CounterCartEven;
import com.obeidareda37.foodapp.eventbus.FoodItemClick;
import com.obeidareda37.foodapp.model.FoodModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder> {
    private Context context;
    private List<FoodModel> foodModelList;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public FoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    @NonNull
    @Override
    public FoodListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FoodListViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_food_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FoodListViewHolder holder, int position) {
        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.imageFood);
        holder.textPrice.setText(new StringBuilder("$")
                .append(foodModelList.get(position).getPrice()));
        holder.textName.setText(new StringBuilder("").
                append(foodModelList.get(position).getName()));

        //Event
        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                Common.selectFood = foodModelList.get(pos);
                Common.selectFood.setKey(String.valueOf(pos));
                EventBus.getDefault().postSticky(new FoodItemClick(true, foodModelList.get(pos)));
            }
        });

        holder.imageCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartItem cartItem = new CartItem();
                cartItem.setUid(Common.currentUser.getUid());
                cartItem.setUserPhone(Common.currentUser.getPhone());
                cartItem.setFoodId(foodModelList.get(position).getId());
                cartItem.setFoodName(foodModelList.get(position).getName());
                cartItem.setFoodImage(foodModelList.get(position).getImage());
                cartItem.setFoodPrice(Double.valueOf(foodModelList.get(position).getPrice()));
                cartItem.setFoodQuantity(1);
                cartItem.setFoodExtraPrice(0.00); // because default we not choose size + addon so extra price is 0.
                cartItem.setFoodAddon("Default");
                cartItem.setFoodSize("Default");

                cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                        cartItem.getFoodId(),
                        cartItem.getFoodSize(),
                        cartItem.getFoodAddon())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<CartItem>() {
                            @Override
                            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull CartItem cartItemFromDB) {

                                if (cartItemFromDB.equals(cartItem)) {

                                    //Already in database , just update
                                    cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                                    cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                                    cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                                    cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity()
                                            + cartItem.getFoodQuantity());
                                    cartDataSource.updateCartItems(cartItemFromDB)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new SingleObserver<Integer>() {
                                                @Override
                                                public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                                }

                                                @Override
                                                public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {

                                                    Toast.makeText(context, "Update Cart success", Toast.LENGTH_SHORT).show();
                                                    EventBus.getDefault().postSticky(new CounterCartEven(true));

                                                }

                                                @Override
                                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                                    Toast.makeText(context, "[CART UPDATE]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                } else {
                                    //Item not available in cart before , insert new
                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(() -> {
                                                Toast.makeText(context, "Add to Cart success", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEven(true));
                                            }, Throwable -> {
                                                Toast.makeText(context, "[CART ERROR]" + Throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));
                                }
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                if (e.getMessage().contains("empty")) {
                                    //Default , if Cart is empty , this code will be fired
                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(() -> {
                                                Toast.makeText(context, "Add to Cart success", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEven(true));
                                            }, Throwable -> {
                                                Toast.makeText(context, "[CART ERROR]" + Throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));
                                } else {
                                    Toast.makeText(context, "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });

    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public class FoodListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textName;
        TextView textPrice;
        ImageView imageFood;
        ImageView imageFavorite;
        ImageView imageCart;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public FoodListViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_food_name);
            textPrice = itemView.findViewById(R.id.text_food_price);
            imageFood = itemView.findViewById(R.id.image_food_list);
            imageFavorite = itemView.findViewById(R.id.image_favorite);
            imageCart = itemView.findViewById(R.id.image_cart);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());
        }
    }
}
