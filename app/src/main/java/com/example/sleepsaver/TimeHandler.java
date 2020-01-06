package com.example.sleepsaver;

public class TimeHandler {

    // 時分を表示する形式に整理するメソッド
    protected String[] timeString(int hour, int minute) {
        String[] timeSt = {"--", "--"};

        if(hour<10 && hour > -1){
            timeSt[0] = "0" + hour;
        }else if(hour != -1){
            timeSt[0] = String.valueOf(hour);
        }
        if(minute<10 && minute > -1){
            timeSt[1] = "0" + minute;
        }else if(minute != -1){
            timeSt[1] = String.valueOf(minute);
        }

        return timeSt;
    }
}
