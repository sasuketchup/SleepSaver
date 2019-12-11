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
    LinearLayout varGULay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery_intent = this.registerReceiver(null, intentFilter);

        final int battery_charge = battery_intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        varGULay = (LinearLayout) findViewById(R.id.GULayout);

        Cursor cursor = db.query("GetUpTable", new String[] {"id", "year", "month", "date", "hour", "minute"}, null, null, null, null, null);

        long idCount = DatabaseUtils.queryNumEntries(db, "GetUpTable");

        TextView[] textGU = new TextView[(int) idCount];

        cursor.moveToFirst();
        for(int i=0;i<idCount;i++){
            textGU[i] = new TextView(this);
            int yearGU = cursor.getInt(1);
            int monthGU = cursor.getInt(2);
            int dateGU = cursor.getInt(3);
            int hourGU = cursor.getInt(4);
            int minuteGU = cursor.getInt(5);
            textGU[i].setText(yearGU + "年" + monthGU + "月" + dateGU + "日" + hourGU + "時" + minuteGU + "分");
            cursor.moveToNext();
            varGULay.addView(textGU[i], 0);
        }
        cursor.close();

        final ContentValues contentValues = new ContentValues();

        // 起床時刻ボタンの処理
        findViewById(R.id.GUbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final long idNumber = DatabaseUtils.queryNumEntries(db, "GetUpTable");

                        calendar = Calendar.getInstance();

                        final int year = calendar.get(Calendar.YEAR);
                        final int month = calendar.get(Calendar.MONTH) + 1;
                        final int date = calendar.get(Calendar.DATE);
                        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        final int minute = calendar.get(Calendar.MINUTE);

                        String hourSt;
                        String minuteSt;

                        if(hour<10){
                            hourSt = "0" + hour;
                        }else {
                            hourSt = String.valueOf(hour);
                        }
                        if(minute<10){
                            minuteSt = "0" + minute;
                        }else {
                            minuteSt = String.valueOf(minute);
                        }

                        TextView tvTime = new TextView(getApplicationContext());
                        tvTime.setText(hourSt + ":" + minuteSt);
                        tvTime.setTextColor(Color.BLACK);
                        tvTime.setTextSize(30);
                        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(year + "年" + month + "月" + date + "日の起床時刻" + battery_charge);
                        builder.setView(tvTime);
                        builder.setPositiveButton(
                                "記録",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        contentValues.put("id", idNumber);
                                        contentValues.put("year", year);
                                        contentValues.put("month", month);
                                        contentValues.put("date", date);
                                        contentValues.put("hour", hour);
                                        contentValues.put("minute", minute);

                                        db.insert("GetUpTable", null, contentValues);


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

        findViewById(R.id.GTBbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(MainActivity.this, PopUpActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}
