package com.example.myvideo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class StudyKotlin {

    void test(Context context){

        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
    }
}
