package com.example.myvideo.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Subtitle {
    public float from;
    public float to;
    public int location;
    public String content;
    public String[] segs;

    public String getDetail(){
        return String.format("%f~%f:%s",from,to,content);
    }

    public static void main(String[] args) {
        String url = "https://m.youtube.com/api/timedtext?v=tZ2P0b-UT_I&caps=asr&xoaf=5&hl=zh-CN&ip=0.0.0.0&ipbits=0&expire=1654272001&sparams=ip,ipbits,expire,v,caps,xoaf&signature=6BCB128C940595EE7A8B556CCFD28048244E0DDD.C270B662DB54F38ED1D11B2ED5B422B42D2CD269&key=yt8&kind=asr&lang=en&fmt=json3&xorb=2&xobt=3&xovt=3";
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        Request request = new Request.Builder().url(url).get().build();
//        try {
//            Response rsp = client.newCall(request).execute();
//            JsonObject o = gson.fromJson(rsp.body().string(), JsonObject.class);
//            System.out.println(o);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        client.newCall(request).enqueue(new Callback() {
            //这些okhttp的回调方法执行在子线程里面，返回的数据为了给主线程使用，
            // 必须想办法把数据供给主线程使用，所以引用了自定义的回调接口
            @Override
            public void onFailure(final Call call, final IOException e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                JsonObject data = gson.fromJson(response.body().string(), JsonObject.class);
                JsonArray a = data.getAsJsonArray("events");
                ArrayList<Subtitle> subList = new ArrayList<Subtitle>();
                for (Iterator<JsonElement> it = a.iterator(); it.hasNext(); ) {
                    JsonObject s = it.next().getAsJsonObject();
                    JsonArray segs = s.getAsJsonArray("segs");
                    if (segs == null) {
                        continue;
                    } else if (segs.size() == 1 && "\n".equals(segs.get(0).getAsJsonObject().get("utf8").getAsString())) {
                        continue;
                    } else {
                        Subtitle subtitle = new Subtitle();
                        subtitle.from = s.get("tStartMs").getAsFloat() / 60;
                        subtitle.to = subtitle.from + s.get("dDurationMs").getAsFloat() / 60;
                        subtitle.segs = new String[segs.size()];
                        int i=0;
                        for (Iterator<JsonElement> it_s = segs.iterator(); it_s.hasNext();) {
                            subtitle.segs[i]=it_s.next().getAsJsonObject().get("utf8").getAsString();
                            i++;
                        }
                        subList.add(subtitle);
                    }

                }
            }
        });
    }
}
