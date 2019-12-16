package com.example.sleepsaver;

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class PopUpActivity extends AppCompatActivity {

    TextView varTextGUorGTB;
    TextView varTextTime;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        // ロック画面上に表示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        final ContentValues contentValues = new ContentValues();

        varTextGUorGTB = findViewById(R.id.textGUorGTB);
        varTextTime = findViewById(R.id.textTime);

        calendar = Calendar.getInstance();

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int date = calendar.get(Calendar.DATE);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        // 時刻の表示形式を整理
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

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery_intent = this.registerReceiver(null, intentFilter);

        int battery_charge = battery_intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String charge_state = "";

        if(battery_charge == 0){
            charge_state = "起床時刻";
        }else if(battery_charge == 1 || battery_charge == 2 || battery_charge == 4){
            charge_state = "就寝時刻";
        }

        varTextGUorGTB.setText(year + "年" + month + "月" + date + "日の" + charge_state);
        varTextTime.setText(hourSt + ":" + minuteSt);

        // 記録ボタン
        findViewById(R.id.btnRecord).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        long idNumber = DatabaseUtils.queryNumEntries(db, "GetUpTable"); // あとで条件分岐すること！

                        contentValues.put("id", idNumber);
                        contentValues.put("year", year);
                        contentValues.put("month", month);
                        contentValues.put("date", date);
                        contentValues.put("hour", hour);
                        contentValues.put("minute", minute);

                        db.insert("GetUpTable", null, contentValues); // ここも！

                        finish();
                    }
                }
        );

        // アプリを開くボタン
        findViewById(R.id.btnOpen).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(PopUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );

        // 閉じるボタン
        findViewById(R.id.btnClose).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                }
        );

    }
}
