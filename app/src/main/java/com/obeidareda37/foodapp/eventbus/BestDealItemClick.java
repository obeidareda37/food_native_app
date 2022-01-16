package com.obeidareda37.foodapp.eventbus;

import com.obeidareda37.foodapp.model.BestDeal;

public class BestDealItemClick {
    private BestDeal bestDeal;

    public BestDealItemClick(BestDeal bestDeal) {
        this.bestDeal = bestDeal;
    }

    public BestDeal getBestDeal() {
        return bestDeal;
    }

    public void setBestDeal(BestDeal bestDeal) {
        this.bestDeal = bestDeal;
    }
}
