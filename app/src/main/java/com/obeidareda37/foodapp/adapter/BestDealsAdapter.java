package com.obeidareda37.foodapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.bumptech.glide.Glide;
import com.obeidareda37.foodapp.R;
import com.obeidareda37.foodapp.eventbus.BestDealItemClick;
import com.obeidareda37.foodapp.model.BestDeal;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


public class BestDealsAdapter extends LoopingPagerAdapter<BestDeal> {
    ImageView imageBestDeal;
    TextView textBestDeal;

    public BestDealsAdapter(Context context, List<BestDeal> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        return LayoutInflater.from(context).inflate(R.layout.layout_beast_deal_item, container, false);

    }

    @Override
    protected void bindView(View convertView, int listPosition, int viewType) {
        textBestDeal = convertView.findViewById(R.id.text_best_deal);
        imageBestDeal = convertView.findViewById(R.id.image_best_deal);
        //set data
        Glide.with(convertView).load(itemList.get(listPosition).getImage()).into(imageBestDeal);
        textBestDeal.setText(itemList.get(listPosition).getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new BestDealItemClick(itemList.get(listPosition)));
            }
        });


    }
}
