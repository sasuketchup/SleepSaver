package com.example.sleepsaver;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

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

    // 記録し忘れの対応処理メソッド
    public void fillForget(SQLiteDatabase db, Calendar cal_now, Calendar cal_latest) {
        Cursor cursor0 = db.query("DateTable", new String[]{"id", "year", "month", "date"}, null, null, null, null, null);
        // 最新の行に移動
        cursor0.moveToLast();
        int latestID = cursor0.getInt(0);
        int latestYear = cursor0.getInt(1);
        int latestMonth = cursor0.getInt(2) - 1; // 月は0からなので-1する！！
        int latestDate = cursor0.getInt(3);
        cursor0.close();

        // 現在の日付(と時刻)を取得
        cal_now = Calendar.getInstance();

        // 最新の日付をセット
        cal_latest = Calendar.getInstance();
        cal_latest.set(latestYear, latestMonth, latestDate);

        // 最新の日とアプリを開いた日の差分を計算
        long cal_diff_Millis = cal_now.getTimeInMillis() - cal_latest.getTimeInMillis();
        int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
        final int cal_diff_Days = (int) (cal_diff_Millis / MILLIS_OF_DAY);

        // 1日以上差があるとき実行
        if (cal_diff_Days > 0) {
//            ContentValues emptyCV = new ContentValues();
//            ContentValues emptyCV1 = new ContentValues();
//            ContentValues emptyCV2 = new ContentValues();

            // 当日まで、-1を保存
            for (int i = 0; i < cal_diff_Days; i++) {
                cal_latest.add(Calendar.DAY_OF_MONTH, 1);
                int emptyYear = cal_latest.get(Calendar.YEAR);
                int emptyMonth = cal_latest.get(Calendar.MONTH) + 1;
                int emptyDate = cal_latest.get(Calendar.DATE);

                // 最新の値のidに試行回数と1を足し保存
                insertTime(db, latestID + i + 1, emptyYear, emptyMonth, emptyDate, -1, -1, -1, -1);

//                emptyCV.put("id", latestID + i + 1);
//                emptyCV.put("year", emptyYear);
//                emptyCV.put("month", emptyMonth);
//                emptyCV.put("date", emptyDate);
//
//                emptyCV1.put("id", latestID + i + 1);
//                emptyCV1.put("hour", -1);
//                emptyCV1.put("minute", -1);
//
//                emptyCV2.put("id", latestID + i + 1);
//                emptyCV2.put("hour", -1);
//                emptyCV2.put("minute", -1);
//
//                db.insert("DateTable", null, emptyCV);
//                db.insert("GetUpTable", null, emptyCV1);
//                db.insert("GoToBedTable", null, emptyCV2);
            }
        }
    }

    // 年月日時刻をデータベースに追加するメソッド
    public void insertTime(SQLiteDatabase db, int id, int year, int month, int date, int hour1, int minute1, int hour2, int minute2) {
        ContentValues contentValues = new ContentValues();
        ContentValues contentValues1 = new ContentValues();
        ContentValues contentValues2 = new ContentValues();

        contentValues.put("id", id);
        contentValues.put("year", year);
        contentValues.put("month", month);
        contentValues.put("date", date);

        contentValues1.put("id", id);
        contentValues1.put("hour", hour1);
        contentValues1.put("minute", minute1);

        contentValues2.put("id", id);
        contentValues2.put("hour", hour2);
        contentValues2.put("minute", minute2);

        db.insert("DateTable", null, contentValues);
        db.insert("GetUpTable", null, contentValues1);
        db.insert("GoToBedTable", null, contentValues2);
    }
}
