package com.example.assignment3_stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AsyncSymbolLoader extends AsyncTask<String, Integer, String> {

    private Map<String, String> symbols = new HashMap<>();   //symbol,name

    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private static final String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private static final String TAG = "AsyncSymbolLoader";

    public AsyncSymbolLoader(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    protected void onPostExecute(String s) {
        mainActivity.updateSymbol(symbols);
    }

    @Override
    protected String doInBackground(String... strings) {
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "doInBackground: " + urlToUse);
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "doInBackground: ResponseCode: " + conn.getResponseCode());
            if (conn.getResponseCode() != 200) {
                return null;
            }
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            Log.d(TAG, "doInBackground: " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: error happen", e);
            return null;
        }
        parseJSON(sb.toString());
        return null;
    }


    private void parseJSON(String s) {
        try {
            JSONArray jObjMain = new JSONArray(s);
            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jSymbol = (JSONObject) jObjMain.get(i);
                String symbol = jSymbol.getString("symbol");
                String name = jSymbol.getString("name");
                symbols.put(symbol, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
