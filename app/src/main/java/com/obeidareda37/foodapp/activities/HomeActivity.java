package com.obeidareda37.foodapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.database.CartDataSource;
import com.obeidareda37.foodapp.database.CartDatabase;
import com.obeidareda37.foodapp.database.LocalCartDataSource;
import com.obeidareda37.foodapp.eventbus.BestDealItemClick;
import com.obeidareda37.foodapp.eventbus.CategoryClick;
import com.obeidareda37.foodapp.eventbus.CounterCartEven;
import com.obeidareda37.foodapp.eventbus.FoodItemClick;
import com.obeidareda37.foodapp.eventbus.HideFabCart;
import com.obeidareda37.foodapp.eventbus.PopularCategoryClick;
import com.obeidareda37.foodapp.model.CategoryModel;
import com.obeidareda37.foodapp.model.FoodModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.PaymentResultListener;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PaymentResultListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    private CartDataSource cartDataSource;
    private android.app.AlertDialog dialog;
    private CounterFab fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fab = findViewById(R.id.fab);
        dialog = new SpotsDialog.Builder().setContext(getApplicationContext())
                .setCancelable(false).build();

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_cart);
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_foodList, R.id.nav_food_detail, R.id.nav_cart)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView textUser = headerView.findViewById(R.id.text_user);

        Common.setSpanString("Hey, ", Common.currentUser.getName(), textUser);

        counterCartItem();


    }

    @Override
    protected void onResume() {
        super.onResume();
        counterCartItem();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    //EventBus
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {

        if (event.isSuccess()) {

            navController.navigate(R.id.nav_foodList);
            // Toast.makeText(this, "Click to " + event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event) {

        if (event.isSuccess()) {

            navController.navigate(R.id.nav_food_detail);
            // Toast.makeText(this, "Click to " + event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularClick(PopularCategoryClick event) {

        if (event.getPopularCategory() != null) {

            FirebaseDatabase.getInstance().getReference("Category")
                    .child(event.getPopularCategory().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                //Load food
                                FirebaseDatabase.getInstance().getReference("Category")
                                        .child(event.getPopularCategory().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                        Common.selectFood = dataSnapshot.getValue(FoodModel.class);
                                                        Common.selectFood.setKey(dataSnapshot.getKey());

                                                    }
                                                    navController.navigate(R.id.nav_food_detail);

                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Item does't exists!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "Item does't exist!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item does't exist!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                            dialog.dismiss();
                            // Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event) {

        if (event.getBestDeal() != null) {

            FirebaseDatabase.getInstance().getReference("Category")
                    .child(event.getBestDeal().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                //Load food
                                FirebaseDatabase.getInstance().getReference("Category")
                                        .child(event.getBestDeal().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                        Common.selectFood = dataSnapshot.getValue(FoodModel.class);
                                                        Common.selectFood.setKey(dataSnapshot.getKey());

                                                    }
                                                    navController.navigate(R.id.nav_food_detail);

                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Item does't exists!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "Item does't exist!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item does't exist!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                            dialog.dismiss();
                            //Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHidFabCart(HideFabCart event) {

        if (event.isHidden()) {

            fab.hide();
        } else fab.show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEven event) {

        if (event.isSuccess()) {

            counterCartItem();
        }
    }

    private void counterCartItem() {

        cartDataSource.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Integer integer) {
                        fab.setCount(integer);

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (!e.getMessage().contains("Query returned empty")) {
                            // Toast.makeText(HomeActivity.this, "[CART COUNT]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else
                            fab.setCount(0);

                    }
                });
    }


    @Override
    public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_home:
                navController.navigate(R.id.nav_home);
                break;
            case R.id.nav_menu:
                navController.navigate(R.id.nav_menu);
                break;
            case R.id.nav_cart:
                navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
        }
        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
                .setMessage("Do you really want to sign out?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Common.selectFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(getApplicationContext(), "Payment Successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentError(int i, String s) {

        Toast.makeText(getApplicationContext(), "Payment Cancel", Toast.LENGTH_SHORT).show();
        Log.d("sssssssssssss", s);
    }

}