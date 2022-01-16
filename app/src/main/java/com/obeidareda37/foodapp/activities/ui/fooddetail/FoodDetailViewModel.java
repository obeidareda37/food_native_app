package com.obeidareda37.foodapp.activities.ui.fooddetail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.model.CommentModel;
import com.obeidareda37.foodapp.model.FoodModel;

public class FoodDetailViewModel extends ViewModel {

    private MutableLiveData<FoodModel> foodModelMutableLiveData;
    private MutableLiveData<CommentModel> commentModelMutableLiveData;


    public void setCommentModelMutableLiveData(CommentModel commentModel) {
        if (commentModelMutableLiveData != null) {
            commentModelMutableLiveData.setValue(commentModel);
        }
    }

    public MutableLiveData<CommentModel> getCommentModelMutableLiveData() {
        return commentModelMutableLiveData;
    }

    public FoodDetailViewModel() {
        commentModelMutableLiveData = new MutableLiveData<>();

    }

    public MutableLiveData<FoodModel> getFoodModelMutableLiveData() {
        if (foodModelMutableLiveData == null)
            foodModelMutableLiveData = new MutableLiveData<>();
        foodModelMutableLiveData.setValue(Common.selectFood);
        return foodModelMutableLiveData;
    }

    public void setFoodModel(FoodModel foodModel) {
        if (foodModelMutableLiveData != null) {
            foodModelMutableLiveData.setValue(foodModel);
        }
    }
}