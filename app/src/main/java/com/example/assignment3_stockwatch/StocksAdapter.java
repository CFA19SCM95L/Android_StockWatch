package com.example.assignment3_stockwatch;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StocksAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private static final String TAG = "StocksAdapter";
    private List<Stock> stockList;
    private MainActivity mainAct;

    StocksAdapter(List<Stock> stockList, MainActivity ma) {
        this.stockList = stockList;
        mainAct = ma;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.symbol.setText(stock.getSymbol());
        holder.companyName.setText(stock.getCompanyName());

        holder.latestPrice.setText("" + String.format("%.2f", stock.getLatestPrice()));
        holder.changePercent.setText("(" + String.format("%.2f", stock.getChangePercent() * 100) + "%)");
        if (stock.getChange() < 0) {
            holder.change.setText("▼ " + String.format("%.2f", stock.getChange()));
            holder.symbol.setTextColor(Color.parseColor("#FF0000"));
            holder.companyName.setTextColor(Color.parseColor("#FF0000"));
            holder.latestPrice.setTextColor(Color.parseColor("#FF0000"));
            holder.changePercent.setTextColor(Color.parseColor("#FF0000"));
            holder.change.setTextColor(Color.parseColor("#FF0000"));

        } else {
            holder.change.setText("▲ " + String.format("%.2f", stock.getChange()));
            holder.symbol.setTextColor(Color.parseColor("#00FF00"));
            holder.companyName.setTextColor(Color.parseColor("#00FF00"));
            holder.latestPrice.setTextColor(Color.parseColor("#00FF00"));
            holder.changePercent.setTextColor(Color.parseColor("#00FF00"));
            holder.change.setTextColor(Color.parseColor("#00FF00"));
        }


    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

}
