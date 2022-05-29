package com.example.myvideo.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class SubtitleInfo {
    public String lan;
    @SerializedName("lan_doc")
    public String lanName;
    @SerializedName("subtitle_url")
    public String url;
}

public class BiliVideo {
    public JsonObject data;
    public ArrayList<SubtitleInfo> subtitles;

    public String getSubtitle(String lan) {
        if (subtitles == null && data != null) {
            Gson gson = new Gson();
            JsonArray a = data.getAsJsonObject("subtitle").getAsJsonArray("subtitles");
            subtitles = gson.fromJson(a, new TypeToken<List<SubtitleInfo>>() {
            }.getType());
            for (SubtitleInfo s : subtitles) {
                if (s.url.startsWith("//")) {
                    s.url = "https:" + s.url;
                }
            }
        }
        for (SubtitleInfo s : subtitles) {
            if (s.lan.indexOf(lan) >= 0) {
                return s.url;
            }
        }
        return "";
    }

    public static void main(String[] args) {
        String url = "https://api.bilibili.com/x/player/v2?cid=144914957&aid=84745697&ep_id=0&season_id=0";
        Request request = new Request.Builder().url(url).get().build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response rsp = client.newCall(request).execute();
            String json = rsp.body().string();
            Gson gson = new Gson();
            BiliVideo o = gson.fromJson(json, BiliVideo.class);
            System.out.println(o.getSubtitle("en"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
