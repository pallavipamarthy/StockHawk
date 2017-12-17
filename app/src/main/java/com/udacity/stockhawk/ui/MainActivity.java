package com.udacity.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import java.util.Set;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final String INTENT_EXTRA_SYMBOL = "Symbol";
    private static final String INTENT_EXTRA_STOCK_NAME = "StockName";
    private static final int STOCK_LOADER = 0;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView mStockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error_text_view) TextView mErrorTextView;
    @BindView(R.id.error_image) ImageView mErrorImageView;
    private NetworkInfoReceiver mNetworkInfoReceiver;
    private StockAdapter mStockAdapter;

    @Override
    public void onClick(String symbol,String symbolName) {
        Intent intent = new Intent(this,StockDetailActivity.class);
        intent.putExtra(INTENT_EXTRA_SYMBOL,symbol);
        intent.putExtra(INTENT_EXTRA_STOCK_NAME,symbolName);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setIcon(R.drawable.app_icon);
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
        ButterKnife.bind(this);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mStockAdapter = new StockAdapter(this, this);
        mStockRecyclerView.setAdapter(mStockAdapter);
        mStockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = mStockAdapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
                MainActivity.this.sendBroadcast(dataUpdatedIntent);
                if(PrefUtils.getStocks(MainActivity.this).size()==0){
                    onRefresh();
                }
            }
        }).attachToRecyclerView(mStockRecyclerView);

        mNetworkInfoReceiver = new NetworkInfoReceiver();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter symbolFilter = new IntentFilter(QuoteSyncJob.ACTION_WRONG_SYMBOL);
        IntentFilter dataFilter = new IntentFilter(QuoteSyncJob.ACTION_DATA_UNAVAILABLE);
        registerReceiver(mNetworkInfoReceiver,networkFilter);
        registerReceiver(mNetworkInfoReceiver,symbolFilter);
        registerReceiver(mNetworkInfoReceiver,dataFilter);
    }

    private boolean networkUp() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(this);
        if (!networkUp()) {
            if (PrefUtils.getStocks(MainActivity.this).size() == 0
                    || mStockAdapter.getItemCount() == 0) {
                mErrorTextView.setVisibility(View.VISIBLE);
                mErrorImageView.setVisibility(View.VISIBLE);
                mErrorTextView.setText(getString(R.string.error_no_network));
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                mErrorTextView.setVisibility(View.INVISIBLE);
                mErrorImageView.setVisibility(View.INVISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
            }
        } else if (PrefUtils.getStocks(this).size() == 0) {
            mErrorTextView.setVisibility(View.VISIBLE);
            mErrorImageView.setVisibility(View.INVISIBLE);
            mErrorTextView.setText(getString(R.string.error_no_stocks));
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            mErrorImageView.setVisibility(View.INVISIBLE);
            mErrorTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if(symbol.equals("")){
            Toast.makeText(this,R.string.empty_symbol_toast,Toast.LENGTH_SHORT).show();
        } else if(!symbol.matches("[a-zA-Z ]+")){
            Toast.makeText(this,R.string.invalid_symbol_toast,Toast.LENGTH_SHORT).show();
        } else if (symbol != null && !symbol.isEmpty()) {
            Set<String> currentStockList = PrefUtils.getStocks(this);
            if(currentStockList.contains(symbol.toUpperCase())) {
                String message = getString(R.string.toast_stock_already_added, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                return;
            }
            if (networkUp()) {
                mErrorTextView.setVisibility(View.INVISIBLE);
                mErrorImageView.setVisibility(View.INVISIBLE);
                mSwipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            PrefUtils.addStock(this, symbol.toUpperCase());
            QuoteSyncJob.syncImmediately(this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (data.getCount() != 0){
            mErrorTextView.setVisibility(View.GONE);
            mErrorImageView.setVisibility((View.GONE));
        }
        mStockAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        mStockAdapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this).equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
            item.setTitle(getString(R.string.a11y_percent_mode));
        } else {
            item.setIcon(R.drawable.ic_dollar);
            item.setTitle(getString(R.string.a11y_absolute_mode));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportActionBar().setIcon(R.drawable.app_icon);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            mStockAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class NetworkInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                onRefresh();
            }
            else if(QuoteSyncJob.ACTION_WRONG_SYMBOL.equals(intent.getAction())){
                Toast.makeText(context,R.string.wrong_symbol_toast, Toast.LENGTH_LONG).show();
            }
            else if(QuoteSyncJob.ACTION_DATA_UNAVAILABLE.equals(intent.getAction())){
                Toast.makeText(context,R.string.data_unavailable_for_symbol, Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkInfoReceiver);
    }
}
