package com.example.sleepsaver;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    Calendar cal_spec;
    Calendar cal_spec1;
    Calendar cal_spec2;

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

        SharedPreferences sp = getSharedPreferences("pref", MODE_PRIVATE);
        // 表示範囲を取得
        int results = sp.getInt("results", 0);
        // 起床→就寝切り替え時刻を取得
        int stay_up_line = sp.getInt("stay_up_line", 1200);
        int hour_line = timeHandler.number_to_time(stay_up_line)[0];

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        // データの行数を取得
        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");

        // データが1行以上あるとき実行
        if(idCount > 0) {
            // 記録し忘れがある場合、差分を埋める
            timeHandler.fillForget(db, MainActivity.this);
        }else{
            // データが空のとき実行
            calendar = Calendar.getInstance();

            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            date = calendar.get(Calendar.DATE);

            timeHandler.insertTime(db, 0, year, month, date, -1, -1, -1, -1);
        }

        // データの行数を再取得
        idCount = DatabaseUtils.queryNumEntries(db, "DateTable");

        // データと表示件数の差分
        int diff_id = 0;

        // 指定日1～2の時、表示範囲を決めるための変数
        int diff_now_spec1 = (int) (idCount - 1);
        int diff_now_spec2 = 0;

        // 指定日1～指定日2(resultsが-2)の場合
        if (results == -2) {
            // 今日と指定日1、2の差日数を計算
            diff_now_spec1 = spec12_today(db, MainActivity.this)[0];
            diff_now_spec2 = spec12_today(db, MainActivity.this)[1];

//            Cursor cursor = db.query("RangeTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
//            cursor.moveToPosition(1);
//            int spec_year1 = cursor.getInt(1);
//            int spec_month1 = cursor.getInt(2);
//            int spec_date1 = cursor.getInt(3);
//            cursor.moveToNext();
//            int spec_year2 = cursor.getInt(1);
//            int spec_month2 = cursor.getInt(2);
//            int spec_date2 = cursor.getInt(3);
//            cursor.close();
//            // 取得した日付をセット
//            cal_spec1 = Calendar.getInstance();
//            cal_spec1.set(spec_year1, spec_month1, spec_date1);
//            cal_spec2 = Calendar.getInstance();
//            cal_spec2.set(spec_year2, spec_month2, spec_date2);
//            // 今日の日付を取得
//            cal_now = Calendar.getInstance();
//            cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(MainActivity.this));
//            // 今日と指定日1、2の差日数を計算
//            diff_now_spec1 = timeHandler.cal_diff_Days(cal_now, cal_spec1);
//            diff_now_spec2 = timeHandler.cal_diff_Days(cal_now, cal_spec2);
        }

        // 指定日～今日(resultsが-1)の場合に指定日と今日の差分を計算
        if (results == -1) {
            // 指定日と今日の差を計算し、表示件数に代入
            results = spec_today(db, MainActivity.this);

//            // 指定日をDBから取得
//            Cursor cursor = db.query("RangeTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
//            cursor.moveToFirst();
//            int spec_year = cursor.getInt(1);
//            int spec_month = cursor.getInt(2);
//            int spec_date = cursor.getInt(3);
//            cursor.close();
//            // 取得した日付をセット
//            cal_spec = Calendar.getInstance();
//            cal_spec.set(spec_year, spec_month, spec_date);
//            // 今日の日付を取得
//            cal_now = Calendar.getInstance();
//            cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(MainActivity.this));
//            // 指定日と今日の差を計算し、表示件数に代入
//            results = timeHandler.cal_diff_Days(cal_now, cal_spec) + 1;
        }

        // 表示件数を計算
        if (results > 0) {
            if (idCount > results) {
                diff_id = (int) (idCount - results);
                idCount = results;
            }
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

                // 睡眠時間計算のため、一つ前の就寝時刻を取得
                int hourGTBPrevious = cursor2.getInt(1);
                int minuteGTBPrevious = cursor2.getInt(2);

                // 起床・就寝の値が揃っているとき
                if (hourGTBPrevious != -1 && hourGU != -1) {
                    int dateST = 0;
                    // 日付を跨いだ場合(起床→就寝切り替え時刻より遅い(かつ24時より前の)場合は24時を跨ぐので引かれる方の日付をプラス1)
                    if (hourGTBPrevious > hour_line) {
                        dateST = 1;
                    }

                    Calendar calST = Calendar.getInstance();
                    calST.set(0, 0, dateST, hourGU, minuteGU);
                    calST.add(Calendar.HOUR, 0 - hourGTBPrevious);
                    calST.add(Calendar.MINUTE, 0 - minuteGTBPrevious);

                    int hourST = calST.get(Calendar.HOUR_OF_DAY);
                    int minuteST = calST.get(Calendar.MINUTE);

                    timeSTSt = timeHandler.timeString(hourST, minuteST);
                }
                textST[i].setText(timeSTSt);
                textST[i].setTextSize(30);
                textST[i].setHeight(150);
                textST[i].setGravity(Gravity.RIGHT);

                if (diff_now_spec2 <= i && i <= (diff_now_spec1 - 1)) {
                    varSTLay.addView(textST[i]);
                }
            }

