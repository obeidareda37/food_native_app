package com.obeidareda37.foodapp.activities.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.loopingviewpager.LoopingViewPager;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.adapter.BestDealsAdapter;
import com.obeidareda37.foodapp.adapter.PopularCategoryAdapter;


public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private RecyclerView recycler_popular;
    private LoopingViewPager bestDealViewPager;
    private LayoutAnimationController animationController;
    private PopularCategoryAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        recycler_popular = root.findViewById(R.id.recycler_popular);
         bestDealViewPager= root.findViewById(R.id.viewPager);

        init();
        homeViewModel.getPopularList().observe(getViewLifecycleOwner(),popularCategories -> {
            //Create adapter
             adapter = new PopularCategoryAdapter(getContext(),popularCategories);
            recycler_popular.setAdapter(adapter);
            recycler_popular.setLayoutAnimation(animationController);

        });

        homeViewModel.getBestDealList().observe(getViewLifecycleOwner(),bestDeals -> {
            BestDealsAdapter adapter = new BestDealsAdapter(getContext(),bestDeals,true);
            bestDealViewPager.setAdapter(adapter);
        });
        return root;
    }

    private void init() {
        animationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        recycler_popular.setHasFixedSize(true);
        recycler_popular.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));

    }

    @Override
    public void onResume() {
        super.onResume();
        bestDealViewPager.resumeAutoScroll();
    }

    @Override
    public void onPause() {
        bestDealViewPager.pauseAutoScroll();
        super.onPause();
    }
}