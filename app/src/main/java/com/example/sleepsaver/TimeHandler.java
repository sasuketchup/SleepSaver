package com.example.sleepsaver;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;

public class TimeHandler extends Application {

    // 範囲を格納する変数
    static int resultsNum;
    // 指定日～今日の指定日
    static int spec_year;
    static int spec_month;
    static int spec_date;
    // 指定日1～指定日2の指定日1
    static int spec_year1;
    static int spec_month1;
    static int spec_date1;
    // 指定日1～指定日2の指定日2
    static int spec_year2;
    static int spec_month2;
    static int spec_date2;

    // 範囲のgetter
    public int getResultsNum() {
        return resultsNum;
    }
    // 範囲のsetter
    public void setResultsNum(int results) {
        resultsNum = results;
    }

    // getter(指定日)
    public int getSpec_year() {
        return spec_year;
    }

    public int getSpec_month() {
        return spec_month;
    }

    public int getSpec_date() {
        return spec_date;
    }

    public int getSpec_year1() {
        return spec_year1;
    }

    public int getSpec_month1() {
        return spec_month1;
    }

    public int getSpec_date1() {
        return spec_date1;
    }

    public int getSpec_year2() {
        return spec_year2;
    }

    public int getSpec_month2() {
        return spec_month2;
    }

    public int getSpec_date2() {
        return spec_date2;
    }

    // setter(指定日)
    public void setSpec_year(int year) {
        spec_year = year;
    }

    public void setSpec_month(int month) {
        spec_month = month;
    }

    public void setSpec_date(int date) {
        spec_date = date;
    }

    public void setSpec_year1(int year) {
        spec_year1 = year;
    }

    public void setSpec_month1(int month) {
        spec_month1 = month;
    }

    public void setSpec_date1(int date) {
        spec_date1 = date;
    }

    public void setSpec_year2(int year) {
        spec_year2 = year;
    }

    public void setSpec_month2(int month) {
        spec_month2 = month;
    }

    public void setSpec_date2(int date) {
        spec_date2 = date;
    }

