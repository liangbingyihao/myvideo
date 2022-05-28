package com.example.myvideo.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.example.myvideo.MyApplication;

public class ClipBoardUtil {
    /**
     * 获取剪切板内容
     * @return
     */
    public static String paste(){
        ClipboardManager manager = (ClipboardManager) MyApplication.mainContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
                CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
                String addedTextString = String.valueOf(addedText);
                if (!TextUtils.isEmpty(addedTextString)) {
                    return addedTextString;
                }
            }
        }
        return "";
    }

    /**
     * 清空剪切板
     */
    public static void clear(){
        ClipboardManager manager = (ClipboardManager) MyApplication.mainContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText("",""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
