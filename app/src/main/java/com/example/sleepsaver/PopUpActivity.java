package com.example.sleepsaver;

import android.content.Intent;
import android.content.IntentFilter;
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

        varTextGUorGTB = findViewById(R.id.textGUorGTB);
        varTextTime = findViewById(R.id.textTime);

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