    // ダイアログを表示するメソッド
    public void showDialog(final Context context, final int positiveNum, String title, String message, String... button) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        // ポジティブボタン
        if (button.length > 0) {
            builder.setPositiveButton(
                    button[0],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pushPositiveButton(context, positiveNum);
                            dialog.dismiss();
                        }
                    }
            );
        }
        // ネガティブボタン
        if (button.length > 1) {
            builder.setNegativeButton(
                    button[1],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );
        }
        // ニュートラルボタン
        if (button.length > 2) {
            builder.setNeutralButton(
                    button[2],
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );
        }
        builder.show();
    }

    // ダイアログのポジティブボタンの処理
    public void pushPositiveButton(Context context, int number) {

        MyOpenHelper helper = new MyOpenHelper(context);
        final SQLiteDatabase db = helper.getWritableDatabase();

        switch (number) {
            case 0: // ダイアログを閉じるのみ
                break;
            case 1: // 指定された期間のデータをCSV出力

                // データの行数を取得
                long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");

                // データと件数の差分
                int diff_id = 0;

                // 指定日1～2の時、表示範囲を決めるための変数
                int diff_now_spec1 = (int) (idCount - 1);
                int diff_now_spec2 = 0;

                // 指定日1～指定日2(resultsが-2)の場合
                if (resultsNum == -2) {
                    // 今日と指定日1、2の差日数を計算
                    diff_now_spec1 = spec12_today(db, context, true)[0];
                    diff_now_spec2 = spec12_today(db, context, true)[1];
                }
                // 指定日～今日(resultsが-1)の場合に指定日と今日の差分を計算
                if (resultsNum == -1) {
                    // 指定日と今日の差を計算し、件数に代入
                    resultsNum = spec_today(db, context, true);
                }
                // 件数を計算
                if (resultsNum > 0) {
                    if (idCount > resultsNum) {
                        diff_id = (int) (idCount - resultsNum);
                        idCount = resultsNum;
                    }
                }

                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!exportDir.exists()) { // ディレクトリがなければ作る
                        exportDir.mkdirs();
                    }
                    File file;
                    PrintWriter printWriter = null;

                    Cursor cursor = db.query("DateTable", new String[]{"id", "year", "month", "date"}, null, null, null, null, null);
                    Cursor cursor1 = db.query("GetUpTable", new String[]{"id", "hour", "minute"}, null, null, null, null, null);
                    Cursor cursor2 = db.query("GoToBedTable", new String[]{"id", "hour", "minute"}, null, null, null, null, null);
                    // カーソルを最新の記録へ
                    cursor.moveToLast();
                    cursor1.moveToLast();
                    cursor2.moveToLast();

                    try {
                        file = new File(exportDir, "data_SleepSaver.csv");
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                        printWriter = new PrintWriter(osw);

                        // データを書き込む
                        for (int i=0; i<idCount; i++) {
                            int id = cursor.getInt(0);

                            int year = cursor.getInt(1);
                            int month = cursor.getInt(2);
                            int date = cursor.getInt(3);

                            int hourGU = cursor1.getInt(1);
                            int minuteGU = cursor1.getInt(2);

                            int hourGTB = cursor2.getInt(1);
                            int minuteGTB = cursor2.getInt(2);

                            if (diff_now_spec2 <= i && i <= diff_now_spec1) {
                                printWriter.print(id);
                                printWriter.print(",");
                                printWriter.print(year);
                                printWriter.print(",");
                                printWriter.print(month);
                                printWriter.print(",");
                                printWriter.print(date);
                                printWriter.print(",");
                                printWriter.print(hourGU);
                                printWriter.print(",");
                                printWriter.print(minuteGU);
                                printWriter.print(",");
                                printWriter.print(hourGTB);
                                printWriter.print(",");
                                printWriter.print(minuteGTB);
                                printWriter.println();
                            }
                            cursor.moveToPrevious();
                            cursor1.moveToPrevious();
                            cursor2.moveToPrevious();
                        }
                        cursor.close();
                        cursor1.close();
                        cursor2.close();

                    } catch (FileNotFoundException exc) {
                        Toast.makeText(context, "アクセス権限がありません", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "CSV出力に失敗しました", Toast.LENGTH_LONG).show();
                    } finally {
                        if (printWriter != null) {
                            printWriter.close();
                        }
                    }
                }
                break;
        }
    }

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

    // 分換算された時間の値を時と分に分け、表示する形式に整理するメソッド
    public String minutes_to_timeString(int minutesValue) {
        int minute = minutesValue % 60;
        int hour = (minutesValue - minute) / 60;
        return timeString(hour, minute);
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

    // 自作のタイムピッカーを表示するメソッド
    public void showOriginalTimePicker(String title) {

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

    // 指定日と今日の差分を計算するメソッド
    public int spec_today(SQLiteDatabase db, Context context, boolean restart) {
        int spec_year;
        int spec_month;
        int spec_date;
        // アクティビティ初回起動時
        if (!restart) {
            // 指定日をDBから取得
            Cursor cursor = db.query("RangeTable", new String[]{"id", "year", "month", "date"}, null, null, null, null, null);
            cursor.moveToFirst();
            spec_year = cursor.getInt(1);
            spec_month = cursor.getInt(2);
            spec_date = cursor.getInt(3);
            cursor.close();
        } else { // アクティビティ更新時
            spec_year = getSpec_year();
            spec_month = getSpec_month();
            spec_date = getSpec_date();
        }
        // 取得した日付をセット
        Calendar cal_spec = Calendar.getInstance();
        cal_spec.set(spec_year, spec_month, spec_date);
        // 今日の日付を取得
        Calendar cal_now = Calendar.getInstance();
        cal_now.add(Calendar.DAY_OF_MONTH, compareTime(context));
        // 指定日と今日の差を計算し、返す
        return cal_diff_Days(cal_now, cal_spec) + 1;
    }

    // 指定日1と今日、指定日2と今日の差分を計算するメソッド
    public int[] spec12_today(SQLiteDatabase db, Context context, boolean restart) {
        int spec_year1;
        int spec_month1;
        int spec_date1;
        int spec_year2;
        int spec_month2;
        int spec_date2;
        // アクティビティ初回起動時
        if (!restart) {
            // 指定日1、2をDBから取得
            Cursor cursor = db.query("RangeTable", new String[]{"id", "year", "month", "date"}, null, null, null, null, null);
            cursor.moveToPosition(1);
            spec_year1 = cursor.getInt(1);
            spec_month1 = cursor.getInt(2);
            spec_date1 = cursor.getInt(3);
            cursor.moveToNext();
            spec_year2 = cursor.getInt(1);
            spec_month2 = cursor.getInt(2);
            spec_date2 = cursor.getInt(3);
            cursor.close();
        } else {
            spec_year1 = getSpec_year1();
            spec_month1 = getSpec_month1();
            spec_date1 = getSpec_date1();
            spec_year2 = getSpec_year2();
            spec_month2 = getSpec_month2();
            spec_date2 = getSpec_date2();
        }
        // 取得した日付をセット
        Calendar cal_spec1 = Calendar.getInstance();
        cal_spec1.set(spec_year1, spec_month1, spec_date1);
        Calendar cal_spec2 = Calendar.getInstance();
        cal_spec2.set(spec_year2, spec_month2, spec_date2);
        // 今日の日付を取得
        Calendar cal_now = Calendar.getInstance();
        cal_now.add(Calendar.DAY_OF_MONTH, compareTime(context));
        // 今日と指定日1、2の差日数を計算し配列として返す
        return new int[]{cal_diff_Days(cal_now, cal_spec1), cal_diff_Days(cal_now, cal_spec2)};
    }
}