//            timeLayout[i].setOrientation(LinearLayout.HORIZONTAL);
//            timeLayout[i].addView(textDate[i]);
//            timeLayout[i].addView(textGU[i]);
//            timeLayout[i].addView(textGTB[i]);
//            varRecordLay.addView(timeLayout[i], 0);
            if (diff_now_spec2 <= i && i <= diff_now_spec1) {
                varDateLay.addView(textDate[i]);
                varGULay.addView(textGU[i]);
                varGTBLay.addView(textGTB[i]);
            }
        }
        cursor.close();
        cursor1.close();
        cursor2.close();

        // 睡眠時間の表示位置調整のための空のテキストビュー
        TextView emptyST = new TextView(this);
        emptyST.setHeight(75);
        varSTLay.addView(emptyST, 0);

        // 睡眠データボタンの処理
        findViewById(R.id.data_page).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, DataActivity.class);
                        startActivity(intent);
                    }
                }
        );

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
            final int finalI = (int) (idCount - i) - 1 + diff_id;
            textGU[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    recordTime(false, db, finalI);
                    return true;
                }
            });

            // 就寝時刻の修正・削除
            final int finalI1 = (int) (idCount - i) - 1 + diff_id;
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

        // 現在の日付と時刻を取得
        calendar = Calendar.getInstance();
        // 該当する時刻の場合加算or減算
        calendar.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(this));

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

    // 指定日と今日の差分を計算するメソッド
    public int spec_today(SQLiteDatabase db, Context context) {
        // 指定日をDBから取得
        Cursor cursor = db.query("RangeTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        cursor.moveToFirst();
        int spec_year = cursor.getInt(1);
        int spec_month = cursor.getInt(2);
        int spec_date = cursor.getInt(3);
        cursor.close();
        // 取得した日付をセット
        Calendar cal_spec = Calendar.getInstance();
        cal_spec.set(spec_year, spec_month, spec_date);
        // 今日の日付を取得
        Calendar cal_now = Calendar.getInstance();
        cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context));
        // 指定日と今日の差を計算し、返す
        return timeHandler.cal_diff_Days(cal_now, cal_spec) + 1;
    }

    // 指定日1と今日、指定日2と今日の差分を計算するメソッド
    public int[] spec12_today(SQLiteDatabase db, Context context) {
        // 指定日1、2をDBから取得
        Cursor cursor = db.query("RangeTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        cursor.moveToPosition(1);
        int spec_year1 = cursor.getInt(1);
        int spec_month1 = cursor.getInt(2);
        int spec_date1 = cursor.getInt(3);
        cursor.moveToNext();
        int spec_year2 = cursor.getInt(1);
        int spec_month2 = cursor.getInt(2);
        int spec_date2 = cursor.getInt(3);
        cursor.close();
        // 取得した日付をセット
        Calendar cal_spec1 = Calendar.getInstance();
        cal_spec1.set(spec_year1, spec_month1, spec_date1);
        Calendar cal_spec2 = Calendar.getInstance();
        cal_spec2.set(spec_year2, spec_month2, spec_date2);
        // 今日の日付を取得
        Calendar cal_now = Calendar.getInstance();
        cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context));
        // 今日と指定日1、2の差日数を計算し配列として返す
        return new int[]{timeHandler.cal_diff_Days(cal_now, cal_spec1), timeHandler.cal_diff_Days(cal_now, cal_spec2)};
    }

    // dpをpxに変換するメソッド
    public int convertDp2Px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        int px = (int) (dp * scale);
        return px;
    }
}
