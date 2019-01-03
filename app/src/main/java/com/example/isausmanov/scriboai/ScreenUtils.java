package com.example.isausmanov.scriboai;


import android.content.Context;
import android.util.TypedValue;


public class ScreenUtils {

    public static int dp2px(Context context, int dp){
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }

}
