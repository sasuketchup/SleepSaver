package com.example.sleepsaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.fragment.app.FragmentActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class PrefActivity extends PreferenceActivity {

    TimeHandler timeHandler = new TimeHandler();

    // 表示範囲を格納する変数
    int resultsNum;

    // 表示範囲の指定日を格納する変数
    // 指定日～今日の指定日
    int spec_year;
    int spec_month;
    int spec_date;
    // 指定日1～指定日2の指定日1
    int spec_year1;
    int spec_month1;
    int spec_date1;
    // 指定日1～指定日2の指定日2
    int spec_year2;
    int spec_month2;
    int spec_date2;

    // 指定日表示用のString
    String spec_St;

    // 1日のサイクルを格納する変数(4桁→それぞれ)
    int stay_up_line;
    int sleeping_line;
    int hour_line;
    int minute_line;
    int hour_line2;
    int minute_line2;

    String stay_up_St;
    String sleeping_St;

    AlertDialog alertDialog;

    int resultsWhich;

    // ピッカーの日付を変更したときに変数に代入するようにするために、DatePickerDialogを継承したクラス(指定日～今日用)
    public class CustomDatePickerDialog extends DatePickerDialog {
        public CustomDatePickerDialog(Context context, OnDateSetListener listener, int year, int month, int date) {
            super(context, listener, year, month, date);
        }
        @Override
        public void onDateChanged(DatePicker view, int year, int month, int date) {
            spec_year = year;
            spec_month = month;
            spec_date = date;
        }
    }

    // ピッカーの日付を変更したときに変数に代入するようにするために、DatePickerDialogを継承したクラス(指定日1～指定日2の指定日1用)
    public class CustomDatePickerDialog1 extends DatePickerDialog {
        public CustomDatePickerDialog1(Context context, OnDateSetListener listener, int year, int month, int date) {
            super(context, listener, year, month, date);
        }
        @Override
        public void onDateChanged(DatePicker view, int year, int month, int date) {
            spec_year1 = year;
            spec_month1 = month;
            spec_date1 = date;
        }
    }

    // ピッカーの日付を変更したときに変数に代入するようにするために、DatePickerDialogを継承したクラス(指定日1～指定日2の指定日2用)
    public class CustomDatePickerDialog2 extends DatePickerDialog {
        public CustomDatePickerDialog2(Context context, OnDateSetListener listener, int year, int month, int date) {
            super(context, listener, year, month, date);
        }
        @Override
        public void onDateChanged(DatePicker view, int year, int month, int date) {
            spec_year2 = year;
            spec_month2 = month;
            spec_date2 = date;
        }
    }

    // ピッカーの時刻を変更したときに変数に代入するようにするために、TimePickerDialogを継承したクラス(起床→就寝用)
    public class CustomTimePickerDialog extends TimePickerDialog {

        public CustomTimePickerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
            super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker view, int s_hour, int s_minute) {
            hour_line = s_hour;
            minute_line = s_minute;
        }
    }
    // ピッカーの時刻を変更したときに変数に代入するようにするために、TimePickerDialogを継承したクラス(就寝→起床用)
    public class CustomTimePickerDialog2 extends TimePickerDialog {

        public CustomTimePickerDialog2(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
            super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker view, int s_hour, int s_minute) {
            hour_line2 = s_hour;
            minute_line2 = s_minute;
        }
    }

    // 表示範囲の日付を指定するメソッド
    public void designateDate(final PreferenceScreen button, final int spec_point, final DatePickerDialog datePickerDialog) {
        // 最大値を今日に
        Calendar cal_max = Calendar.getInstance();
        cal_max.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(PrefActivity.this) + 1);
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMaxDate(cal_max.getTimeInMillis());

        // 指定日2の時、最小値を指定日1に
        if (spec_point == 2) {
            Calendar cal_min = Calendar.getInstance();
            cal_min.set(spec_year1, spec_month1, spec_date1);
            datePicker.setMinDate(cal_min.getTimeInMillis());
        }


        if (spec_point != 0) {
            datePickerDialog.setTitle("指定日" + spec_point);
        }
        datePickerDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (spec_point == 0) {
                            resultsNum = -1;
                            spec_St = timeHandler.dateString(spec_year, spec_month + 1, spec_date) + "～今日";
                        } else if (spec_point == 1) {
                            // 指定日2のカスタムデイトピッカーダイアログ
                            CustomDatePickerDialog2 datePickerDialog2;
                            CustomDatePickerDialog2.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                }
                            };
                            if (spec_year2 == 0 && spec_month2 == 0 && spec_date2 == 0) {
                                Calendar cal_now = Calendar.getInstance();
                                cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(PrefActivity.this));
                                spec_year2 = cal_now.get(Calendar.YEAR);
                                spec_month2 = cal_now.get(Calendar.MONTH);
                                spec_date2 = cal_now.get(Calendar.DAY_OF_MONTH);
                            }
                            datePickerDialog2 = new CustomDatePickerDialog2(PrefActivity.this, listener, spec_year2, spec_month2, spec_date2);
                            designateDate(button, 2, datePickerDialog2);
                        } else if (spec_point == 2) {
                            resultsNum = -2;
                            spec_St = timeHandler.dateString(spec_year1, spec_month1 + 1, spec_date1) + "～" + timeHandler.dateString(spec_year2, spec_month2 + 1, spec_date2);
                        }
                        button.setSummary("記録の表示範囲: " + spec_St);
                        dialog.dismiss();
                    }
                }
        );
        datePickerDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "キャンセル",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        datePickerDialog.show();
    }

    // 1日のサイクルのボタンを押したときに呼ばれるメソッド
    public void cycleButton(final PreferenceScreen button, final boolean state, TimePickerDialog timePickerDialog) {

        String switchingText;
        final String textSure;
        if (state == true) {
            switchingText = "起床→就寝";
            textSure = "起き";
        } else {
            switchingText = "就寝→起床";
            textSure = "寝";
        }

        // タイムピッカーを表示
        timePickerDialog.setTitle(switchingText + "切り替え時刻");
        timePickerDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String line_St = "--:--";
                        if (state == true) {
                            stay_up_line = timeHandler.time_to_number(hour_line, minute_line);
                            line_St = timeHandler.timeString(hour_line, minute_line);
                        } else {
                            sleeping_line = timeHandler.time_to_number(hour_line2, minute_line2);
                            line_St = timeHandler.timeString(hour_line2, minute_line2);
                        }
                        button.setSummary(line_St + "\n確実に" + textSure + "ている時刻を指定してください。");

                        dialogInterface.dismiss();
                    }
                }
        );
        timePickerDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "キャンセル",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 値をリセット
                        if (state == true) {
                            hour_line = timeHandler.number_to_time(stay_up_line)[0];
                            minute_line = timeHandler.number_to_time(stay_up_line)[1];
                        } else {
                            hour_line2 = timeHandler.number_to_time(sleeping_line)[0];
                            minute_line2 = timeHandler.number_to_time(sleeping_line)[1];
                        }

                        dialogInterface.dismiss();
                    }
                }
        );
        timePickerDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        SharedPreferences sp = PrefActivity.this.getSharedPreferences("pref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        // 表示範囲の選択肢
        final String[] resultsSt = {"すべて表示", "過去1週間", "過去2週間", "過去3週間", "過去4週間", "指定日～今日", "指定日1～指定日2"};
        // 表示範囲を取得
        resultsNum = sp.getInt("results", 0);
        switch (resultsNum) {
            case 0:
                resultsWhich = 0;
                break;
            case 7:
                resultsWhich = 1;
                break;
            case 14:
                resultsWhich = 2;
                break;
            case 21:
                resultsWhich = 3;
                break;
            case 28:
                resultsWhich = 4;
                break;
            case -1:
                resultsWhich = 5;
                break;
            case -2:
                resultsWhich = 6;
                break;
        }

        // 表示範囲の指定日をDBから取得
        Cursor cursor = db.query("RangeTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        cursor.moveToFirst();
        spec_year = cursor.getInt(1);
        spec_month = cursor.getInt(2);
        spec_date = cursor.getInt(3);
        cursor.moveToNext();
        spec_year1 = cursor.getInt(1);
        spec_month1 = cursor.getInt(2);
        spec_date1 = cursor.getInt(3);
        cursor.moveToNext();
        spec_year2 = cursor.getInt(1);
        spec_month2 = cursor.getInt(2);
        spec_date2 = cursor.getInt(3);
        cursor.close();

        // 表示範囲ボタン
        final PreferenceScreen resultsBtn = (PreferenceScreen) findPreference("results");
        if (resultsWhich < 5) {
            resultsBtn.setSummary("記録の表示範囲: " + resultsSt[resultsWhich]);
        } else {
            if (resultsWhich == 5) {
                spec_St = timeHandler.dateString(spec_year, spec_month + 1, spec_date) + "～今日";
            } else if (resultsWhich == 6) {
                spec_St = timeHandler.dateString(spec_year1, spec_month1 + 1, spec_date1) + "～" + timeHandler.dateString(spec_year2, spec_month2 + 1, spec_date2);
            }
            resultsBtn.setSummary("記録の表示範囲: " + spec_St);
        }
        resultsBtn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
                        DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                resultsWhich = i;
                                switch (resultsWhich) {
                                    case 0:
                                        resultsNum = 0;
                                        break;
                                    case 1:
                                        resultsNum = 7;
                                        break;
                                    case 2:
                                        resultsNum = 14;
                                        break;
                                    case 3:
                                        resultsNum = 21;
                                        break;
                                    case 4:
                                        resultsNum = 28;
                                        break;
                                    case 5:
                                        // 指定日～今日のカスタムデイトピッカーダイアログ
                                        CustomDatePickerDialog datePickerDialog;
                                        CustomDatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                            }
                                        };
                                        if (spec_year == 0 && spec_month == 0 && spec_date == 0) {
                                            Calendar cal_now = Calendar.getInstance();
                                            cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(PrefActivity.this));
                                            spec_year = cal_now.get(Calendar.YEAR);
                                            spec_month = cal_now.get(Calendar.MONTH);
                                            spec_date = cal_now.get(Calendar.DAY_OF_MONTH);
                                        }
                                        datePickerDialog = new CustomDatePickerDialog(PrefActivity.this, listener, spec_year, spec_month, spec_date);

                                        designateDate(resultsBtn, 0, datePickerDialog);

