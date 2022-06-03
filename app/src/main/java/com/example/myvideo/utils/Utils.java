package com.example.myvideo.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

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
