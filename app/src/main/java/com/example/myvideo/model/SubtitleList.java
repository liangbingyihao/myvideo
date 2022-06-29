package com.example.myvideo.model;

import java.util.ArrayList;

public class SubtitleList {
    public ArrayList<Subtitle> body;
    private int lastSecond = 0;
    private int start, end = -1;

    public String getSubtitleFromSecond(int second) {
        //返回Null表示不需要调整
        if (second == lastSecond || second <= 0) {
            return null;
        }
        int where = -1;
        for (int i = 0; i < body.size(); i++) {
            Subtitle s = body.get(i);
            if (s.from < second && s.to > second) {
                where = i;
                break;
            }
        }
        if ((where >= start && where <= end) || where <= 0) {
            return null;
        }
        end = where;
        int diff = second-lastSecond;
        if (start >= 0 && (end-start) > 4) {
            start = Math.max(0, end - 2);
        }else if(start<0 || start>end){
            start = end;
        }
        lastSecond = second;
        StringBuilder sb = new StringBuilder();
        sb.append(start).append(",").append(end).append(",");
        for (int i = start; i <= end; i++) {
            sb.append(body.get(i).content).append(" ");
        }
        return sb.toString();
    }
}