//                                        // 最大値を今日に
//                                        Calendar cal_max = Calendar.getInstance();
//                                        cal_max.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(PrefActivity.this));
//                                        DatePicker datePicker = datePickerDialog.getDatePicker();
//                                        datePicker.setMaxDate(cal_max.getTimeInMillis());
//
//                                        datePickerDialog.setButton(
//                                                DialogInterface.BUTTON_POSITIVE,
//                                                "OK",
//                                                new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        resultsNum = -1;
//                                                        spec_St = timeHandler.dateString(spec_year, spec_month + 1, spec_date) + "～今日";
//                                                        resultsBtn.setSummary("記録の表示範囲: " + spec_St);
//                                                        dialog.dismiss();
//                                                    }
//                                                }
//                                        );
//                                        datePickerDialog.setButton(
//                                                DialogInterface.BUTTON_NEGATIVE,
//                                                "キャンセル",
//                                                new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        dialog.dismiss();
//                                                    }
//                                                }
//                                        );
//                                        datePickerDialog.show();
                                        break;
                                    case 6:
                                        // 指定日1のカスタムデイトピッカーダイアログ
                                        CustomDatePickerDialog1 datePickerDialog1;
                                        CustomDatePickerDialog1.OnDateSetListener listener1 = new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                            }
                                        };
                                        if (spec_year1 == 0 && spec_month1 == 0 && spec_date1 == 0) {
                                            Calendar cal_now = Calendar.getInstance();
                                            cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(PrefActivity.this));
                                            spec_year1 = cal_now.get(Calendar.YEAR);
                                            spec_month1 = cal_now.get(Calendar.MONTH);
                                            spec_date1 = cal_now.get(Calendar.DAY_OF_MONTH);
                                        }
                                        datePickerDialog1 = new CustomDatePickerDialog1(PrefActivity.this, listener1, spec_year1, spec_month1, spec_date1);

                                        designateDate(resultsBtn, 1, datePickerDialog1);

                                        break;
                                }
                                if (resultsWhich < 5) {
                                    resultsBtn.setSummary("記録の表示範囲: " + resultsSt[resultsWhich]);
                                }
                                alertDialog.dismiss();
                            }
                        };
                        builder.setTitle("表示範囲").setSingleChoiceItems(resultsSt, resultsWhich, onDialogClickListener);
                        alertDialog = builder.show();
                        return true;
                    }
                }
        );

        // 起床→就寝切り替え時刻を取得
        stay_up_line = sp.getInt("stay_up_line", 1200);
        hour_line = timeHandler.number_to_time(stay_up_line)[0];
        minute_line = timeHandler.number_to_time(stay_up_line)[1];
        stay_up_St = timeHandler.timeString(hour_line, minute_line);

        // 起床→就寝切り替え時刻ボタン
        final PreferenceScreen stay_up_btn = (PreferenceScreen) findPreference("stay_up_line");
        stay_up_btn.setSummary(stay_up_St + "\n確実に起きている時刻を指定してください。");
        stay_up_btn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // 1つ目のカスタム型
                        CustomTimePickerDialog timePickerDialog;
                        CustomTimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                            }
                        };
                        timePickerDialog = new CustomTimePickerDialog(PrefActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, hour_line, minute_line, true);

                        cycleButton(stay_up_btn, true, timePickerDialog);

                        return true;
                    }
                }
        );

        // 就寝→起床切り替え時刻を取得
        sleeping_line = sp.getInt("sleeping_line", 0);
        hour_line2 = timeHandler.number_to_time(sleeping_line)[0];
        minute_line2 = timeHandler.number_to_time(sleeping_line)[1];
        sleeping_St = timeHandler.timeString(hour_line2, minute_line2);

        // 就寝→起床切り替え時刻ボタン
        final PreferenceScreen sleeping_btn = (PreferenceScreen) findPreference("sleeping_line");
        sleeping_btn.setSummary(sleeping_St + "\n確実に寝ている時刻を指定してください。");
        sleeping_btn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // 2つ目のカスタム型
                        CustomTimePickerDialog2 timePickerDialog;
                        CustomTimePickerDialog2.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                            }
                        };
                        timePickerDialog = new CustomTimePickerDialog2(PrefActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, hour_line2, minute_line2, true);

                        cycleButton(sleeping_btn, false, timePickerDialog);

                        return true;
                    }
                }
        );

        // 保存ボタン
        PreferenceScreen saveBtn = (PreferenceScreen) findPreference("save");
        saveBtn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // ダイアログを表示
                        final AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
                        builder.setTitle("設定を保存しますか？");
                        builder.setPositiveButton(
                                "保存する",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // 表示範囲
                                        editor.putInt("results", resultsNum);
                                        // 今日～指定日の指定日
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put("year", spec_year);
                                        contentValues.put("month", spec_month);
                                        contentValues.put("date", spec_date);
                                        db.update("RangeTable", contentValues, "id=" + 0, null);
                                        // 指定日1
                                        ContentValues contentValues1 = new ContentValues();
                                        contentValues1.put("year", spec_year1);
                                        contentValues1.put("month", spec_month1);
                                        contentValues1.put("date", spec_date1);
                                        db.update("RangeTable", contentValues1, "id=" + 1, null);
                                        // 指定日2
                                        ContentValues contentValues2 = new ContentValues();
                                        contentValues2.put("year", spec_year2);
                                        contentValues2.put("month", spec_month2);
                                        contentValues2.put("date", spec_date2);
                                        db.update("RangeTable", contentValues2, "id=" + 2, null);

                                        // 起床→就寝切り替え時刻
                                        editor.putInt("stay_up_line", stay_up_line);
                                        // 就寝→起床切り替え時刻
                                        editor.putInt("sleeping_line", sleeping_line);

                                        editor.commit();

                                        Intent intent = new Intent(PrefActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                        );
                        builder.setNegativeButton(
                                "キャンセル",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }
                        );
                        builder.show();

                        return true;
                    }
                }
        );
    }

    // バックボタン押下時の処理
    @Override
    public void onBackPressed() {
        // 保存されている設定値を取得
        SharedPreferences sp = PrefActivity.this.getSharedPreferences("pref", Context.MODE_PRIVATE);
        int results_back = sp.getInt("results", 0);
        int stay_up_back = sp.getInt("stay_up_line", 1200);
        int sleeping_back = sp.getInt("sleeping_line", 0);
        // 表示範囲の指定日をDBから取得
        MyOpenHelper helper = new MyOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("RangeTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        cursor.moveToFirst();
        int year_back = cursor.getInt(1);
        int month_back = cursor.getInt(2);
        int date_back = cursor.getInt(3);
        cursor.moveToNext();
        int year_back1 = cursor.getInt(1);
        int month_back1 = cursor.getInt(2);
        int date_back1 = cursor.getInt(3);
        cursor.moveToNext();
        int year_back2 = cursor.getInt(1);
        int month_back2 = cursor.getInt(2);
        int date_back2 = cursor.getInt(3);
        cursor.close();

        // 設定が変更されていなければ閉じる、そうでなければダイアログ表示
        if (results_back == resultsNum && stay_up_back == stay_up_line && sleeping_back == sleeping_line
                && year_back == spec_year && month_back == spec_month && date_back == spec_date
                && year_back1 == spec_year1 && month_back1 == spec_month1 && date_back1 == spec_date1
                && year_back2 == spec_year2 && month_back2 == spec_month2 && date_back2 == spec_date2) {
            finish();
        } else {
            // アラートダイアログ表示
            AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
            builder.setTitle("変更された設定があります。\n破棄しますか？");
            builder.setPositiveButton(
                    "破棄する",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }
            );
            builder.setNegativeButton(
                    "キャンセル",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }
            );
            builder.show();
        }
    }
}
