package com.obeidareda37.foodapp.activities.ui.foodlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.adapter.FoodListAdapter;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.model.FoodModel;

import java.util.List;


public class FoodListFragment extends Fragment {

    private FoodListViewModel mViewModel;
    private RecyclerView recycler_food;
    private LayoutAnimationController controller;
    private FoodListAdapter adapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.food_list_fragment, container, false);
        recycler_food = root.findViewById(R.id.recycler_food_list);
        initViews();

        mViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), new Observer<List<FoodModel>>() {
            @Override
            public void onChanged(List<FoodModel> foodModels) {
                adapter = new FoodListAdapter(FoodListFragment.this.getContext(), foodModels);
                recycler_food.setAdapter(adapter);
                recycler_food.setLayoutAnimation(controller);
            }
        });

        return root;
    }

    private void initViews() {
        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName());
        recycler_food.setHasFixedSize(true);
        recycler_food.setLayoutManager(new LinearLayoutManager(getContext()));
        controller = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);


    }


}