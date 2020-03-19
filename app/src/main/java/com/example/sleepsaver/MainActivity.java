package com.example.sleepsaver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.database.DatabaseUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // 年月日時刻を扱う変数
    int year = 0;
    int month = 0;
    int date = 0;
    int hour = 0;
    int minute = 0;

    // ピッカーの時刻を変更したときに変数に代入するようにするために、TimePickerDialogを継承したクラス
    public class CustomTimePickerDialog extends TimePickerDialog {

        public CustomTimePickerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
            super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker view, int s_hour, int s_minute) {
            hour = s_hour;
            minute = s_minute;
        }
    }

    Calendar calendar;
    Calendar cal_now;
    Calendar cal_latest;

    LinearLayout varRecordLay;

    LinearLayout varDateLay;
    LinearLayout varGULay;
    LinearLayout varGTBLay;

    TimeHandler timeHandler = new TimeHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor0 = db.query("DateTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        cursor0.moveToLast();
        int latestID = cursor0.getInt(0);
        int latestYear = cursor0.getInt(1);
        int latestMonth = cursor0.getInt(2) - 1; // 月は0からなので-1する！！
        int latestDate = cursor0.getInt(3);
        cursor0.close();

        cal_now = Calendar.getInstance();

        cal_latest = Calendar.getInstance();
        cal_latest.set(latestYear, latestMonth, latestDate);

        long cal_diff_Millis = cal_now.getTimeInMillis() - cal_latest.getTimeInMillis();
        int MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
        final int cal_diff_Days = (int)(cal_diff_Millis / MILLIS_OF_DAY);

        ContentValues emptyCV = new ContentValues();
        ContentValues emptyCV1 = new ContentValues();
        ContentValues emptyCV2 = new ContentValues();

        for(int i=0;i<(cal_diff_Days-1);i++){
            cal_latest.add(Calendar.DAY_OF_MONTH, 1);
            int emptyYear = cal_latest.get(Calendar.YEAR);
            int emptyMonth = cal_latest.get(Calendar.MONTH) + 1;
            int emptyDate = cal_latest.get(Calendar.DATE);

            emptyCV.put("id", latestID);
            emptyCV.put("year", emptyYear);
            emptyCV.put("month", emptyMonth);
            emptyCV.put("date", emptyDate);

            emptyCV1.put("id", latestID);
            emptyCV1.put("hour", -1);
            emptyCV1.put("minute", -1);

            emptyCV2.put("id", latestID);
            emptyCV2.put("hour", -1);
            emptyCV2.put("minute", -1);

            db.insert("DateTable", null, emptyCV);
            db.insert("GetUpTable", null, emptyCV1);
            db.insert("GoToBedTable",null, emptyCV2);
        }

        varRecordLay = (LinearLayout) findViewById(R.id.RecordLayout);

        varDateLay = (LinearLayout) findViewById(R.id.DateLayout);
        varGULay = (LinearLayout) findViewById(R.id.GULayout);
        varGTBLay = (LinearLayout) findViewById(R.id.GTBLayout);

        Cursor cursor = db.query("DateTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");
        long idGU = DatabaseUtils.queryNumEntries(db, "GetUpTable");
        long idGTB = DatabaseUtils.queryNumEntries(db, "GoToBedTable");

//        LinearLayout[] timeLayout = new LinearLayout[(int) idCount];

        TextView[] textDate = new TextView[(int) idCount];
        TextView[] textGU = new TextView[(int) idCount];
        TextView[] textGTB = new TextView[(int) idCount];

        // 記録を表示
        cursor.moveToFirst();
        cursor1.moveToFirst();
        cursor2.moveToFirst();
        for(int i=0;i<idCount;i++){
//            timeLayout[i] = new LinearLayout(this);
            textDate[i] = new TextView(this);
            textGU[i] = new TextView(this);
            textGTB[i] = new TextView(this);
            int year = cursor.getInt(1);
            int month = cursor.getInt(2);
            int date = cursor.getInt(3);

            String[] timeGUSt = {"--", "--"};
            String[] timeGTBSt = {"--", "--"};

            if(i < idGU){
                int hourGU = cursor1.getInt(1);
                int minuteGU = cursor1.getInt(2);

                timeGUSt = timeHandler.timeString(hourGU, minuteGU);
            }

            if(i < idGTB) {
                int hourGTB = cursor2.getInt(1);
                int minuteGTB = cursor2.getInt(2);

                timeGTBSt = timeHandler.timeString(hourGTB, minuteGTB);
            }

            textDate[i].setText(year + "年" + month + "月" + date + "日");
            textGU[i].setText(timeGUSt[0] + ":" + timeGUSt[1]);
            textGTB[i].setText(timeGTBSt[0] + ":" + timeGTBSt[1]);
//            textDate[i].setWidth(convertDp2Px(80));
            textDate[i].setHeight(100);
            textGU[i].setHeight(100);
            textGTB[i].setHeight(100);
//            textGU[i].setWidth(convertDp2Px(100));
//            textGTB[i].setWidth(convertDp2Px(100));
//            textDate[i].setGravity(Gravity.TOP);
            textGU[i].setGravity(Gravity.RIGHT);
            textGTB[i].setGravity(Gravity.RIGHT);
            textGU[i].setTextSize(30);
            textGTB[i].setTextSize(30);
            cursor.moveToNext();
            cursor1.moveToNext();
            cursor2.moveToNext();

//            timeLayout[i].setOrientation(LinearLayout.HORIZONTAL);
//            timeLayout[i].addView(textDate[i]);
//            timeLayout[i].addView(textGU[i]);
//            timeLayout[i].addView(textGTB[i]);
//            varRecordLay.addView(timeLayout[i], 0);
            varDateLay.addView(textDate[i], 0);
            varGULay.addView(textGU[i], 0);
            varGTBLay.addView(textGTB[i], 0);
        }
        cursor.close();
        cursor1.close();
        cursor2.close();

        // 起床時刻ボタンの処理
        findViewById(R.id.GUbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recordTime(false, db);
                    }
                }
        );

        // 就寝時刻ボタンの処理
        findViewById(R.id.GTBbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recordTime(true, db);
                    }
                }
        );
    }

    // 起床or就寝時刻ボタンを押したときに呼ばれるメソッド
    public void recordTime(final boolean sleep, final SQLiteDatabase db) {
        final long idNumber = DatabaseUtils.queryNumEntries(db, "DateTable");

        final ContentValues contentValues = new ContentValues();
        final ContentValues contentValues1 = new ContentValues();

        calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        // タイムピッカーを表示
        final CustomTimePickerDialog timePickerDialog;
        final CustomTimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int s_hour, int s_minute) {

            }
        };
        timePickerDialog = new CustomTimePickerDialog(MainActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, hour, minute, true);

        timePickerDialog.setTitle(year + "年" + month + "月" + date + "日の起床時刻");
        timePickerDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "記録する",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contentValues.put("id", idNumber); // このままだと日付が同じときでもidをインクリメントしてしまう！
                        contentValues.put("year", year);
                        contentValues.put("month", month);
                        contentValues.put("date", date);

                        contentValues1.put("id", idNumber);
                        contentValues1.put("hour", hour);
                        contentValues1.put("minute", minute);

                        db.insert("DateTable", null, contentValues);
                        if(sleep == false) {
                            db.insert("GetUpTable", null, contentValues1);
                        }else{
                            db.insert("GoToBedTable", null, contentValues1);
                        }

                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);

                        dialog.dismiss();
                    }
                }
        );
        timePickerDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "キャンセル",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Negative Button がクリックされた時の動作
                        dialog.dismiss();
                    }
                }
        );
        timePickerDialog.show();
    }

    // dpをpxに変換するメソッド
    public int convertDp2Px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        int px = (int) (dp * scale);
        return px;
    }
}
