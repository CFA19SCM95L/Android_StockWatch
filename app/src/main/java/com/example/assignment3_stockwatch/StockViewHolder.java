package com.example.assignment3_stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {

    public TextView symbol;
    public TextView companyName;
    public TextView latestPrice;
    public TextView change;
    public TextView changePercent;




    public StockViewHolder(View view) {
        super(view);
        symbol = view.findViewById(R.id.symbol);
        companyName = view.findViewById(R.id.companyName);
        latestPrice = view.findViewById(R.id.latestPrice);
        change = view.findViewById(R.id.change);
        changePercent = view.findViewById(R.id.changePercent);
    }
}
