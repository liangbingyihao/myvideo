package com.example.myvideo;


import android.app.Application;

import androidx.room.Room;
import okhttp3.OkHttpClient;

public class MyApplication extends Application {
    public static Application mainContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mainContext = this;
    }
}
