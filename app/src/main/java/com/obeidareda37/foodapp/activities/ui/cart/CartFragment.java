package com.obeidareda37.foodapp.activities.ui.cart;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.adapter.CartAdapter;
import com.obeidareda37.foodapp.common.Common;
import com.obeidareda37.foodapp.common.SwipeHelper;
import com.obeidareda37.foodapp.database.CartDataSource;
import com.obeidareda37.foodapp.database.CartDatabase;
import com.obeidareda37.foodapp.database.CartItem;
import com.obeidareda37.foodapp.database.LocalCartDataSource;
import com.obeidareda37.foodapp.eventbus.CounterCartEven;
import com.obeidareda37.foodapp.eventbus.HideFabCart;
import com.obeidareda37.foodapp.eventbus.UpdateItemInCart;
import com.obeidareda37.foodapp.model.Order;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;

public class CartFragment extends Fragment {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Parcelable recyclerViewState;
    private CartViewModel cartViewModel;
    private CartDataSource cartDataSource;
    private CartAdapter cartAdapter;
    private Geocoder geocoder;
    private double amount = 0.0;
    private double total = 0.0;
    private String address, comment;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationListener locationListener;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private RecyclerView recyclerCart;
    private TextView textTotal;
    private TextView textEmptyCart;
    private CardView cardView;
    private MaterialButton buttonPlaceOrder;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.cart_fragment, container, false);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        recyclerCart = itemView.findViewById(R.id.recycler_cart);
        textTotal = itemView.findViewById(R.id.text_total_price);
        textEmptyCart = itemView.findViewById(R.id.text_empty_cart);
        cardView = itemView.findViewById(R.id.group_place_holder);
        buttonPlaceOrder = itemView.findViewById(R.id.btn_place_order);

        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCart().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems == null || cartItems.isEmpty()) {
                recyclerCart.setVisibility(View.GONE);
                cardView.setVisibility(View.GONE);
                textEmptyCart.setVisibility(View.VISIBLE);

            } else {
                recyclerCart.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);
                textEmptyCart.setVisibility(View.GONE);

                cartAdapter = new CartAdapter(getContext(), cartItems);
                recyclerCart.setAdapter(cartAdapter);
            }
        });

        initViews();

        initLocations();
        buttonPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlaceOrderClick();
            }
        });
        return itemView;
    }



    void onPlaceOrderClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText editAddress = itemView.findViewById(R.id.edit_address1);
        EditText editComment = itemView.findViewById(R.id.edit_Comment1);
        TextView textAddress = itemView.findViewById(R.id.text_address_detail);
        RadioButton rdi_home = itemView.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other = itemView.findViewById(R.id.rdi_other_address);
//        RadioButton rdi_Ship_this = itemView.findViewById(R.id.rdi_this_address);
        RadioButton rdi_cod = itemView.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree = itemView.findViewById(R.id.rdi_braintree);


        //Data
        Common.currentUser.getAddress(); // By Default we select home address , so user's address will display

        //Event
        rdi_home.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                editAddress.setText(Common.currentUser.getAddress());
                textAddress.setVisibility(View.GONE);

            }
        });

        rdi_other.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                editAddress.setText(" "); //clear
                editAddress.setHint("Enter your address");
                textAddress.setVisibility(View.GONE);

            }
        });

