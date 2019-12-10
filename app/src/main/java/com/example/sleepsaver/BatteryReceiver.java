package com.example.sleepsaver;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryReceiver extends BroadcastReceiver {

    // 充電の接続状態が変わったときに実行
    @Override
    public void onReceive(Context context, Intent intent) {

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        intent = context.registerReceiver(null, intentFilter);

        int battery_charge = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String charge_state = "";

        if(battery_charge == 0){
            charge_state = "起床時刻";
        }else if(battery_charge == 1 || battery_charge == 2 || battery_charge == 4){
            charge_state = "就寝時刻";
        }

        Notification.Builder builder = new Notification.Builder(context);
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        builder.setSmallIcon(android.R.drawable.sym_def_app_icon);
        builder.setContentTitle("SleepSaver");
        builder.setContentText(charge_state);

        manager.notify(1, builder.build());
    }
}
