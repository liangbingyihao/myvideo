package com.example.myvideo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.webkit.WebResourceRequest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.myvideo.model.Subtitle;
import com.example.myvideo.model.SubtitleList;
import com.example.myvideo.utils.ClipBoardUtil;
import com.example.myvideo.utils.OkHttpUtils;
import com.example.myvideo.utils.ResultCallback;

import okhttp3.Request;

public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private static String TAG = "MainActivity";
    Handler jsHandler;
    Runnable jsRun = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            jsHandler.postDelayed(this, 2000);
            mWebView.evaluateJavascript(
                    "(function(){return document.getElementsByClassName('mplayer-control-text mplayer-time-current-text')[0].innerHTML})()",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.d(TAG, "From JS: " + s); // NEVER LOGGED on API 19-21
                        }
                    });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webView);

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyChrome());    // here
        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true); // 关键点
        settings.setAllowFileAccess(true); // 允许访问文件
        settings.setSupportZoom(true); // 支持缩放
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);

//        if (savedInstanceState == null) {
//            mWebView.loadUrl("https://www.bilibili.com/video/BV1oE411M7YW?t=22");
//        }
        jsHandler = new Handler(getMainLooper());
    }

    private Runnable mClipRunnable = new Runnable() {
        @Override
        public void run() {
            //把获取到的内容打印出来
            String url = ClipBoardUtil.paste();
            ClipBoardUtil.clear();
            if (!TextUtils.isEmpty(url)) {
                int start = url.indexOf("http");
                if(start>=0){
                    url = url.substring(start);
                    Toast.makeText(MainActivity.this, url, Toast.LENGTH_SHORT).show();
                    mWebView.loadUrl(url);
                }
            }
        }
    };

    //获取剪切板内容
    private void getClipboardData() {
        this.getWindow().getDecorView().post(mClipRunnable);
    }

    private static class MyWebViewClient extends WebViewClient {
        @Override
        public void onLoadResource(WebView view,
                                   String url) {
            if (url.endsWith("json")) {
                Log.d(TAG, "onLoadResource: " + url);
                OkHttpUtils.getInstace().doGet(url, new ResultCallback<SubtitleList>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(SubtitleList response) {
                        for (Subtitle s : response.body) {
                            Log.d(TAG, s.getDetail());
                        }
                    }
                });
            }
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            Log.d(TAG, "shouldOverrideUrlLoading2: " + request.getUrl().toString());
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    private class MyChrome extends WebChromeClient {

        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome() {
        }

        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView,
                                     WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView,
                    new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView()
                    .setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        jsHandler.removeCallbacks(jsRun);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getClipboardData();
        jsHandler.postDelayed(jsRun, 2000);
    }
}
