package com.example.sleepsaver;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

public class TimeHandler {

    // 時分を表示する形式に整理するメソッド
    protected String timeString(int hour, int minute) {
        String hourSt = "--";
        String minuteSt = "--";

        if(hour<10 && hour > -1){
            hourSt = "0" + hour;
        }else if(hour != -1){
            hourSt = String.valueOf(hour);
        }
        if(minute<10 && minute > -1){
            minuteSt = "0" + minute;
        }else if(minute != -1){
            minuteSt = String.valueOf(minute);
        }

        String timeSt = hourSt + ":" + minuteSt;

        return timeSt;
    }

    // 日付を表示する形式に整理するメソッド
    public String dateString(int year, int month, int date) {
        return year + "/" + month + "/" + date;
    }

    // 2つの日付の差分の日数を計算するメソッド
    public int cal_diff_Days(Calendar calendar1, Calendar calendar2) {
        long cal_diff_Millis = calendar1.getTimeInMillis() - calendar2.getTimeInMillis();
        int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
        return (int) (cal_diff_Millis / MILLIS_OF_DAY);
    }

    // 4桁の数値を時と分に分けるメソッド
    public int[] number_to_time(int number) {
        int[] time = {0,0};
        time[1] = number % 100;
        time[0] = (number - time[1]) / 100;
        return time;
    }
    // 時と分を4桁の数値に合成するメソッド
    public int time_to_number(int hour, int minute) {
        int number = 0;
        number = (hour * 100) + minute;
        return number;
    }

    // 目標起床時刻と目標就寝時刻の差を計算し、Calenderを返すメソッド
    public Calendar diff_gu_gtb(int gu_target, int gtb_target) {
        int gu_hour = number_to_time(gu_target)[0];
        int gu_minute = number_to_time(gu_target)[1];
        int gtb_hour = number_to_time(gtb_target)[0];
        int gtb_minute = number_to_time(gtb_target)[1];
        Calendar cal_target = Calendar.getInstance();
        cal_target.set(0, 0, 0, gu_hour, gu_minute);
        cal_target.add(Calendar.HOUR, 0 - gtb_hour);
        cal_target.add(Calendar.MINUTE, 0 - gtb_minute);
        return cal_target;
    }

    // 1日のサイクルと現在時刻を比較するメソッド
    public int compareTime(Context context) {
        // 設定から1日のサイクルを取得
        SharedPreferences sp = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        // 起床→就寝
        int stay_up_line = sp.getInt("stay_up_line", 1200);
        int hour_line = number_to_time(stay_up_line)[0];
        int minute_line = number_to_time(stay_up_line)[1];
        // Calenderにセット
        Calendar cal_stay_up = Calendar.getInstance();
        cal_stay_up.set(Calendar.HOUR_OF_DAY, hour_line);
        cal_stay_up.set(Calendar.MINUTE, minute_line);
        // 就寝→起床
        int sleeping_line = sp.getInt("sleeping_line", 0);
        int hour_line2 = number_to_time(sleeping_line)[0];
        int minute_line2 = number_to_time(sleeping_line)[1];
        // Calenderにセット
        Calendar cal_sleeping = Calendar.getInstance();
        cal_sleeping.set(Calendar.HOUR_OF_DAY, hour_line2);
        cal_sleeping.set(Calendar.MINUTE, minute_line2);

        // 起床→就寝と就寝→起床の大小を比較
        int comparison = cal_stay_up.compareTo(cal_sleeping);

        // 現在の日付(と時刻)を取得
        Calendar cal_now = Calendar.getInstance();

        // 現在の時刻と就寝→起床の時刻を比較し、該当の時刻の場合、日付に加算or減算
        int comp_now_sl = cal_now.compareTo(cal_sleeping);
        int add_date = 0;
        if (comparison == -1 && comp_now_sl == 1) {
            add_date = 1;
        } else if (comparison == 1 && comp_now_sl == -1){
            add_date = -1;
        }
        return add_date;
    }

    // 記録し忘れの対応処理メソッド
    public void fillForget(SQLiteDatabase db, Context context) {
        Cursor cursor0 = db.query("DateTable", new String[]{"id", "year", "month", "date"}, null, null, null, null, null);
        // 最新の行に移動
        cursor0.moveToLast();
        int latestID = cursor0.getInt(0);
        int latestYear = cursor0.getInt(1);
        int latestMonth = cursor0.getInt(2) - 1; // 月は0からなので-1する！！
        int latestDate = cursor0.getInt(3);
        cursor0.close();

        // 現在の日付(と時刻)を取得
        Calendar cal_now = Calendar.getInstance();

        // 1日のサイクルと現在時刻を比較し結果を加算
        cal_now.add(Calendar.DAY_OF_MONTH, compareTime(context));

        // 記録されているうちの最新の日付をセット
        Calendar cal_latest = Calendar.getInstance();
        cal_latest.set(latestYear, latestMonth, latestDate);

        // 最新の日とアプリを開いた日の差分を計算
        int cal_diff_now_latest = cal_diff_Days(cal_now, cal_latest);

        // 1日以上差があるとき実行
        if (cal_diff_now_latest > 0) {
            // 当日まで、-1を保存
            for (int i = 0; i < cal_diff_now_latest; i++) {
                cal_latest.add(Calendar.DAY_OF_MONTH, 1);
                int emptyYear = cal_latest.get(Calendar.YEAR);
                int emptyMonth = cal_latest.get(Calendar.MONTH) + 1;
                int emptyDate = cal_latest.get(Calendar.DATE);

                // 最新の値のidに試行回数と1を足し保存
                insertTime(db, latestID + i + 1, emptyYear, emptyMonth, emptyDate, -1, -1, -1, -1);
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

    // データベースの年月日時刻を上書きするメソッド
    public void updateTime(boolean sleep, SQLiteDatabase db, int id, int year, int month, int date, int hour, int minute) {
        ContentValues contentValues = new ContentValues();
        ContentValues contentValues1 = new ContentValues();

        contentValues.put("year", year);
        contentValues.put("month", month);
        contentValues.put("date", date);

        contentValues1.put("hour", hour);
        contentValues1.put("minute", minute);

        db.update("DateTable", contentValues, "id=" + id, null);

        if(sleep == false) {
            db.update("GetUpTable", contentValues1, "id=" + id, null);
        }else{
            db.update("GoToBedTable", contentValues1, "id=" + id, null);
        }
    }
}
