package com.example.myvideo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.webkit.WebViewAssetLoader;

import com.example.myvideo.model.BiliVideo;
import com.example.myvideo.model.Subtitle;
import com.example.myvideo.model.SubtitleList;
import com.example.myvideo.utils.Constants;
import com.example.myvideo.utils.LocalContentWebViewClient;
import com.example.myvideo.utils.OkHttpUtils;
import com.example.myvideo.utils.ResultCallback;
import com.example.myvideo.utils.SubtitleService;
import com.example.myvideo.utils.Utils;
import com.example.myvideo.utils.VideoEnabledWebChromeClient;
import com.example.myvideo.utils.VideoEnabledWebView;

import java.util.regex.Pattern;

import okhttp3.Request;

public class VideoActivity extends Activity {
    private static String TAG = "VideoActivity";
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    private Handler jsHandler;
    private SubtitleList subtitleList;
    private HandlerThread subtitleThread;
    private Handler subtitleHandler;
    private final int MSG_TIME = 0;
    private String subTitle;
    private TextView subTitleView;
    private String videoJs;
    private String playerTimeJs="";
    private ValueCallback<String> mPlayerTime = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String s) {
            s = s.replace("\"", "");
            boolean isMatch = Pattern.matches(".*\\d{1,2}:\\d{1,2}.*", s);
            if(subtitleList!=null){
                Log.d(TAG, String.format("From JS:%s,%b",s,isMatch)); // NEVER LOGGED on API 19-21
            }
            if (!isMatch) {
                return;
            }
            Message msg = Message.obtain();
            msg.what = MSG_TIME;
            msg.arg1 = Utils.timeStr2Seconds(s.replace("\"", ""));
            subtitleHandler.removeMessages(MSG_TIME);
            subtitleHandler.sendMessage(msg);
        }
    };

    Runnable jsRun = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            jsHandler.postDelayed(this, 2000);
            if (!TextUtils.isEmpty(playerTimeJs)) {
//                Log.d(TAG, playerTimeJs);
                webView.evaluateJavascript(playerTimeJs, mPlayerTime);
            }
        }
    };
    Runnable subTitleRun = new Runnable() {
        @Override
        public void run() {
            subTitleView.setText(subTitle);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Save the web view
        webView = (VideoEnabledWebView) findViewById(R.id.webView);
        subTitleView = findViewById(R.id.subtitle);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Your code...
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen) {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                } else {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        jsHandler = new Handler(getMainLooper());
        webView.setWebChromeClient(webChromeClient);
        // Call private class InsideWebViewClient
//        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
//                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
//                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
//                .build();
        webView.setWebViewClient(new InsideWebViewClient());

        // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
//        webView.loadUrl("https://www.bilibili.com/video/BV1U7411a7xG");
//        webView.loadUrl("https://m.youtube.com/watch?v=tZ2P0b-UT_I");
//        webView.loadUrl("https://www.youtube.com/watch?v=r6sGWTCMz2k");
        webView.loadUrl("file:///android_asset/index.html");

        subtitleThread = new HandlerThread("SubtitleThread");
        subtitleThread.start();
        subtitleHandler = new Handler(subtitleThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_TIME:
                        int second = msg.arg1;
                        if (subtitleList != null) {
                            for (Subtitle s : subtitleList.body) {
                                if (s.from < second && s.to > second) { // NEVER LOGGED on API 19-21
                                    jsHandler.removeCallbacks(subTitleRun);
                                    if(!s.content.equals(subTitle)){
                                        Log.d(TAG, String.format("set subtitle:%f,%f,%d,%s,",s.from,s.to,second,s.content));
                                        subTitle = s.content;
                                        jsHandler.post(subTitleRun);
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
        videoJs = Utils.getAssetsData(this, "video.js");
    }

    private ResultCallback<SubtitleList> subtitleListCallback = new ResultCallback<SubtitleList>(false) {

        @Override
        public void onError(Request request, Exception e) {
            subtitleList = null;
            playerTimeJs = null;
        }

        @Override
        public void onResponse(SubtitleList response) {
            if (response != null) {
                subtitleList = response;
                for (Subtitle s : subtitleList.body) {
                    Log.d(TAG, s.getDetail());
                }
            }
        }
    };

//    private ResultCallback<BiliVideo> biliVideoCallback = new ResultCallback<BiliVideo>() {
//
//        @Override
//        public void onResponse(BiliVideo response) {
//            if (response != null) {
//                String enSubtitle = response.getSubtitle("en");
//                if (!TextUtils.isEmpty(enSubtitle)) {
//                    OkHttpUtils.getInstace().doGet(enSubtitle, subtitleListCallback);
//                }
//            }
//
//        }
//    };

    private class InsideWebViewClient extends WebViewClient {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            int subtitleType = SubtitleService.getSubtitleFromUrl(request.getUrl().toString(), subtitleListCallback);
            String js = SubtitleService.getPlayTimeJS(subtitleType);
            if (!TextUtils.isEmpty(js)) {
                playerTimeJs = js;
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            view.loadUrl("file:///android_asset/video.js");
//            view.loadUrl("javascript:console.log(finish:" + url + ");");
            view.loadUrl("javascript:" + VideoActivity.this.videoJs);
        }
    }

    @Override
    public void onBackPressed() {
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
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
