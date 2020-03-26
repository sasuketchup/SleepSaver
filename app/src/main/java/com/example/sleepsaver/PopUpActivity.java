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
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class PopUpActivity extends AppCompatActivity {

    TextView varTextState;
    TextView varTextGUorGTB;
    TimePicker varTextTime;

    Calendar calendar;
    Calendar cal_now;
    Calendar cal_latest;

    TimeHandler timeHandler = new TimeHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        // ロック画面上に表示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        final ContentValues contentValues = new ContentValues();
        final ContentValues contentValues1 = new ContentValues();

        varTextState = findViewById(R.id.textState);
        varTextGUorGTB = findViewById(R.id.textGUorGTB);
        varTextTime = findViewById(R.id.textTime);

        calendar = Calendar.getInstance();

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int date = calendar.get(Calendar.DATE);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        // 時刻の表示形式を整理
        String[] timeSt = timeHandler.timeString(hour, minute);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery_intent = this.registerReceiver(null, intentFilter);

        final int battery_charge = battery_intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String charge_state = "";

        if(battery_charge == 0){
            varTextState.setText("電源から切断しました。");
            charge_state = "起床時刻";
        }else if(battery_charge == 1 || battery_charge == 2 || battery_charge == 4){
            varTextState.setText("電源に接続しました。");
            charge_state = "就寝時刻";
        }

        varTextGUorGTB.setText(year + "年" + month + "月" + date + "日の" + charge_state);
        varTextTime.setIs24HourView(true);
        varTextTime.setCurrentHour(hour);
        varTextTime.setCurrentMinute(minute);
//        varTextTime.setText(timeSt[0] + ":" + timeSt[1]);

        // 現在時刻にセットボタン
        findViewById(R.id.btnCurrent).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        calendar = Calendar.getInstance();
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int currentMinute = calendar.get(Calendar.MINUTE);

                        varTextTime.setCurrentHour(currentHour);
                        varTextTime.setCurrentMinute(currentMinute);
                    }
                }
        );

        // 記録ボタン
        findViewById(R.id.btnRecord).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 記録し忘れがある場合、差分を埋める
                        timeHandler.fillForget(db, cal_now, cal_latest);

                        long idNumber = DatabaseUtils.queryNumEntries(db, "DateTable"); // 日付によるインクリメント問題！

//                        contentValues.put("id", idNumber);
                        contentValues.put("year", year);
                        contentValues.put("month", month);
                        contentValues.put("date", date);

                        contentValues1.put("hour", varTextTime.getCurrentHour());
                        contentValues1.put("minute", varTextTime.getCurrentMinute());

                        db.update("DateTable", contentValues, "id=" + (idNumber - 1), null);

                        if(battery_charge == 0) {
                            db.update("GetUpTable", contentValues1, "id=" + (idNumber - 1), null);
                        }else if(battery_charge == 1 || battery_charge == 2 || battery_charge == 4){
                            db.update("GoToBedTable", contentValues1, "id=" + (idNumber - 1), null);
                        }

                        findViewById(R.id.btnRecord).setEnabled(false);
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
