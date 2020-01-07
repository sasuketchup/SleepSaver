package com.example.sleepsaver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.ContentValues;
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

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Calendar calendar;
    Calendar calendar1;
    Calendar calendar2;


    LinearLayout varDateLay;
    LinearLayout varGULay;
    LinearLayout varGTBLay;

    TimeHandler timeHandler = new TimeHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendar1 = Calendar.getInstance();

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor0 = db.query("DateTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        cursor0.moveToLast();
        int latestYear = cursor0.getInt(1);
        int latestMonth = cursor0.getInt(2);
        int latestDate = cursor0.getInt(3);

        calendar2 = Calendar.getInstance();
        calendar2.set(latestYear, latestMonth, latestDate);

        varDateLay = (LinearLayout) findViewById(R.id.DateLayout);
        varGULay = (LinearLayout) findViewById(R.id.GULayout);
        varGTBLay = (LinearLayout) findViewById(R.id.GTBLayout);

        Cursor cursor = db.query("DateTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");
        long idGU = DatabaseUtils.queryNumEntries(db, "GetUpTable");
        long idGTB = DatabaseUtils.queryNumEntries(db, "GoToBedTable");

        TextView[] textDate = new TextView[(int) idCount];
        TextView[] textGU = new TextView[(int) idCount];
        TextView[] textGTB = new TextView[(int) idCount];

        // 記録を表示
        cursor.moveToFirst();
        cursor1.moveToFirst();
        cursor2.moveToFirst();
        for(int i=0;i<idCount;i++){
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
            textGU[i].setTextSize(24);
            textGTB[i].setTextSize(24);
            cursor.moveToNext();
            cursor1.moveToNext();
            cursor2.moveToNext();
            varDateLay.addView(textDate[i], 0);
            varGULay.addView(textGU[i], 0);
            varGTBLay.addView(textGTB[i], 0);
        }
        cursor.close();
        cursor1.close();
        cursor2.close();

        final ContentValues contentValues = new ContentValues();
        final ContentValues contentValues1 = new ContentValues();

        // 起床時刻ボタンの処理
        findViewById(R.id.GUbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final long idNumber = DatabaseUtils.queryNumEntries(db, "DateTable");

                        calendar = Calendar.getInstance();

                        final int year = calendar.get(Calendar.YEAR);
                        final int month = calendar.get(Calendar.MONTH) + 1;
                        final int date = calendar.get(Calendar.DATE);
                        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        final int minute = calendar.get(Calendar.MINUTE);

                        String[] timeSt = timeHandler.timeString(hour, minute);

                        TextView tvTime = new TextView(getApplicationContext());
                        tvTime.setText(timeSt[0] + ":" + timeSt[1]);
                        tvTime.setTextColor(Color.BLACK);
                        tvTime.setTextSize(30);
                        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(year + "年" + month + "月" + date + "日の起床時刻");
                        builder.setView(tvTime);
                        builder.setPositiveButton(
                                "記録",
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
                                        db.insert("GetUpTable", null, contentValues1);


                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);

                                        dialog.dismiss();
                                    }
                                }
                        );
                        builder.show();
                    }
                }
        );

        // 就寝時刻ボタンの処理
        findViewById(R.id.GTBbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final long idNumber = DatabaseUtils.queryNumEntries(db, "DateTable");

                        calendar = Calendar.getInstance();

                        final int year = calendar.get(Calendar.YEAR);
                        final int month = calendar.get(Calendar.MONTH) + 1;
                        final int date = calendar.get(Calendar.DATE);
                        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        final int minute = calendar.get(Calendar.MINUTE);

                        String[] timeSt = timeHandler.timeString(hour, minute);

                        TextView tvTime = new TextView(getApplicationContext());
                        tvTime.setText(timeSt[0] + ":" + timeSt[1]);
                        tvTime.setTextColor(Color.BLACK);
                        tvTime.setTextSize(30);
                        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(year + "年" + month + "月" + date + "日の就寝時刻");
                        builder.setView(tvTime);
                        builder.setPositiveButton(
                                "記録",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        contentValues.put("id", idNumber); // ここも！！
                                        contentValues.put("year", year);
                                        contentValues.put("month", month);
                                        contentValues.put("date", date);

                                        contentValues1.put("id", idNumber);
                                        contentValues1.put("hour", hour);
                                        contentValues1.put("minute", minute);

                                        db.insert("DateTable", null, contentValues);
                                        db.insert("GoToBedTable", null, contentValues1);


                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);

                                        dialog.dismiss();
                                    }
                                }
                        );
                        builder.show();
                    }
                }
        );
    }
}
