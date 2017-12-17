package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.DailyStock;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GraphFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.stock_symbol) TextView mDetailSymbolTextView;
    @BindView(R.id.stock_name) TextView mDetailSymbolNameView;
    @BindView(R.id.stock_currency) TextView mDetailCurrencyView;
    @BindView(R.id.stock_open) TextView mDetailOpenTextView;
    @BindView(R.id.stock_prev_close) TextView mDetailPreviousCloseView;
    @BindView(R.id.stock_day_high) TextView mDetailDayHighView;
    @BindView(R.id.stock_day_low) TextView mDetailDayLowView;
    @BindView(R.id.stock_year_high) TextView mDetailYearHighView;
    @BindView(R.id.stock_year_low) TextView mDetailYearLowView;
    @BindView(R.id.chart) LineChart mLineChart;

    private NumberFormat mNumberFormat;
    private ArrayList<DailyStock> mDailyStock;
    private Context mContext;
    private static final int GRAPH_STOCK_LOADER = 1;
    private String mSymbolName;
    private ArrayList<String> mLabels;

    public GraphFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.graph_layout, container, false);
        ButterKnife.bind(this,rootView);
        mContext = getActivity();
        mDailyStock = new ArrayList<>();
        mLabels = new ArrayList<>();
        mNumberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        getLoaderManager().initLoader(GRAPH_STOCK_LOADER, null, this);
        return rootView;
    }

    private LineDataSet getDataSet() {

        ArrayList<Entry> graphEntryList = new ArrayList<>();
        int count = mDailyStock.size();
        for(int i=count-1;i>=0;i--) {
            String closeValueInString = mDailyStock.get(i).getCloseValue();
            float closeValue = Float.parseFloat(closeValueInString);
            Entry entry = new Entry(closeValue,count-i-1);
            graphEntryList.add(entry);
        }

        LineDataSet dataSet = new LineDataSet(graphEntryList,mSymbolName);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(mContext,R.color.graph_gradient));
        return dataSet;
    }

    private ArrayList<String> getXLabels(){
        int count = mDailyStock.size();
        for(int i=count-1;i>=0;i--){
            String label = mDailyStock.get(i).getDate();
            mLabels.add(label);
        }
        return mLabels;
    }

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
        mSymbolName = "";
        String symbol = "";
        float prevClose = 0;
        float open = 0;
        float dayHigh = 0;
        float dayLow = 0;
        float yearHigh = 0;
        float yearLow = 0;
        String currency = "";

        if(data != null && data.moveToFirst()) {
            history = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            mSymbolName = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_NAME));
            symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
            currency = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_CURRENCY));
            dayHigh = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_DAYHIGH));
            dayLow = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_DAYLOW));
            yearHigh = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_YEARHIGH));
            yearLow = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_YEARLOW));
            open = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_OPEN));
            prevClose = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PREVCLOSE));
        }

        mDetailSymbolNameView.setText(mSymbolName);
        mDetailSymbolTextView.setText(symbol);
        mDetailCurrencyView.setText(currency);
        mDetailDayHighView.setText(String.valueOf(mNumberFormat.format(dayHigh)));
        mDetailDayLowView.setText(String.valueOf(mNumberFormat.format(dayLow)));
        mDetailPreviousCloseView.setText(String.valueOf(mNumberFormat.format(prevClose)));
        mDetailOpenTextView.setText(String.valueOf(mNumberFormat.format(open)));
        mDetailYearHighView.setText(String.valueOf(mNumberFormat.format(yearHigh)));
        mDetailYearLowView.setText(String.valueOf(mNumberFormat.format(yearLow)));

        String[] stockHistory = history.split("\n");
        DailyStock stockForADay;

        for (String str : stockHistory) {
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
            stockForADay = new DailyStock(date, finalCloseValue);
            mDailyStock.add(stockForADay);
        }

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setLabelsToSkip(25);
        LineData linedata = new LineData(getXLabels(),getDataSet());
        mLineChart.setData(linedata);
        mLineChart.setDescription(getResources().getString(R.string.graph_description));
        mLineChart.setPinchZoom(false);
        mLineChart.setScaleEnabled(false);
        mLineChart.animateX(3000);
        mLineChart.getData().setHighlightEnabled(false);
        mLineChart.getLegend().setTextColor(ContextCompat.getColor(mContext,R.color.graph_values));
        mLineChart.getXAxis().setTextColor(ContextCompat.getColor(mContext,R.color.graph_values));
        mLineChart.getAxisLeft().setTextColor(ContextCompat.getColor(mContext,R.color.graph_values));
        mLineChart.getAxisRight().setTextColor(ContextCompat.getColor(mContext,R.color.graph_values));
        mLineChart.invalidate();
        getLoaderManager().destroyLoader(GRAPH_STOCK_LOADER);
    }
    private String formatDate(String dateInMillis){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM, yyyy",Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Long date = Long.parseLong(dateInMillis);
        calendar.setTimeInMillis(date);
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