//        rdi_Ship_this.setOnCheckedChangeListener((buttonView, isChecked) -> {
//
//            if (isChecked) {
//
//                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                fusedLocationProviderClient.getLastLocation()
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                textAddress.setVisibility(View.GONE);
//                            }
//                        }).addOnCompleteListener(new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        String coordinates = new StringBuilder()
//                                .append(task.getResult().getLatitude())
//                                .append("/").append(task.getResult().getLongitude()).toString();
//
//                        Log.d("Location2", coordinates);
//
//                        Single<String> singleAddress = Single.just(getAddressFromLatLong(task.getResult().getLatitude(),
//                                task.getResult().getLongitude(), getActivity()));
//                        Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
//                            @Override
//                            public void onSuccess(@io.reactivex.annotations.NonNull String s) {
//                                editAddress.setText(coordinates);
//                                textAddress.setText(s);
//                                textAddress.setVisibility(View.VISIBLE);
//                            }
//
//                            @Override
//                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
//                                editAddress.setText(coordinates);
//                                textAddress.setText(e.getMessage());
//                                textAddress.setVisibility(View.VISIBLE);
//                                Log.d("Location", e.getMessage());
//                            }
//                        });
//
//
//                    }
//                });
//            }
//        });

        builder.setView(itemView);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (rdi_cod.isChecked()) {
                    paymentCOD(editAddress.getText().toString(), editComment.getText().toString());
                } else if (rdi_braintree.isChecked()) {
                    address = editAddress.getText().toString();
                    comment = editComment.getText().toString();
                    Checkout checkout = new Checkout();
                    checkout.setKeyID("rzp_test_KKsav0ylNCC2QB");
                    try {
                        JSONObject options = new JSONObject();
                        //Set Company Name
                        options.put("name", "My E-Commerce App");
                        //Ref no
                        options.put("description", "Reference No. #123456");
                        //Image to be display
                        options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
                        //options.put("order_id", "order_9A33XWu170gUtm");
                        // Currency type
                        options.put("currency", "USD");
                        //double total = Double.parseDouble(mAmountText.getText().toString());
                        //multiply with 100 to get exact amount in rupee

                        //amount
                        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Double>() {
                                    @Override
                                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(@io.reactivex.annotations.NonNull Double aDouble) {
                                        compositeDisposable.add(cartDataSource.
                                                getAllCarts(Common.currentUser.getUid())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Consumer<List<CartItem>>() {
                                                    @Override
                                                    public void accept(List<CartItem> cartItems) throws Exception {
                                                        amount = aDouble;
                                                        Order order = new Order();
                                                        order.setUserId(Common.currentUser.getUid());
                                                        order.setUserName(Common.currentUser.getName());
                                                        order.setUserPhone(Common.currentUser.getPhone());
                                                        order.setShippingAddress(address);
                                                        order.setComment(comment);

                                                        if (currentLocation != null) {
                                                            order.setLat(currentLocation.getLatitude());
                                                            order.setLog(currentLocation.getLongitude());
                                                        } else {
                                                            order.setLat(-0.1f);
                                                            order.setLog(-0.1f);
                                                        }

                                                        order.setCartItemList(cartItems);
                                                        order.setTotalPayment(amount);
                                                        order.setDiscount(0);
                                                        order.setFinalPayment(amount);
                                                        order.setCod(false);
                                                        order.setTransactionId("Razorpay");

                                                        options.put("amount", amount);
                                                        submitOrderToFirebase(order);

                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        //    Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                        Log.d("error1", throwable.getMessage());

                                                    }
                                                }));
                                    }

                                    @Override
                                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                        //   Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.d("error1", e.getMessage());
                                    }
                                });
