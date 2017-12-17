package com.udacity.stockhawk.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.DailyStock;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;

class StockDetailAdapter extends RecyclerView.Adapter<StockDetailAdapter.StockDetailViewHolder> {
    private final Context mContext;
    private ArrayList<DailyStock> mDailyStock;
    private NumberFormat mNumberFormat;

    StockDetailAdapter(Context context,ArrayList<DailyStock> dailyStock) {
        mContext = context;
        mDailyStock = dailyStock;
        mNumberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    }

    void setData(ArrayList<DailyStock> dailyStock) {
        mDailyStock = dailyStock;
        notifyDataSetChanged();
    }

    @Override
    public StockDetailAdapter.StockDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(mContext).inflate(R.layout.stock_history_list_item, parent, false);
        return new StockDetailAdapter.StockDetailViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockDetailAdapter.StockDetailViewHolder holder, int position) {
        DailyStock dailyStock = mDailyStock.get(position);
        String closeValue = dailyStock.getCloseValue();
        String closeValueFloat = mNumberFormat.format(Float.parseFloat(closeValue));

        holder.mDateTextView.setText(dailyStock.getDate());
        holder.mCloseValueTextView.setText(closeValueFloat);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mDailyStock != null) {
            count = mDailyStock.size();
        }
        return count;
    }

    class StockDetailViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.stock_date_text_view)
        TextView mDateTextView;

        @BindView(R.id.stock_close_value_text_view)
        TextView mCloseValueTextView;

        StockDetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
