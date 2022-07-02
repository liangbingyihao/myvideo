package com.example.myvideo.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static Point getDisplaySize(Context context) {
        DisplayMetrics dm = null;
        try {
            dm = new DisplayMetrics();
            WindowManager localWindowManager =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            localWindowManager.getDefaultDisplay().getRealMetrics(dm);
            return new Point(dm.widthPixels, dm.heightPixels);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Point(400, 800);
    }

    public static int timeStr2Seconds(String timeStr) {
        if (timeStr == null) {
            return 0;
        }
        String ts[] = timeStr.split(":");
        int seconds = 0;
        for (int i = ts.length; i > 0; i--) {
            seconds += Integer.parseInt(ts[i - 1]) * Math.pow(60, ts.length - i);
        }
        return seconds;
    }

    public static String getAssetsData(Context context, String path) {
        String result = "";
        try {
            //获取输入流
            InputStream mAssets = context.getAssets().open(path);

            //获取文件的字节数
            int lenght = mAssets.available();
            //创建byte数组
            byte[] buffer = new byte[lenght];
            //将文件中的数据写入到字节数组中
            mAssets.read(buffer);
            mAssets.close();
            result = new String(buffer);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("fuck", "error");
            return result;
        }
    }
}
