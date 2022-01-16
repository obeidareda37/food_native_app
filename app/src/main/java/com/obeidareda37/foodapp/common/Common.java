package com.obeidareda37.foodapp.common;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;

import com.obeidareda37.foodapp.model.AddonModel;
import com.obeidareda37.foodapp.model.CategoryModel;
import com.obeidareda37.foodapp.model.FoodModel;
import com.obeidareda37.foodapp.model.SizeModel;
import com.obeidareda37.foodapp.model.UserModel;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Common {

    public static final String USER_REFERENCES = "Users";
    public static final String POPULAR_CATEGORY_REF = "MostPopular";
    public static final String BEST_DEAL_REF = "BestDeals";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static final String COMMENT_REF = "Comments";
    public static final String ORDER_REF ="Order" ;
    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectFood;

    public static String formatPrice(double price) {
        if (price != 0) {

            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            decimalFormat.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(decimalFormat.format(price)).toString();
            return finalPrice.replace(".", ",");
        } else {
            return "0,00";
        }
    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        Double result = 0.0;
        if (userSelectedSize == null&& userSelectedAddon == null) {
            return 0.0;
        } else if (userSelectedSize == null) {
            //If userSelectedAddon !=null , we need sum price
            for (AddonModel addonModel : userSelectedAddon) {
                result += addonModel.getPrice();
            }
            return result;
        } else if (userSelectedAddon == null) {
            return userSelectedSize.getPrice() * 1.0;
        } else {
            //If both size and addon is select
            result = userSelectedSize.getPrice() * 1.0;
            for (AddonModel addonModel : userSelectedAddon) {
                result += addonModel.getPrice();
            }
            return result;
        }
    }

    public static void setSpanString(String welcome, String name, TextView textUser) {

        //Span to users
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan,0,name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textUser.setText(builder,TextView.BufferType.SPANNABLE);
    }

    public static String createOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis())// Get current time
                .append(Math.abs(new Random().nextInt())) // Add random number to block same order at same time
                .toString();
    }
}
