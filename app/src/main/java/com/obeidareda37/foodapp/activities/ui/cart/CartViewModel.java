package com.obeidareda37.foodapp.activities.ui.cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.database.CartDataSource;
import com.obeidareda37.foodapp.database.CartDatabase;
import com.obeidareda37.foodapp.database.CartItem;
import com.obeidareda37.foodapp.database.LocalCartDataSource;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private MutableLiveData<List<CartItem>> mutableLiveDataCart;
    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable;

    public CartViewModel() {
        compositeDisposable = new CompositeDisposable();

    }

    public void initCartDataSource(Context context){
        cartDataSource =new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());

    }
    public void onStop(){
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCart() {
        if (mutableLiveDataCart == null){
            mutableLiveDataCart = new MutableLiveData<>();
        }
        getAllCartItems();
        return mutableLiveDataCart;
    }

    private void getAllCartItems() {
    compositeDisposable.add(cartDataSource.getAllCarts(Common.currentUser.getUid())
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Consumer<List<CartItem>>() {
        @Override
        public void accept(List<CartItem> cartItems) throws Exception {
            mutableLiveDataCart.setValue(cartItems);

        }
    }, throwable -> mutableLiveDataCart.setValue(null)));
    }
}