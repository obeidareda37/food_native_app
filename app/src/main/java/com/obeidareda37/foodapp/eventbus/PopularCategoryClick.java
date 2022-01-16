package com.obeidareda37.foodapp.eventbus;

import com.obeidareda37.foodapp.model.PopularCategory;

public class PopularCategoryClick {
    private PopularCategory popularCategory;

    public PopularCategoryClick(PopularCategory popularCategory) {
        this.popularCategory = popularCategory;
    }

    public PopularCategory getPopularCategory() {
        return popularCategory;
    }

    public void setPopularCategory(PopularCategory popularCategory) {
        this.popularCategory = popularCategory;
    }
}
