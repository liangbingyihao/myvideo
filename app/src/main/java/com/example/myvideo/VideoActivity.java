package com.example.myvideo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.myvideo.model.Subtitle;
import com.example.myvideo.model.SubtitleList;
import com.example.myvideo.utils.ResultCallback;
import com.example.myvideo.utils.SubtitleService;
import com.example.myvideo.utils.Utils;
import com.example.myvideo.utils.VideoEnabledWebChromeClient;
import com.example.myvideo.utils.VideoEnabledWebView;
import com.example.myvideo.widget.ClickWordHelper;
import com.example.myvideo.widget.KtHelper;
import com.example.myvideo.widget.ScaleImage;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.lzf.easyfloat.interfaces.OnInvokeView;

import java.util.regex.Pattern;

import okhttp3.Request;

public class VideoActivity extends Activity {
    private static final String TAG = "VideoActivity";
    private static final String FLOAT_SUBTITLE = "SUBTITLE";
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
    private String playerTimeJs = "";
    private ValueCallback<String> mPlayerTime = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String s) {
            s = s.replace("\"", "");
            boolean isMatch = Pattern.matches(".*\\d{1,2}:\\d{1,2}.*", s);
            if (subtitleList != null) {
//                Log.d(TAG, String.format("From JS:%s,%b", s, isMatch)); // NEVER LOGGED on API 19-21
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
            ClickWordHelper.setText(subTitleView, subTitle);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // Save the web view
        webView = (VideoEnabledWebView) findViewById(R.id.webView);

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
        webView.loadUrl("https://www.bilibili.com/video/BV1cZ4y1W7rC");
//        webView.loadUrl("https://m.youtube.com/watch?v=tZ2P0b-UT_I");
//        webView.loadUrl("https://www.youtube.com/watch?v=r6sGWTCMz2k");
//        webView.loadUrl("file:///android_asset/index.html");

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
                                    if (!s.content.equals(subTitle)) {
//                                        Log.d(TAG, String.format("set subtitle:%f,%f,%d,%s,", s.from, s.to, second, s.content));
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
        KtHelper.registerActivityFloat(this, FLOAT_SUBTITLE, new OnFloatCallbacks() {
            @Override
            public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void show(@NonNull View view) {

            }

            @Override
            public void hide(@NonNull View view) {

            }

            @Override
            public void dragEnd(@NonNull View view) {

            }

            @Override
            public void drag(@NonNull View view, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void dismiss() {

            }

            @Override
            public void createdResult(boolean b, @Nullable String s, @Nullable View view) {
                subTitleView = view.findViewById(R.id.main_content);
                EasyFloat.hide(FLOAT_SUBTITLE);
            }
        });
//        subTitleView = EasyFloat.getFloatView(FLOAT_SUBTITLE).findViewById(R.id.main_content);
//        jsHandler.postDelayed(mHideFloat, 50);
    }

//    private void setOnClickSubtitle(TextView v) {
//        subTitleView = v;
//        subTitleView.setOnClickListener(onSubtitleClick);
//        subTitleView.setOnLongClickListener(onSubtitleLongClick);
//        SpannableStringBuilder s = new SpannableStringBuilder(subTitleView.getText());
//        for (int i = 0; i < s.length(); i++) {
//            s.setSpan(new ClickableSpan() {
//                @Override
//                public void onClick(View v) {
//                }
//
//                @Override
//                public void updateDrawState(TextPaint ds) {
//                    super.updateDrawState(ds);
//                    ds.setColor(0xff000000);       //设置文件颜色
//                    ds.setUnderlineText(false);      //设置下划线
//                }
//            }, i, i + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//        }
//        subTitleView.setText(s, TextView.BufferType.SPANNABLE);
//        subTitleView.setMovementMethod(LinkMovementMethod.getInstance());
//        subTitleView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //若没有绑定clickableSpan，无法使用subSequence方法
//                //若tv.getSelectionStart()-1,则输出点击的文字以及其上一个文字
//                //若tv.getSelectionEnd()+1,则输出点击的文字以及其下一个文字，如此类推
//                //通过标点判断还可截取一段文字中我们所点击的那句话
//                TextView tv = (TextView) v;
//                String s = tv
//                        .getText()
//                        .subSequence(tv.getSelectionStart(),
//                                tv.getSelectionEnd()).toString();
//                Log.d("tapped on:", s);
//            }
//        });
//    }
//
//    private View.OnClickListener onSubtitleClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            Log.d(TAG, "onSubtitleClick");
//        }
//
//    };
//    private View.OnLongClickListener onSubtitleLongClick = new View.OnLongClickListener() {
//        @Override
//        public boolean onLongClick(View view) {
//            Log.d(TAG, "onSubtitleLongClick");
//            return false;
//        }
//
//    };

    private Runnable mHideFloat = new Runnable() {
        @Override
        public void run() {
            EasyFloat.hide(FLOAT_SUBTITLE);
        }
    };

    private ResultCallback<SubtitleList> subtitleListCallback = new ResultCallback<SubtitleList>(true) {

        @Override
        public void onError(Request request, Exception e) {
            subtitleList = null;
            playerTimeJs = null;
            Log.d(TAG, "get subtitle error");
        }

        @Override
        public void onResponse(SubtitleList response) {
            if (response != null) {
                subtitleList = response;
                Log.d(TAG, "get subtitle:" + Thread.currentThread().getName() + Integer.toString(subtitleList.body.size()));
                EasyFloat.show(FLOAT_SUBTITLE);
                for (Subtitle s : subtitleList.body) {
//                    Log.d(TAG, s.getDetail());
                }
            }
        }
    };

    private class InsideWebViewClient extends WebViewClient {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//            Log.d(TAG, "shouldInterceptRequest:" + request.getUrl().toString());
            int subtitleType = SubtitleService.getSubtitleFromUrl(request.getUrl().toString(), subtitleListCallback);
            String js = SubtitleService.getPlayTimeJS(subtitleType);
            if (!TextUtils.isEmpty(js)) {
                playerTimeJs = js;
                Log.d(TAG, Thread.currentThread().getName() + " HIDE...");
                jsHandler.post(mHideFloat);
//                EasyFloat.hide(FLOAT_SUBTITLE);
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
