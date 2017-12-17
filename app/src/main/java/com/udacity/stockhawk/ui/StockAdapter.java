package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private final DecimalFormat mDollarFormat;
    private final DecimalFormat mDollarFormatWithPlus;
    private final DecimalFormat mPercentageFormat;
    private final StockAdapterOnClickHandler clickHandler;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        mContext = context;
        this.clickHandler = clickHandler;

        mDollarFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        mDollarFormatWithPlus = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        mDollarFormat.setPositivePrefix("$");
        mDollarFormatWithPlus.setPositivePrefix("+$");
        mPercentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        mPercentageFormat.setMaximumFractionDigits(2);
        mPercentageFormat.setMinimumFractionDigits(2);
        mPercentageFormat.setPositivePrefix("+");
    }

    void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(mContext).inflate(R.layout.list_item_quote, parent, false);
        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        String stockName = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_NAME));
        holder.mSymbolTextView.setText(mCursor.getString(Contract.Quote.POSITION_SYMBOL));
        holder.mSymbolTextView.setContentDescription(stockName);

        holder.mSymbolNameTextView.setText(mCursor.getString(Contract.Quote.POSITION_NAME));
        holder.mSymbolNameTextView.setContentDescription("\u00A0");

        holder.mPriceTextView.setText(mDollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE)));
        holder.mPriceTextView.setContentDescription(mContext.getString(R.string.a11y_stock_price,
                holder.mPriceTextView.getText()));

        float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            holder.mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            holder.mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

        String absoluteChangeValue = mDollarFormatWithPlus.format(rawAbsoluteChange);
        String percentageChangeValue = mPercentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(mContext)
                .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            holder.mChangeTextView.setText(absoluteChangeValue);
            holder.mChangeTextView.setContentDescription(mContext.getString(R.string.a11y_change_value,
                    absoluteChangeValue));
        } else {
            holder.mChangeTextView.setText(percentageChangeValue);
            holder.mChangeTextView.setContentDescription(mContext.getString(R.string.a11y_change_value,
                    percentageChangeValue));
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mCursor != null) {
            count = mCursor.getCount();
        }
        return count;
    }

    interface StockAdapterOnClickHandler {
        void onClick(String symbol,String symbolName);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol_text_view)
        TextView mSymbolTextView;

        @BindView(R.id.symbol_name)
        TextView mSymbolNameTextView;

        @BindView(R.id.price_text_view)
        TextView mPriceTextView;

        @BindView(R.id.change_text_view)
        TextView mChangeTextView;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int symbolColumn = mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            int symbolNameColumn = mCursor.getColumnIndex(Contract.Quote.COLUMN_NAME);
            clickHandler.onClick(mCursor.getString(symbolColumn),mCursor.getString(symbolNameColumn));
        }
    }
}
