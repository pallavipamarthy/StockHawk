package com.udacity.stockhawk.data;

public class DailyStock {
    private String mDate;
    private String mCloseValue;

    public DailyStock(String date,String closeValue){
        mDate = date;
        mCloseValue = closeValue;
    }
    public String getDate(){
        return mDate;
    }
    public String getCloseValue(){
        return mCloseValue;
    }
}
