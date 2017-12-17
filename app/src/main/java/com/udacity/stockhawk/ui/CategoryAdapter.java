package com.udacity.stockhawk.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.udacity.stockhawk.R;

class CategoryAdapter extends FragmentPagerAdapter {

    private static int PAGE_COUNT = 2;
    private String tabTitles[];
    private Context mContext;

    CategoryAdapter(FragmentManager fm,Context context) {
       super(fm);
       mContext = context;
       tabTitles = new String[]{mContext.getResources().getString(R.string.summary_tab_heading),
                       mContext.getResources().getString(R.string.history_tab_heading)};
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new GraphFragment();
        } else {
            return new HistoricalDataFragment();
        }
    }

    @Override
    public int getCount() {
            return PAGE_COUNT;
        }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
