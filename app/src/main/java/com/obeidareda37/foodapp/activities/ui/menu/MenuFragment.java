package com.obeidareda37.foodapp.activities.ui.menu;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.adapter.CategoriesAdapter;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.common.SpacesItemDecorations;
import dmax.dialog.SpotsDialog;


public class MenuFragment extends Fragment {

    private MenuViewModel menuViewModel;
    private RecyclerView recyclerMenu;

    private AlertDialog alertDialog;
    private LayoutAnimationController animationController;
    private CategoriesAdapter categoriesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuViewModel =
                new ViewModelProvider(this).get(MenuViewModel.class);
        View root = inflater.inflate(R.layout.fragment_menu, container, false);
        recyclerMenu = root.findViewById(R.id.recycler_menu);

        initViews();

        menuViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            // Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
            alertDialog.dismiss();
        });

        menuViewModel.getCategoryListMultable().observe(getViewLifecycleOwner(), categoryModels -> {
            alertDialog.dismiss();
            categoriesAdapter = new CategoriesAdapter(getContext(), categoryModels);
            recyclerMenu.setAdapter(categoriesAdapter);
            recyclerMenu.setLayoutAnimation(animationController);
        });

        return root;
    }

    private void initViews() {
        alertDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        alertDialog.show();
        animationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (categoriesAdapter != null) {
                    switch (categoriesAdapter.getItemViewType(position)) {
                        case Common
                                .DEFAULT_COLUMN_COUNT:
                            return 1;
                        case Common.FULL_WIDTH_COLUMN:
                            return 2;
                        default:
                            return -1;
                    }
                }
                return -1;
            }
        });

        recyclerMenu.setLayoutManager(gridLayoutManager);
        recyclerMenu.addItemDecoration(new SpacesItemDecorations(8));
    }
}