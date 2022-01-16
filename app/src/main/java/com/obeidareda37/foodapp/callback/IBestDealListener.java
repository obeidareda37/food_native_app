package com.obeidareda37.foodapp.callback;

import com.obeidareda37.foodapp.model.BestDeal;

import java.util.List;

public interface IBestDealListener {
    void onBestDealLoadSuccess(List<BestDeal> bestDeals);
    void onBestDealLoadFailed(String message);
}
