package com.example.myvideo.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Subtitle {
    public float from;
    public float to;
    public int location;
    public String content;


    public static void main(String[] args) {
        String timeStr = "01";
        String url = "https://www.bilibili.com/video/BV1oE411M7YW";
        Pattern p = Pattern.compile(".*bilibili.com/video/([^\\?]+)*");
        Matcher m = p.matcher(url);
        if(m.matches()){
            System.out.println(m.group(1));
        }
    }
}
