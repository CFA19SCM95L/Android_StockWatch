package com.example.assignment3_stockwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, Serializable {

    private static final String TAG = "MainActivity";
    private static String urlPrefix = "http://www.marketwatch.com/investing/stock/";
    private DatabaseHandler databaseHandler;
    private SwipeRefreshLayout reload;
    private boolean reloadFlag;
    private List<Stock> stockList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StocksAdapter mAdapter;
    private Set<String> duplicate = new HashSet<>();
    private Map<String, String> symbols = new HashMap<>();
    private ConnectivityManager cm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reload = findViewById(R.id.reload);
        recyclerView = findViewById(R.id.recycler);
        mAdapter = new StocksAdapter(stockList, this);
        recyclerView.setAdapter(mAdapter);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!networkCheck()) {
                    disconnectDialog("update");
                    reload.setRefreshing(false);

                    return;
                }
                doRefresh();
            }
        });


        doAsyncSymbol();

        databaseHandler = new DatabaseHandler(this);
        ArrayList<String[]> stocks = databaseHandler.loadStocks();
        if (!networkCheck()) {
            disconnectDialog("update");
            //add zero to all stock
            for (int i = stocks.size()-1; i >=0; i--) {
                //
                duplicate.add(stocks.get(i)[0]);
                //
                stockList.add(new Stock(stocks.get(i)[0],stocks.get(i)[1],0,0,0));
                mAdapter.notifyDataSetChanged();
            }

            return;
        } else {
            for (String[] tmp : stocks) {
                doAsyncStock(tmp[0]);
            }
        }


    }

    private void doRefresh() {
        reloadFlag = true;
        int num = stockList.size();
        ArrayList<String> a = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            a.add(i, stockList.get(i).getSymbol());
        }
        for (int i = 0; i < num; i++) {
            stockList.remove(0);
            mAdapter.notifyDataSetChanged();

        }


        for (int i = 0; i < a.size(); i++) {
            doAsyncStock(a.get(i));
        }

        reload.setRefreshing(false);
        Toast.makeText(this, "Reload success", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                if (!networkCheck()) {
                    disconnectDialog("add");
                    return false;
                }
                if (symbols.size() == 0) {
                    doAsyncSymbol();
                }
                reloadFlag = false;
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final EditText et = new EditText(this);
                et.setGravity(Gravity.CENTER_HORIZONTAL);
                et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                builder.setView(et);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String userInput = et.getText().toString().toUpperCase();
                        ArrayList num = numElements(userInput);

                        if (num.size() == 1 && symbols.containsKey(userInput)) {
                            Toast.makeText(MainActivity.this, userInput, Toast.LENGTH_SHORT).show();
                            doAsyncStock(num.get(0).toString());
                        } else if (num.size() > 0) {
                            multiDialog(num);
                        } else {
                            notFoundDialog(userInput);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setMessage("Please enter a Stock Symbol:");
                builder.setTitle("Stock Selection:");
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock stock = stockList.get(pos);
        String url = urlPrefix + stock.getSymbol();
        Intent i = new Intent((Intent.ACTION_VIEW));
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_delete_black_18dp);
        builder.setMessage("Delete Stock Symbol " + stockList.get(pos).getSymbol());
        builder.setTitle("Delete Stock");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                databaseHandler.deleteStock(stockList.get(pos).getSymbol());
                duplicate.remove(stockList.get(pos).getSymbol());
                stockList.remove(pos);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(MainActivity.this, "Cancel ", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    public void multiDialog(final ArrayList numStocks) {
        final CharSequence[] sArray = new CharSequence[numStocks.size()];
        for (int i = 0; i < numStocks.size(); i++)
            sArray[i] = numStocks.get(i) + " - " + symbols.get(numStocks.get(i));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");
        builder.setItems(sArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                doAsyncStock(numStocks.get(which).toString());
                Toast.makeText(MainActivity.this, sArray[which], Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(MainActivity.this, "You changed never mind!", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void dupDialog(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_warning_black_18dp);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setMessage("Stock Symbol " + symbol + "is already displayed");
        builder.setTitle("Duplicate Stock");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void disconnectDialog(String state) {   //Updated / Added
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setMessage("Stocks Cannot Be " + state + " Without A Network Connection");
        builder.setTitle("No Network Connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public Boolean networkCheck() {
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }


    public void notFoundDialog(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        Log.d(TAG, "Response Code: 404 Not Found");
        builder.setMessage("Data for stock symbol");
        builder.setTitle("Symbol Not Found: " + symbol);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doAsyncSymbol() {
        new AsyncSymbolLoader(this).execute();
    }

    private void doAsyncStock(String s) {
        new AsyncStockLoader(this).execute(s);
    }

    public void updateSymbol(Map<String, String> symbol) {
        if (symbol.size() == 0) {
//            networkCheck("update");
//            disconnectDialog("update");
            for (int i = 0; i < stockList.size(); i++) {
                stockList.get(i).setChange(0);
                stockList.get(i).setChangePercent(0);
                stockList.get(i).setLatestPrice(0);
                //
                duplicate.add(stockList.get(i).getSymbol());
            }
            mAdapter.notifyDataSetChanged();
        }
        symbols = symbol;
    }

    public void updateStock(Stock stock, int num) {
        if (!reloadFlag) {
            Log.d(TAG, "updateStock: !!!! " + num);
            if (num == 0) {

                disconnectDialog("add");
                return;
            }
            if (duplicate.contains(stock.getSymbol())) {    //Check duplicate
                dupDialog(stock.getSymbol());
                return;
            }
            duplicate.add(stock.getSymbol());
            stockList.add(0, stock);
            databaseHandler.addStock(stock);
            mAdapter.notifyDataSetChanged();
        } else {
            stockList.add(stock);
            mAdapter.notifyDataSetChanged();
        }
    }

    public ArrayList<String> numElements(String userInput) {
        ArrayList<String> stocks = new ArrayList<>();
        int count;
        for (Map.Entry<String, String> entry : symbols.entrySet()) {
            if (entry.getKey().contains(userInput)) {
                if (entry.getValue().equals("")) {
                    continue;
                }
                stocks.add(entry.getKey());
            }
            if (entry.getValue().toLowerCase().contains(userInput.toLowerCase())) {
                stocks.add(entry.getKey());
            }
        }
        return stocks;
    }

}




