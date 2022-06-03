package com.example.myvideo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myvideo.MyApplication;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {
    private static OkHttpUtils okHttpUtils;
    private final Gson gson;
    private final OkHttpClient client;
    private final Handler handler;
    private final String TAG = OkHttpUtils.class.getName();

    private OkHttpUtils() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .cache(new Cache(MyApplication.mainContext.getCacheDir(), 25 * 1024 * 1024));
        builder.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request request = chain.request();
                long t1 = System.nanoTime();
                Log.d(TAG, String.format("Sending request %s on %s%n%s", request.url(),
                        chain.connection(), request.headers()));

                Response response = chain.proceed(request);
                long t2 = System.nanoTime();
                Log.d(TAG, String.format("Received response for %s in %.1fms%n%s",
                        response.request().url(), (t2 - t1) / 1e6d, response.headers()));
                return response;
            }
        });
        client = builder.build();
        gson = new Gson();
        handler = new Handler(Looper.getMainLooper());
    }

    public static OkHttpUtils getInstace() {

        if (okHttpUtils == null) {
            synchronized (OkHttpUtils.class) {
                if (okHttpUtils == null) {
                    okHttpUtils = new OkHttpUtils();
                }
            }
        }
        return okHttpUtils;
    }

    public void doGet(String url, ResultCallback callback) {
        Request request = new Request.Builder().url(url).get().build();
        doRequest(request, callback);
    }

    //post方法. username  password
    public void doPost(String url, Map<String, String> params, ResultCallback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody body = formBuilder.build();
        Request.Builder builder = new Request.Builder().url(url).post(body);//拦截器
        Request request = builder.build();
        doRequest(request, callback);
    }


    private void doRequest(Request request, final ResultCallback callback) {
        callback.onBefore(request);
        client.newCall(request).enqueue(new Callback() {
            //这些okhttp的回调方法执行在子线程里面，返回的数据为了给主线程使用，
            // 必须想办法把数据供给主线程使用，所以引用了自定义的回调接口
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (callback.ui) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(request, e);
                        }
                    });
                } else {
                    callback.onError(request, e);
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    //返回数据成功的话就解析json串
                    String json = response.body().string();
                    final Object o = gson.fromJson(json, callback.mType);//将json解析成对应的bean
                    if (callback.ui) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //将response返回给主线程
                                callback.onResponse(o);
                            }
                        });
                    } else {
                        callback.onResponse(o);
                    }
                } else {
                    if (callback.ui) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(request, null);
                            }
                        });
                    } else {
                        callback.onError(request, null);
                    }
                }
            }
        });
    }
}
