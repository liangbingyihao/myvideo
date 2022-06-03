package com.example.myvideo.utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.myvideo.model.BiliVideo;
import com.example.myvideo.model.Subtitle;
import com.example.myvideo.model.SubtitleList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Request;

public class SubtitleService {
    private final static int TYPE_BILI = 1;
    private final static int TYPE_YOUTUBE = 2;

    public static String getPlayTimeJS(int type) {
        switch (type) {
            case TYPE_BILI:
                return "(function(){return document.getElementsByClassName('mplayer-control-text mplayer-time-current-text')[0].innerHTML})()";
            case TYPE_YOUTUBE:
                return "(function(){return document.getElementsByClassName('time-first')[0].innerHTML})()";
            default:
                return "";
        }
    }

    public static int getSubtitleFromUrl(String url, ResultCallback<SubtitleList> callback) {
        if (url.indexOf("api.bilibili.com/x/player/v2") > 0) {
            OkHttpUtils.getInstace().doGet(url, new ResultCallback<BiliVideo>(false) {

                @Override
                public void onError(Request request, Exception e) {
                    callback.onError(null, null);
                }

                @Override
                public void onResponse(BiliVideo response) {
                    if (response != null) {
                        String enSubtitle = response.getSubtitle("en");
                        if (!TextUtils.isEmpty(enSubtitle)) {
                            OkHttpUtils.getInstace().doGet(enSubtitle, callback);
                        }
                    }

                }
            });
            return TYPE_BILI;
        } else if (url.indexOf("/api/timedtext") > 0) {
            OkHttpUtils.getInstace().doGet(url, new ResultCallback<JsonObject>(false) {
                @Override
                public void onError(Request request, Exception e) {
                    callback.onError(null, null);
                }

                @Override
                public void onResponse(JsonObject response) {
                    if (response != null) {
                        SubtitleList result = new SubtitleList();
                        JsonArray a = response.getAsJsonArray("events");
                        result.body = new ArrayList<Subtitle>();
                        for (Iterator<JsonElement> it = a.iterator(); it.hasNext(); ) {
                            JsonObject s = it.next().getAsJsonObject();
                            JsonArray segs = s.getAsJsonArray("segs");
                            if (segs == null) {
                                continue;
                            } else if (segs.size() == 1 && "\n".equals(segs.get(0).getAsJsonObject().get("utf8").getAsString())) {
                                continue;
                            } else {
                                Subtitle subtitle = new Subtitle();
                                subtitle.from = s.get("tStartMs").getAsFloat() / 1000;
                                subtitle.to = subtitle.from + s.get("dDurationMs").getAsFloat() / 1000;
                                subtitle.segs = new String[segs.size()];
                                int i = 0;
                                StringBuilder sb = new StringBuilder();
                                for (Iterator<JsonElement> it_s = segs.iterator(); it_s.hasNext(); ) {
                                    String word = it_s.next().getAsJsonObject().get("utf8").getAsString();
                                    subtitle.segs[i] = word;
                                    i++;
                                    sb.append(word).append(" ");
                                }
                                subtitle.content = sb.toString();
                                result.body.add(subtitle);
                            }

                        }
                        callback.onResponse(result);
                    } else {
                        callback.onError(null, null);
                    }

                }
            });
            return TYPE_YOUTUBE;
        }
        return 0;
    }
}
