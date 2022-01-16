package com.obeidareda37.foodapp.activities.ui.fooddetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.activities.ui.comments.CommentFragment;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.database.CartDataSource;
import com.obeidareda37.foodapp.database.CartDatabase;
import com.obeidareda37.foodapp.database.CartItem;
import com.obeidareda37.foodapp.database.LocalCartDataSource;
import com.obeidareda37.foodapp.eventbus.CounterCartEven;
import com.obeidareda37.foodapp.model.AddonModel;
import com.obeidareda37.foodapp.model.CommentModel;
import com.obeidareda37.foodapp.model.FoodModel;
import com.obeidareda37.foodapp.model.SizeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class FoodDetailFragment extends Fragment implements TextWatcher {

    private FoodDetailViewModel foodDetailViewModel;
    android.app.AlertDialog waitingDialog;
    private BottomSheetDialog bottomSheetDialog;
    private ChipGroup chipGroupAdd;
    private EditText editSearch;
    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable;
    private ImageView imageFood;
    private TextView textFoodName;
    private TextView textFoodPrice;
    private CounterFab counterFab;
    private FloatingActionButton actionButton;
    private RatingBar ratingBar;
    private Button buttonShowComment;
    private TextView textFoodDescription;
    private ElegantNumberButton numberButton;
    private RadioGroup radioGroup;
    private ImageView image_Add_on;
    private ChipGroup chipGroupUserSelected;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel =
                new ViewModelProvider(this).get(FoodDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);
        imageFood = root.findViewById(R.id.image_food);
        textFoodName = root.findViewById(R.id.food_name);
        textFoodPrice = root.findViewById(R.id.food_price);
        counterFab = root.findViewById(R.id.btnCart);
        actionButton = root.findViewById(R.id.btn_rating);
        ratingBar = root.findViewById(R.id.ratingBar);
        buttonShowComment = root.findViewById(R.id.buttonShowComments);
        textFoodDescription = root.findViewById(R.id.food_description);
        numberButton = root.findViewById(R.id.number_button);
        radioGroup = root.findViewById(R.id.rdi_group_size);
        image_Add_on = root.findViewById(R.id.image_add_on);
        chipGroupUserSelected = root.findViewById(R.id.chip_group);

        initViews();

        foodDetailViewModel.getFoodModelMutableLiveData().observe(getViewLifecycleOwner(), foodModel -> {
            displayInfo(foodModel);
        });

        foodDetailViewModel.getCommentModelMutableLiveData().observe(getViewLifecycleOwner(), commentModel -> {
            submitRatingToFirebase(commentModel);
        });

        image_Add_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddClick();
            }
        });

        counterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCartItemAdd();
            }
        });
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRating();
            }
        });
        buttonShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentFragment commentFragment = CommentFragment.getInstance();
                commentFragment.show(getActivity().getSupportFragmentManager(), "CommentFragment");
            }
        });
        return root;
    }

    void onAddClick() {

        if (Common.selectFood.getAddon() != null) {
            displayAddonList(); // show all add on options
            bottomSheetDialog.show();
        }
    }

    void onCartItemAdd() {
        CartItem cartItem = new CartItem();
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone(Common.currentUser.getPhone());
        cartItem.setFoodId(Common.selectFood.getId());
        cartItem.setFoodName(Common.selectFood.getName());
        cartItem.setFoodImage(Common.selectFood.getImage());
        cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectFood.getPrice())));
        cartItem.setFoodQuantity(Integer.parseInt(numberButton.getNumber()));
        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectFood.getUserSelectedSize(),
                Common.selectFood.getUserSelectedAddon())); // because default we not choose size + addon so extra price is 0.

        if (Common.selectFood.getUserSelectedAddon() != null) {
            cartItem.setFoodAddon(new Gson().toJson(Common.selectFood.getUserSelectedAddon()));
        } else {
            cartItem.setFoodAddon("Default");
        }
        if (Common.selectFood.getUserSelectedSize() != null) {
            cartItem.setFoodSize(new Gson().toJson(Common.selectFood.getUserSelectedSize()));
        } else {
            cartItem.setFoodSize("Default");
        }


        cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                cartItem.getFoodId(),
                cartItem.getFoodSize(),
                cartItem.getFoodAddon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull CartItem cartItemFromDB) {

                        if (cartItemFromDB.equals(cartItem)) {

                            //Already in database , just update
                            cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                            cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                            cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                            cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity()
                                    + cartItem.getFoodQuantity());
                            cartDataSource.updateCartItems(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {

                                            Toast.makeText(getContext(), "Update Cart success", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEven(true));

                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            //  Toast.makeText(getContext(), "[CART UPDATE]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            //Item not available in cart before , insert new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Add to Cart success", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEven(true));
                                    }, Throwable -> {
                                        //   Toast.makeText(getContext(), "[CART ERROR]" + Throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (e.getMessage().contains("empty")) {
                            //Default , if Cart is empty , this code will be fired
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Add to Cart success", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEven(true));
                                    }, Throwable -> {
                                        //  Toast.makeText(getContext(), "[CART ERROR]" + Throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        } else {
                            //  Toast.makeText(getContext(), "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    private void displayAddonList() {
        if (Common.selectFood.getAddon().size() > 0) {
            chipGroupAdd.clearCheck(); // clear check all Views
            chipGroupAdd.removeAllViews();

            editSearch.addTextChangedListener(this);

            //add all views
            for (AddonModel addonModel : Common.selectFood.getAddon()) {

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_add_on_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (Common.selectFood.getUserSelectedAddon() == null) {
                            Common.selectFood.setUserSelectedAddon(new ArrayList<>());
                        }
                        Common.selectFood.getUserSelectedAddon().add(addonModel);
                    }

                });
                chipGroupAdd.addView(chip);
            }
        }
    }

    private void showDialogRating() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Rating Food");
        builder.setMessage("please fill information");
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating, null);

        RatingBar ratingBar = itemView.findViewById(R.id.rating_bar);
        EditText editText = itemView.findViewById(R.id.edit_comment);
        builder.setView(itemView);


        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                CommentModel commentModel = new CommentModel();
                commentModel.setName(Common.currentUser.getName());
                commentModel.setUid(Common.currentUser.getUid());
                commentModel.setComment(editText.getText().toString());
                commentModel.setRatingValue(ratingBar.getRating());
                Map<String, Object> serverTimeStamp = new HashMap<>();
                serverTimeStamp.put("timeStamp", ServerValue.TIMESTAMP);
                commentModel.setServerTimeStamp(serverTimeStamp);

                foodDetailViewModel.setCommentModelMutableLiveData(commentModel);

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void initViews() {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        compositeDisposable = new CompositeDisposable();
        waitingDialog = new SpotsDialog.Builder().setCancelable(false)
                .setContext(getContext()).build();

        bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.DialogStyle);
        View layout_add_on = getLayoutInflater().inflate(R.layout.layout_add_on, null);
        chipGroupAdd = layout_add_on.findViewById(R.id.chip_group_add);
        editSearch = layout_add_on.findViewById(R.id.edit_search);
        bottomSheetDialog.setContentView(layout_add_on);

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                displayUserSelectedAddOn();
                calculateTotalPrice();
            }
        });
    }

    private void displayUserSelectedAddOn() {

        if (Common.selectFood.getUserSelectedAddon() != null
                && Common.selectFood.getUserSelectedAddon().size() > 0) {
            chipGroupUserSelected.removeAllViews(); // clear All view Already add
            for (AddonModel addonModel : Common.selectFood.getUserSelectedAddon())// Add all available addon to list
            {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_remove_icon, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(v -> {
                    //remove when user select delete
                    chipGroupUserSelected.removeView(v);
                    Common.selectFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chipGroupUserSelected.addView(chip);
            }
        } else {
            chipGroupUserSelected.removeAllViews();
        }


    }

    private void submitRatingToFirebase(CommentModel commentModel) {
        waitingDialog.show();
        //Fires , we will Submit toComment firebase Ref
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
                .child(Common.selectFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            //After submit to CommentRef , we will update value avereg in Food
                            addRatingToFood(commentModel.getRatingValue());

                        }
                        waitingDialog.dismiss();

                    }
                });

    }

    private void addRatingToFood(float ratingValue) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())//Select Category
                .child("foods") //Select array list 'foods' of this category
                .child(Common.selectFood.getKey())// Because food item is array list so key is index of arrayList
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            FoodModel foodModel = snapshot.getValue(FoodModel.class);
                            foodModel.setKey(Common.selectFood.getKey());// don not forget

                            //Apply rating
                            if (foodModel.getRatingValue() == null) {
                                foodModel.setRatingValue(0d);
                            }
                            if (foodModel.getRatingCount() == null) {
                                foodModel.setRatingCount(0l);
                            }
                            double sumRating = foodModel.getRatingValue() + ratingValue;
                            long ratingCount = foodModel.getRatingCount() + 1;

                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("ratingValue", sumRating);
                            updateData.put("ratingCount", ratingCount);

                            //update data in variabel
                            foodModel.setRatingValue(sumRating);
                            foodModel.setRatingCount(ratingCount);

                            snapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            waitingDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Thank you !", Toast.LENGTH_SHORT).show();
                                                Common.selectFood = foodModel;
                                                foodDetailViewModel.setFoodModel(foodModel); // Call refresh
                                            }

                                        }
                                    });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        waitingDialog.dismiss();
                        // Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void displayInfo(FoodModel foodModel) {

        Glide.with(getContext()).load(foodModel.getImage()).into(imageFood);
        textFoodName.setText(new StringBuilder(foodModel.getName()));
        textFoodDescription.setText(new StringBuilder(foodModel.getDescription()));
        textFoodPrice.setText(new StringBuilder(foodModel.getPrice().toString()));

        if (foodModel.getRatingValue() != null) {
            ratingBar.setRating(foodModel.getRatingValue().floatValue() / foodModel.getRatingCount());

            //first time => Value :5 , count 1=> 5/1 = 5
            //second time => Value :1 , count 2=> (5+1)/2 = 3
            //three time => Value :1 , count 3=> (5+1+1)/3 = 3.2

        }

        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setTitle(Common.selectFood.getName());


        for (SizeModel sizeModel : Common.selectFood.getSize()) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        Common.selectFood.setUserSelectedSize(sizeModel);
                    calculateTotalPrice(); // Update price

                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT
                    , LinearLayout.LayoutParams.MATCH_PARENT
                    , 1.0f);

            radioButton.setLayoutParams(params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());

            radioGroup.addView(radioButton);

            if (radioGroup.getChildCount() > 0) {
                RadioButton radioButton1 = (RadioButton) radioGroup.getChildAt(0);
                radioButton1.setChecked(true); //Default first Select
            }
            calculateTotalPrice();

        }
    }

    private void calculateTotalPrice() {


        // total price food
        double totalPrice = Double.parseDouble(Common.selectFood.getPrice().toString()), displayPrice = 0.0;

        //Add on
        if (Common.selectFood.getUserSelectedAddon() != null && Common.selectFood.getUserSelectedAddon().size() > 0) {
            for (AddonModel addonModel : Common.selectFood.getUserSelectedAddon()) {
                totalPrice += Double.parseDouble(addonModel.getPrice().toString());
            }
        }

        //total price food + price addition with size
        if (Common.selectFood.getUserSelectedSize() != null)
            totalPrice += Double.parseDouble(Common.selectFood.getUserSelectedSize().getPrice().toString());

        displayPrice = totalPrice * (Integer.parseInt(numberButton.getNumber()));
        displayPrice = Math.round(displayPrice * 100.0 / 100.0); // اقرب عدد صحيح
        textFoodPrice.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        chipGroupAdd.clearCheck();
        chipGroupAdd.removeAllViews();

        for (AddonModel addonModel : Common.selectFood.getAddon()) {
            if (addonModel.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_add_on_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            if (Common.selectFood.getUserSelectedAddon() == null) {
                                Common.selectFood.setUserSelectedAddon(new ArrayList<>());
                            }
                            Common.selectFood.getUserSelectedAddon().add(addonModel);
                        }

                    }
                });
                chipGroupAdd.addView(chip);
            }
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}