package com.obeidareda37.foodapp.activities.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.obeidareda37.foodapp.callback.IBestDealListener;
import com.obeidareda37.foodapp.callback.IPopularCallbackListener;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.model.BestDeal;
import com.obeidareda37.foodapp.model.PopularCategory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IPopularCallbackListener, IBestDealListener {

    private MutableLiveData<List<PopularCategory>> popularList;
    private MutableLiveData<List<BestDeal>> bestDealList;
    private MutableLiveData<String> messageError;
    private IPopularCallbackListener popularCallbackListener;
    private IBestDealListener bestDealListener;

    public HomeViewModel() {
        popularCallbackListener = this;
        bestDealListener = this;

    }

    public MutableLiveData<List<PopularCategory>> getPopularList() {
        if (popularList == null) {
            popularList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadPopularList();
        }
        return popularList;
    }

    private void loadPopularList() {
        List<PopularCategory> popularCategoryList = new ArrayList<>();
        DatabaseReference categoryReference = FirebaseDatabase.getInstance().getReference(Common.POPULAR_CATEGORY_REF);
        categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PopularCategory popularCategory = dataSnapshot.getValue(PopularCategory.class);
                    popularCategoryList.add(popularCategory);
                }

                popularCallbackListener.onPopularLoadSuccess(popularCategoryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                popularCallbackListener.onPopularLoadFailed(error.getMessage());

            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<BestDeal>> getBestDealList() {

        if (bestDealList == null){

            bestDealList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadBestDealList();
        }
        return bestDealList;
    }

    private void loadBestDealList() {
        List<BestDeal> bestDealList1 = new ArrayList<>();
        DatabaseReference bestDealReference = FirebaseDatabase.getInstance().getReference(Common.BEST_DEAL_REF);
        bestDealReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    BestDeal bestDeal = dataSnapshot.getValue(BestDeal.class);

                    bestDealList1.add(bestDeal);
                }
                bestDealListener.onBestDealLoadSuccess(bestDealList1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                bestDealListener.onBestDealLoadFailed(error.getMessage());
            }
        });
    }

    @Override
    public void onPopularLoadSuccess(List<PopularCategory> popularCategories) {
        popularList.setValue(popularCategories);
    }

    @Override
    public void onPopularLoadFailed(String message) {
        messageError.setValue(message);

    }

    @Override
    public void onBestDealLoadSuccess(List<BestDeal> bestDeals) {
        bestDealList.setValue(bestDeals);
    }

    @Override
    public void onBestDealLoadFailed(String message) {
        messageError.setValue(message);
    }
}