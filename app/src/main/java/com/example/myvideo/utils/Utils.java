package com.example.myvideo.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class Utils {
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            return new Point(display.getWidth(), display.getHeight());
        } else {
            Point outSize = new Point();
            display.getSize(outSize);
            return outSize;
        }
    }
}
