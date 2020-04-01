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

import org.w3c.dom.Text;

import java.time.LocalDateTime;
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
    LinearLayout varSTLay;

    TimeHandler timeHandler = new TimeHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        // データの行数を取得
        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");

        // データが1行以上あるとき実行
        if(idCount > 0) {
            // 記録し忘れがある場合、差分を埋める
            timeHandler.fillForget(db, cal_now, cal_latest);
        }else{
            // データが空のとき実行
            calendar = Calendar.getInstance();

            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            date = calendar.get(Calendar.DATE);

            timeHandler.insertTime(db, 0, year, month, date, -1, -1, -1, -1);
        }

        varRecordLay = (LinearLayout) findViewById(R.id.RecordLayout);

        varDateLay = (LinearLayout) findViewById(R.id.DateLayout);
        varGULay = (LinearLayout) findViewById(R.id.GULayout);
        varGTBLay = (LinearLayout) findViewById(R.id.GTBLayout);
        varSTLay = (LinearLayout) findViewById(R.id.STLayout);

        Cursor cursor = db.query("DateTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

//        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");
        long idGU = DatabaseUtils.queryNumEntries(db, "GetUpTable");
        long idGTB = DatabaseUtils.queryNumEntries(db, "GoToBedTable");

//        LinearLayout[] timeLayout = new LinearLayout[(int) idCount];

        TextView[] textDate = new TextView[(int) idCount];
        TextView[] textGU = new TextView[(int) idCount];
        TextView[] textGTB = new TextView[(int) idCount];
        TextView[] textST = new TextView[(int) idCount - 1];

        // 記録を表示
        cursor.moveToLast();
        cursor1.moveToLast();
        cursor2.moveToLast();
        for(int i=0;i<idCount;i++){
//            timeLayout[i] = new LinearLayout(this);
            textDate[i] = new TextView(this);
            textGU[i] = new TextView(this);
            textGTB[i] = new TextView(this);

            int year = cursor.getInt(1);
            int month = cursor.getInt(2);
            int date = cursor.getInt(3);

            String timeGUSt = "--:--";
            String timeGTBSt = "--:--";
            String timeSTSt = "--:--";

            int hourGU = -1;
            int minuteGU = -1;
            if(i < idGU){
                hourGU = cursor1.getInt(1);
                minuteGU = cursor1.getInt(2);

                timeGUSt = timeHandler.timeString(hourGU, minuteGU);
            }

            if(i < idGTB) {
                int hourGTB = cursor2.getInt(1);
                int minuteGTB = cursor2.getInt(2);

                timeGTBSt = timeHandler.timeString(hourGTB, minuteGTB);
            }

            textDate[i].setText(year + "年" + month + "月" + date + "日");
            textGU[i].setText(timeGUSt);
            textGTB[i].setText(timeGTBSt);
//            textDate[i].setWidth(convertDp2Px(80));

            textDate[i].setHeight(150);
            textGU[i].setHeight(150);
            textGTB[i].setHeight(150);

//            textGU[i].setWidth(convertDp2Px(100));
//            textGTB[i].setWidth(convertDp2Px(100));
//            textDate[i].setGravity(Gravity.TOP);
            textGU[i].setGravity(Gravity.RIGHT);
            textGTB[i].setGravity(Gravity.RIGHT);
            textGU[i].setTextSize(30);
            textGTB[i].setTextSize(30);
            cursor.moveToPrevious();
            cursor1.moveToPrevious();
            cursor2.moveToPrevious();

            if (i < idCount - 1) {
                textST[i] = new TextView(this);

                // 睡眠時間計算のため、次の就寝時刻を取得
                int hourGTBnext = cursor2.getInt(1);
                int minuteGTBnext = cursor2.getInt(2);

                // 起床・就寝の値が揃っているとき
                if (hourGTBnext != -1 && hourGU != -1) {
                    int dateST = 0;
                    // 日付を跨いだ場合(これは暫定で正午の12時。深夜ではない！)
                    if (hourGTBnext > 12) {
                        dateST = 1;
                    }

                    Calendar calST = Calendar.getInstance();
                    calST.set(0, 0, dateST, hourGU, minuteGU);
                    calST.add(Calendar.HOUR, 0 - hourGTBnext);
                    calST.add(Calendar.MINUTE, 0 - minuteGTBnext);

                    int hourST = calST.get(Calendar.HOUR_OF_DAY);
                    int minuteST = calST.get(Calendar.MINUTE);

                    timeSTSt = timeHandler.timeString(hourST, minuteST);
                }
                textST[i].setText(timeSTSt);
                textST[i].setTextSize(30);
                textST[i].setHeight(150);
                textST[i].setGravity(Gravity.RIGHT);

                varSTLay.addView(textST[i]);
            }

//            timeLayout[i].setOrientation(LinearLayout.HORIZONTAL);
//            timeLayout[i].addView(textDate[i]);
//            timeLayout[i].addView(textGU[i]);
//            timeLayout[i].addView(textGTB[i]);
//            varRecordLay.addView(timeLayout[i], 0);
            varDateLay.addView(textDate[i]);
            varGULay.addView(textGU[i]);
            varGTBLay.addView(textGTB[i]);
        }
        cursor.close();
        cursor1.close();
        cursor2.close();

        // 睡眠時間の表示位置調整のための空のテキストビュー
        TextView emptyST = new TextView(this);
        emptyST.setHeight(75);
        varSTLay.addView(emptyST, 0);

        // 設定ボタンの処理
        findViewById(R.id.Settings).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, PrefActivity.class);
                        startActivity(intent);
                    }
                }
        );

        // 起床時刻ボタンの処理
        findViewById(R.id.GUbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recordTime(false, db, -1);
                    }
                }
        );

        // 就寝時刻ボタンの処理
        findViewById(R.id.GTBbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recordTime(true, db, -1);
                    }
                }
        );

        // 記録時刻の修正・削除(長押し)
        for (int i=0; i<idCount; i++) {
            // 起床時刻の修正・削除
            final int finalI = (int) (idCount - i) - 1;
            textGU[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    recordTime(false, db, finalI);
                    return true;
                }
            });

            // 就寝時刻の修正・削除
            final int finalI1 = (int) (idCount - i) - 1;
            textGTB[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    recordTime(true, db, finalI1);
                    return true;
                }
            });
        }
    }

    // 起床or就寝時刻ボタンまたは記録のテキストビューを押したときに呼ばれるメソッド
    public void recordTime(final boolean sleep, final SQLiteDatabase db, final int i) {

        String sleepText;
        if (sleep == false) {
            sleepText = "起床";
        }else {
            sleepText = "就寝";
        }

        String updateORadd = "記録";

        calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        String tableName;
        if (i != -1) {
            if (sleep == false) {
                tableName = "GetUpTable";
            }else {
                tableName = "GoToBedTable";
            }

            updateORadd = "修正";

            Cursor cursor0 = db.query("DateTable", new String[] {"id", "year", "month", "date"}, "id=" + i, null, null, null, null);
            cursor0.moveToFirst();
            year = cursor0.getInt(1);
            month = cursor0.getInt(2);
            date = cursor0.getInt(3);
            cursor0.close();

            Cursor cursor = db.query(tableName, new String[] {"id", "hour", "minute"}, "id=" + i, null, null, null, null);
            cursor.moveToFirst();
            hour = cursor.getInt(1);
            minute = cursor.getInt(2);
            cursor.close();

            if (hour == -1) {
                hour = calendar.get(Calendar.HOUR_OF_DAY);
            }
            if (minute == -1) {
                minute = calendar.get(Calendar.MINUTE);
            }
        }

        // タイムピッカーを表示
        final CustomTimePickerDialog timePickerDialog;
        final CustomTimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int s_hour, int s_minute) {

            }
        };
        timePickerDialog = new CustomTimePickerDialog(MainActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, hour, minute, true);

        timePickerDialog.setTitle(year + "年" + month + "月" + date + "日の" + sleepText + "時刻");
        timePickerDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                updateORadd + "する",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        long idNumber;
                        if (i == -1) {
                            idNumber = DatabaseUtils.queryNumEntries(db, "DateTable") - 1;
                        }else {
                            idNumber = i;
                        }

                        timeHandler.updateTime(sleep, db, (int) idNumber, year, month, date, hour, minute);

                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);

                        dialog.dismiss();
                    }
                }
        );

        if (i != -1) {
            timePickerDialog.setButton(
                    DialogInterface.BUTTON_NEUTRAL,
                    "削除する",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            timeHandler.updateTime(sleep, db, i, year, month, date, -1, -1);

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);

                            dialog.dismiss();
                        }
                    }
            );
        }

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