//                        amount = sumAllItemInCart();
//                        Log.d("amount", amount+"");
//                        options.put("amount", sumAllItemInCart());
                        JSONObject preFill = new JSONObject();
                        //email
                        preFill.put("email", "Obeidareda47@gmail.com");
                        //contact
                        preFill.put("contact", "0592143946");

                        options.put("prefill", preFill);

                        checkout.open(getActivity(), options);
                    } catch (Exception e) {
                        Log.e("TAG", "Error in starting Razorpay Checkout", e);
                    }


                }
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCarts(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    //When we have all cartItem, we will get total price
                    cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Double>() {
                                @Override
                                public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@io.reactivex.annotations.NonNull Double totalPrice) {
                                    final double finalPrice = totalPrice;
                                    Order order = new Order();
                                    order.setUserId(Common.currentUser.getUid());
                                    order.setUserName(Common.currentUser.getName());
                                    order.setUserPhone(Common.currentUser.getPhone());
                                    order.setShippingAddress(address);
                                    order.setComment(comment);

                                    if (currentLocation != null) {
                                        order.setLat(currentLocation.getLatitude());
                                        order.setLog(currentLocation.getLongitude());
                                    } else {
                                        order.setLat(-0.1f);
                                        order.setLog(-0.1f);
                                    }

                                    order.setCartItemList(cartItems);
                                    order.setTotalPayment(totalPrice);
                                    order.setDiscount(0);
                                    order.setFinalPayment(finalPrice);
                                    order.setCod(true);
                                    order.setTransactionId("Cash On Delivery");
                                    submitOrderToFirebase(order);


                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                    //  Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                }, throwable -> {
                    //  Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void submitOrderToFirebase(Order order) {
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())//create order number with only digit
                .setValue(order)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CartFragment.this.getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(task -> {

                    //success
                    cartDataSource.cleanCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                    //clean Success
                                    Toast.makeText(getContext(), "Place Order successfully!", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                    //     Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

    private String getAddressFromLatLong(double latitude, double longitude, FragmentActivity context) {
        double lat = latitude;
        double lon = longitude;
//        Log.d("latitude",lat+"latitude");
//        Log.d("longitude",lon+"longitude");
//        Log.d("longitude",longitude+"longitude1");
//        Log.d("latitude",latitude+"latitude1");
        String result = new StringBuilder("").append(lat).append("-").append(lon).toString();
//        List<Address> addressList = null;
//        Address address;
//        geocoder = new Geocoder(getContext().getApplicationContext(), Locale.getDefault());
//        try {
//            addressList = geocoder.getFromLocation(lat, lon, 10);
//            Log.d("geocoder", geocoder + "");
//            if (addressList != null && addressList.size() > 0) {
//                Log.d("Location5", "result");
//                address = addressList.get(0);//  always get first item
//                String localite = address.getLocality();
//                StringBuilder stringBuilder = new StringBuilder(address.getAddressLine(0))
//                        .append(localite);
//                result = stringBuilder.toString();
//                Log.d("Location5", result);
//                Log.d("Location5", localite);
//
//            } else {
//                result = "Address not found";
//                Log.d("Location6", result);
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            result = e.getMessage();
//            Log.d("Location1", e.getMessage());
//
//        }
        return result;
    }


    private void initLocations() {
        buildLocationRequest();
        buildLocationCallback();
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED && ActivityCompat.
//                checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() { // تلقي الاخطارات من ال fusedLocationsProvider عندما يتغير موقع الجهاز او يصعب تحديده
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) { //يتم الاتصال عند توفر معلومات موقع الجهاز.
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                Log.d("Location4", currentLocation.toString());
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest(); // تستخدم لطلب جودة الخدمة لتحديثات المواقع.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // طلب الموقع بجودة عالية
        locationRequest.setInterval(5000); // خمس ثواني لطلب الموقع بجودة عالية
        locationRequest.setFastestInterval(3000); // طلب التحديثات بسرعة عالية
        locationRequest.setSmallestDisplacement(10f); // الحد الادنى للازاحة بين تحديثات المواقع بالامتار

    }

    private void initViews() {
        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFabCart(true));

        recyclerCart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerCart.setLayoutManager(layoutManager);
        recyclerCart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        SwipeHelper swipeHelper = new SwipeHelper(getContext(), recyclerCart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#FF3c30"),
                        pos -> {
                            CartItem cartItem = cartAdapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                            cartAdapter.notifyItemRemoved(pos);
                                            calculateTotalPrice(); // Update total price after delete
                                            EventBus.getDefault().postSticky(new CounterCartEven(true));//UPDATE FAB
                                            Toast.makeText(getContext(), "Delete item from Cart successful!", Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            //  Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };

//        sumAllItemInCart();
        calculateTotalPrice();
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false); //Hide Home menu already inflate
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                            Toast.makeText(getContext(), "Clear Cart Success", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEven(true));

                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            // Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onStart();
        calculateTotalPrice();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event) {
        if (event.getCartItem() != null) {
            //First , save state of Recycler view
            recyclerViewState = recyclerCart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                            calculateTotalPrice();
                            recyclerCart.getLayoutManager().onRestoreInstanceState(recyclerViewState);//fix error refresh recycler after update


                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            //Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Double price) {
                        textTotal.setText(new StringBuilder("Total: $")
                                .append(Common.formatPrice(price)));

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        //Toast.makeText(getContext(), "[SUM CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (fusedLocationProviderClient != null) {
//            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
//        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFabCart(false));
        cartViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        compositeDisposable.clear();
        super.onStop();

    }

}