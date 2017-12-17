package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.DailyStock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoricalDataFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.stock_history_recycler_view) RecyclerView mStockHistoryRecyclerView;
    private ArrayList<DailyStock> mDailyStock;
    private Context mContext;
    private static final int DETAIL_STOCK_LOADER = 0;
    private StockDetailAdapter mDetailAdapter;

    public HistoricalDataFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stock_history_recyclerview, container, false);
        ButterKnife.bind(this,rootView);
        mContext = getActivity();
        mDailyStock = new ArrayList<>();
        if(getActivity() != null) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL, false);
            mStockHistoryRecyclerView.setLayoutManager(mLayoutManager);
            getLoaderManager().initLoader(DETAIL_STOCK_LOADER, null,this);
            mDetailAdapter = new StockDetailAdapter(mContext,mDailyStock);
            mStockHistoryRecyclerView.setAdapter(mDetailAdapter);
            mDetailAdapter.setData(mDailyStock);
        }
        return rootView;
    }

    @Override
    public void onResume() {super.onResume();}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String symbol = ((StockDetailActivity)getActivity()).getCurrentSymbol();
        String[] selectionArgs = {symbol};
        return new CursorLoader(mContext,
                Contract.Quote.URI,
                null,
                Contract.Quote.COLUMN_SYMBOL + " = ?",
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor data) {

        String history = "";
        if(data != null && data.moveToFirst()) {
            history = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        }
        String[] stockHistory = history.split("\n");
        DailyStock stockForADay;

        for (String str:stockHistory) {
            String[] stockHistoryForDay = str.split(",");
            String dateInMillis = stockHistoryForDay[0];
            String closeValue = stockHistoryForDay[1];
            int indexOfDecimal = closeValue.indexOf('.');
            String finalCloseValue;
            if (indexOfDecimal != -1) {
                finalCloseValue = closeValue.substring(0, indexOfDecimal + 3);
            } else {
                finalCloseValue = closeValue;
            }

            String date = formatDate(dateInMillis);
            stockForADay = new DailyStock(date,finalCloseValue);
            mDailyStock.add(stockForADay);
        }
        mDetailAdapter.setData(mDailyStock);
        getLoaderManager().destroyLoader(DETAIL_STOCK_LOADER);
    }

    public String formatDate(String dateInMillis){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Long date = Long.parseLong(dateInMillis);
        calendar.setTimeInMillis(date);
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDetailAdapter.setData(null);
    }
}
