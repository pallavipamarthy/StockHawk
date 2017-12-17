package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.udacity.stockhawk.R;

public class StockDetailActivity extends AppCompatActivity  {

    private static final String INTENT_EXTRA_SYMBOL = "Symbol";
    private static final String INTENT_EXTRA_STOCK_NAME = "StockName";
    private String mSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSymbol = getIntent().getStringExtra(INTENT_EXTRA_SYMBOL);
        String symbolName = getIntent().getStringExtra(INTENT_EXTRA_STOCK_NAME);

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setIcon(R.drawable.app_icon);
            getSupportActionBar().setTitle(getString(R.string.detail_activity_title,symbolName));
        }

        setContentView(R.layout.activity_stock_detail);

        if(getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            CategoryAdapter categoryAdapter = new CategoryAdapter(getSupportFragmentManager(),this);
            viewPager.setAdapter(categoryAdapter);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
        } else {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public String getCurrentSymbol(){
        return mSymbol;
    }

    @Override
    public void onBackPressed() {
        Intent backPressedIntent = new Intent(this, MainActivity.class);
        backPressedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(backPressedIntent);
    }
}
