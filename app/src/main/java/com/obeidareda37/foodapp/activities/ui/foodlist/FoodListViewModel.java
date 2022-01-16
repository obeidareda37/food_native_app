package com.obeidareda37.foodapp.activities.ui.foodlist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.model.FoodModel;

import java.util.List;

public class FoodListViewModel extends ViewModel {

    private MutableLiveData<List<FoodModel>> mutableLiveDataFood;

    public FoodListViewModel() {
    }

    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList(){
        if (mutableLiveDataFood == null){
            mutableLiveDataFood = new MutableLiveData<>();
        }
        mutableLiveDataFood.setValue(Common.categorySelected.getFoods());
        return mutableLiveDataFood;
    }


}