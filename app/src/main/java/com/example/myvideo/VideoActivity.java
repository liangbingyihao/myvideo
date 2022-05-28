package com.example.myvideo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myvideo.subtitle.Subtitle;
import com.example.myvideo.subtitle.SubtitleList;
import com.example.myvideo.utils.OkHttpUtils;
import com.example.myvideo.utils.ResultCallback;
import com.example.myvideo.utils.VideoEnabledWebChromeClient;
import com.example.myvideo.utils.VideoEnabledWebView;

import okhttp3.Request;

public class VideoActivity extends Activity
{
    private static String TAG = "VideoActivity";
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    private Handler jsHandler;
    private SubtitleList subtitleList;

    Runnable jsRun = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            jsHandler.postDelayed(this, 2000);
            webView.evaluateJavascript(
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Save the web view
        webView = (VideoEnabledWebView)findViewById(R.id.webView);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup)findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress)
            {
                // Your code...
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                }
                else
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        jsHandler = new Handler(getMainLooper());
        webView.setWebChromeClient(webChromeClient);
        // Call private class InsideWebViewClient
        webView.setWebViewClient(new InsideWebViewClient());

        // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
        webView.loadUrl("https://www.bilibili.com/video/BV1oE411M7YW?t=22");

    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onLoadResource(WebView view,
                                   String url) {
            if (url.endsWith("json")) {
                OkHttpUtils.getInstace().doGet(url, new ResultCallback<SubtitleList>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(SubtitleList response) {
                        subtitleList = response;
                        for (Subtitle s : response.body) {
                            Log.d(TAG, s.from+","+s.to+","+s.location+","+s.content);
                        }
                    }
                });
            }
            super.onLoadResource(view, url);
        }
    }

    @Override
    public void onBackPressed()
    {
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed())
        {
            if (webView.canGoBack())
            {
                webView.goBack();
            }
            else
            {
                // Standard back button implementation (for example this could close the app)
                super.onBackPressed();
            }
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
//        getClipboardData();
        jsHandler.postDelayed(jsRun, 2000);
    }
}
