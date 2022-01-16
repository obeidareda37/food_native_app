package com.obeidareda37.foodapp.callback;

import com.obeidareda37.foodapp.model.PopularCategory;

import java.util.List;

public interface IPopularCallbackListener {
    void onPopularLoadSuccess(List<PopularCategory> popularCategories);
    void onPopularLoadFailed(String message);
}
