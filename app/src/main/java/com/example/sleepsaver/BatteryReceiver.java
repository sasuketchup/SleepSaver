package com.example.sleepsaver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryReceiver extends BroadcastReceiver {

    // 充電の接続状態が変わったときに実行
    @Override
    public void onReceive(Context context, Intent intent) {

        Notification.Builder builder = new Notification.Builder(context);
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        builder.setSmallIcon(android.R.drawable.sym_def_app_icon);
        builder.setContentTitle("SleepSaver");

        manager.notify(1, builder.build());
    }
}
