package com.udacity.stockhawk.widget;


import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockWidgetRemoteViewService extends RemoteViewsService {

    private static final String INTENT_EXTRA_SYMBOL = "Symbol";
    private static final String INTENT_EXTRA_STOCK_NAME = "StockName";
    private DecimalFormat mDollarFormatWithPlus;
    private DecimalFormat mDollarFormat;
    private static final String[] STOCK_COLUMNS = {
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_NAME,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE
    };

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI, STOCK_COLUMNS, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),R.layout.widget_stock_list_item);
                mDollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                mDollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                mDollarFormatWithPlus.setPositivePrefix("+$");
                String symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                String symbolName = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_NAME));
                float price = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                float absoluteChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                String formattedPrice = mDollarFormat.format(price);
                String formattedChange = mDollarFormatWithPlus.format(absoluteChange);

                views.setTextViewText(R.id.widget_symbol, symbol);
                views.setTextViewText(R.id.widget_price, formattedPrice);

                if (absoluteChange > 0) {
                    views.setInt(R.id.widget_change,"setBackgroundResource",R.drawable.percent_change_pill_green);
                } else {
                   views.setInt(R.id.widget_change,"setBackgroundResource",R.drawable.percent_change_pill_red);
                }
                views.setTextViewText(R.id.widget_change, formattedChange);
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(INTENT_EXTRA_SYMBOL,symbol);
                fillInIntent.putExtra(INTENT_EXTRA_STOCK_NAME,symbolName);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_stock_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(data.getColumnIndex(Contract.Quote._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

