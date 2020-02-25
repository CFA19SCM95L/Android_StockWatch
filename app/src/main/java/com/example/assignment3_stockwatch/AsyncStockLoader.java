package com.example.assignment3_stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncStockLoader extends AsyncTask<String, Integer, String> implements Serializable {

    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private static final String TAG = "AsyncStockLoader";
    private static final String STOCK_URL_FIRSTPART = "https://cloud.iexapis.com/stable/stock/";
    private static final String STOCK_URL_SECONDPART = "/quote?token=sk_4f44df27e0ea476f922f6ef87e816126";

    public Stock stock;
    public int num = 0;

    public AsyncStockLoader(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    protected void onPostExecute(String s) {
        mainActivity.updateStock(stock, num);
    }


    @Override
    protected String doInBackground(String... strings) {
        Uri dataUri = Uri.parse(STOCK_URL_FIRSTPART + strings[0] + STOCK_URL_SECONDPART);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "doInBackground: " + urlToUse);
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "doInBackground: ResponseCode: " + conn.getResponseCode());
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            Log.d(TAG, "doInBackground: " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }
        parseJSON(sb.toString());
        num += 1;
        return null;
    }

    private void parseJSON(String s) {
        try {
            JSONObject jStock = new JSONObject(s);
            String symbol = jStock.getString("symbol");
            String companyName = jStock.getString("companyName");
            String latestPriceUC = jStock.getString("latestPrice");
            if (latestPriceUC.equals("null")) {
                latestPriceUC = "0";
            }
            String changeUC = jStock.getString("change");
            if (changeUC.equals("null")) {
                changeUC = "0";
            }
            String changePercentUC = jStock.getString("changePercent");
            if (changePercentUC.equals("null")) {
                changePercentUC = "0";
            }
            double latestPrice = Double.parseDouble(latestPriceUC);
            double change = Double.parseDouble(changeUC);
            double changePercent = Double.parseDouble(changePercentUC);
            stock = new Stock(symbol, companyName, latestPrice, change, changePercent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
