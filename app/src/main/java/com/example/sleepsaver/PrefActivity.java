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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class PrefActivity extends PreferenceActivity {

    TimeHandler timeHandler;

    // 表示範囲を格納する変数
    // static int resultsNum;
    // 表示範囲の指定日を格納する変数
    // 指定日～今日の指定日
//    static int spec_year;
//    static int spec_month;
//    static int spec_date;
    // 指定日1～指定日2の指定日1
//    static int spec_year1;
//    static int spec_month1;
//    static int spec_date1;
    // 指定日1～指定日2の指定日2
//    static int spec_year2;
//    static int spec_month2;
//    static int spec_date2;
    // 指定日表示用のString
    String spec_St;
    // 表示範囲の設定ボタン
    PreferenceScreen resultsBtn;

    // 新しいAPIレベル用のオリジナルタイムピッカー
    TimePicker originalTimePicker;

    // 1日のサイクルを格納する変数(4桁→それぞれ)
    int stay_up_line;
    int sleeping_line;
    int hour_line;
    int minute_line;
    int hour_line2;
    int minute_line2;
    // 切り替え時刻のString
    String stay_up_St;
    String sleeping_St;

    // 目標睡眠時間ボタン
    PreferenceScreen slp_target_btn;
    // 目標就寝時刻の4桁と時と分の変数
    int gtb_target;
    int gtb_target_hour;
    int gtb_target_minute;
    // 目標起床時刻の4桁と時と分の変数
    int gu_target;
    int gu_target_hour;
    int gu_target_minute;
    // 目標睡眠時間の4桁と時と分の変数
    int slp_target;
    int slp_target_hour;
    int slp_target_minute;
    // 目標のString
    String gtb_target_St;
    String gu_target_St;
    String slp_target_St;

    // 表示範囲のアラートダイアログ
    AlertDialog alertDialog;

    // 目標睡眠時間のアラートダイアログ
    AlertDialog alertDialog2;

    // 目標就寝時刻と目標起床時刻から目標睡眠時間を計算するためのCalender
    Calendar cal_diff_target;

    // 表示範囲の選択項目を保持する変数
    int resultsWhich;

    // 目標睡眠時間のアラートダイアログの選択されている項目を保持する変数
    int slp_which;

    // 押し忘れ入力時のデフォルト時刻の状態(と時刻)を格納する変数
    int default_gtb;
    int default_gu;
    // デフォルト時刻の戻り値として返す変数
    int default_time;
    // デフォルト時刻の選択項目を保持する変数
    int defaultWhich;
    // デフォルト時刻のアラートダイアログ
    AlertDialog alertDialog3;
    // デフォルト時刻の表示形式
    String time_defaultSt;
    // デフォルト時刻のダイアログに表示する選択肢&サマリーに表示
    String[] defaultSt = {"現在時刻", "前日の記録", "過去1週間の平均", time_defaultSt + "(自分で指定)"};

    // ポップアップ画面の表示or非表示の変数
    boolean display_popup;
    // 就寝・起床反転の変数
    boolean inversion;

    // ピッカーの日付を変更したときに変数に代入するようにするために、DatePickerDialogを継承したクラス(指定日～今日用)
    public class CustomDatePickerDialog extends DatePickerDialog {
        public CustomDatePickerDialog(Context context, OnDateSetListener listener, int year, int month, int date) {
            super(context, listener, year, month, date);
        }
        @Override
        public void onDateChanged(DatePicker view, int year, int month, int date) {
//            spec_year = year;
//            spec_month = month;
//            spec_date = date;
            timeHandler.setSpec_year(year);
            timeHandler.setSpec_month(month);
            timeHandler.setSpec_date(date);
        }
    }
    // ピッカーの日付を変更したときに変数に代入するようにするために、DatePickerDialogを継承したクラス(指定日1～指定日2の指定日1用)
    public class CustomDatePickerDialog1 extends DatePickerDialog {
        public CustomDatePickerDialog1(Context context, OnDateSetListener listener, int year, int month, int date) {
            super(context, listener, year, month, date);
        }
        @Override
        public void onDateChanged(DatePicker view, int year, int month, int date) {
            timeHandler.setSpec_year1(year);
            timeHandler.setSpec_month1(month);
            timeHandler.setSpec_date1(date);
        }
    }
    // ピッカーの日付を変更したときに変数に代入するようにするために、DatePickerDialogを継承したクラス(指定日1～指定日2の指定日2用)
    public class CustomDatePickerDialog2 extends DatePickerDialog {
        public CustomDatePickerDialog2(Context context, OnDateSetListener listener, int year, int month, int date) {
            super(context, listener, year, month, date);
        }
        @Override
        public void onDateChanged(DatePicker view, int year, int month, int date) {
            timeHandler.setSpec_year2(year);
            timeHandler.setSpec_month2(month);
            timeHandler.setSpec_date2(date);
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

    // ピッカーの時刻を変更したときに変数に代入するようにするために、TimePickerDialogを継承したクラス(目標就寝時刻用)
    public class CustomTimePickerDialog3 extends TimePickerDialog {

        public CustomTimePickerDialog3(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
            super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker view, int s_hour, int s_minute) {
            gtb_target_hour = s_hour;
            gtb_target_minute = s_minute;
        }
    }
    // ピッカーの時刻を変更したときに変数に代入するようにするために、TimePickerDialogを継承したクラス(目標起床時刻用)
    public class CustomTimePickerDialog4 extends TimePickerDialog {

        public CustomTimePickerDialog4(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
            super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker view, int s_hour, int s_minute) {
            gu_target_hour = s_hour;
            gu_target_minute = s_minute;
        }
    }
    // ピッカーの時刻を変更したときに変数に代入するようにするために、TimePickerDialogを継承したクラス(目標睡眠時間用)
    public class CustomTimePickerDialog5 extends TimePickerDialog {

        public CustomTimePickerDialog5(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
            super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker view, int s_hour, int s_minute) {
            slp_target_hour = s_hour;
            slp_target_minute = s_minute;
        }
    }

    // 表示範囲から選択肢をセットするメソッド
    public int setChoices(int resultsNum) {
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
        return resultsWhich;
    }

    // 表示範囲及び対象期間を設定しString型で返すメソッド
    public String setRange(final Context context, final int activityNum, final TimeHandler timeHandler) {

        // 表示範囲の選択肢
        final String[] resultsSt = {"すべて表示", "過去1週間", "過去2週間", "過去3週間", "過去4週間", "指定日～今日", "指定日1～指定日2"};
        // ダイアログのタイトル
        String dialogTitle = "表示範囲";
        if (context != PrefActivity.this) {
            resultsSt[0] = "すべて";
            dialogTitle = "対象期間";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                resultsWhich = i;
                switch (resultsWhich) {
                    case 0:
                        timeHandler.setResultsNum(0);
                        break;
                    case 1:
                        timeHandler.setResultsNum(7);
                        break;
                    case 2:
                        timeHandler.setResultsNum(14);
                        break;
                    case 3:
                        timeHandler.setResultsNum(21);
                        break;
                    case 4:
                        timeHandler.setResultsNum(28);
                        break;
                    case 5:
                        // 指定日～今日のカスタムデイトピッカーダイアログ
                        CustomDatePickerDialog datePickerDialog;
                        CustomDatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                            }
                        };
                        if (timeHandler.getSpec_year() == 0 && timeHandler.getSpec_month() == 0 && timeHandler.getSpec_date() == 0) {
                            Calendar cal_now = Calendar.getInstance();
                            cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context));
                            timeHandler.setSpec_year(cal_now.get(Calendar.YEAR));
                            timeHandler.setSpec_month(cal_now.get(Calendar.MONTH));
                            timeHandler.setSpec_date(cal_now.get(Calendar.DAY_OF_MONTH));
                        }
                        datePickerDialog = new CustomDatePickerDialog(context, listener, timeHandler.getSpec_year(), timeHandler.getSpec_month(), timeHandler.getSpec_date());

                        spec_St = designateDate(0, datePickerDialog, context, activityNum);
                        // resultsBtn.setSummary("記録の表示範囲: " + designateDate(resultsBtn, 0, datePickerDialog));
                        break;
                    case 6:
                        // 指定日1のカスタムデイトピッカーダイアログ
                        CustomDatePickerDialog1 datePickerDialog1;
                        CustomDatePickerDialog1.OnDateSetListener listener1 = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                            }
                        };
                        if (timeHandler.getSpec_year1() == 0 && timeHandler.getSpec_month1() == 0 && timeHandler.getSpec_date1() == 0) {
                            Calendar cal_now = Calendar.getInstance();
                            cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context));
                            timeHandler.setSpec_year1(cal_now.get(Calendar.YEAR));
                            timeHandler.setSpec_month1(cal_now.get(Calendar.MONTH));
                            timeHandler.setSpec_date1(cal_now.get(Calendar.DAY_OF_MONTH));
                        }
                        datePickerDialog1 = new CustomDatePickerDialog1(context, listener1, timeHandler.getSpec_year1(), timeHandler.getSpec_month1(), timeHandler.getSpec_date1());

                        spec_St = designateDate(1, datePickerDialog1, context, activityNum);
                        // resultsBtn.setSummary("記録の表示範囲: " + designateDate(resultsBtn, 1, datePickerDialog1));
                        break;
                }
                alertDialog.dismiss();

                // すべて～過去4週間までのみ
                if (resultsWhich < 5) {
                    spec_St = resultsSt[resultsWhich];
                    // resultsBtn.setSummary("記録の表示範囲: " + resultsSt[resultsWhich]);

                    setOrUpdateRange(context, activityNum);
                }
            }
        };
        builder.setTitle(dialogTitle).setSingleChoiceItems(resultsSt, resultsWhich, onDialogClickListener);
        alertDialog = builder.show();

        return spec_St;
    }

    // 表示範囲の日付を指定しString型で返すメソッド
    public String designateDate(final int spec_point, final DatePickerDialog datePickerDialog, final Context context, final int activityNum) {
        // 最大値を今日に
        Calendar cal_max = Calendar.getInstance();
        cal_max.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context) + 1);
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setMaxDate(cal_max.getTimeInMillis());

        // 指定日2の時、最小値を指定日1に
        if (spec_point == 2) {
            Calendar cal_min = Calendar.getInstance();
            cal_min.set(timeHandler.getSpec_year1(), timeHandler.getSpec_month1(), timeHandler.getSpec_date1());
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
                            timeHandler.setResultsNum(-1);
                            // resultsNum = -1;
                            spec_St = timeHandler.dateString(timeHandler.getSpec_year(), timeHandler.getSpec_month() + 1, timeHandler.getSpec_date()) + "～今日";
                        } else if (spec_point == 1) {
                            // 指定日2のカスタムデイトピッカーダイアログ
                            CustomDatePickerDialog2 datePickerDialog2;
                            CustomDatePickerDialog2.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                }
                            };
                            if (timeHandler.getSpec_year2() == 0 && timeHandler.getSpec_month2() == 0 && timeHandler.getSpec_date2() == 0) {
                                Calendar cal_now = Calendar.getInstance();
                                cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context));
                                timeHandler.setSpec_year2(cal_now.get(Calendar.YEAR));
                                timeHandler.setSpec_month2(cal_now.get(Calendar.MONTH));
                                timeHandler.setSpec_date2(cal_now.get(Calendar.DAY_OF_MONTH));
                            }
                            datePickerDialog2 = new CustomDatePickerDialog2(context, listener, timeHandler.getSpec_year2(), timeHandler.getSpec_month2(), timeHandler.getSpec_date2());
                            spec_St = designateDate(2, datePickerDialog2, context, activityNum);
                        } else if (spec_point == 2) {
                            timeHandler.setResultsNum(-2);
                            // resultsNum = -2;
                            spec_St = timeHandler.dateString(timeHandler.getSpec_year1(), timeHandler.getSpec_month1() + 1, timeHandler.getSpec_date1()) + "～" + timeHandler.dateString(timeHandler.getSpec_year2(), timeHandler.getSpec_month2() + 1, timeHandler.getSpec_date2());
                        }
                        // button.setSummary("記録の表示範囲: " + spec_St);
                        dialog.dismiss();

                        // 指定日～今日の指定日か指定日2の時
                        if (spec_point != 1) {
                            setOrUpdateRange(context, activityNum);
                        }
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

        return spec_St;
    }

    // 範囲をサマリーに表示または更新するメソッド
    public void setOrUpdateRange(Context context, int activityNum) {
        // 設定画面から呼んだ場合サマリーに表示
        if (context == PrefActivity.this) {
            resultsBtn.setSummary("記録の表示範囲: " + spec_St);
        } else if (activityNum == 2) { // 睡眠データ画面から呼んだ場合アクティビティを再スタート
            Intent intent = new Intent(context, DataActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Update", true);
            context.startActivity(intent);
        } else if (activityNum == 1) {
            timeHandler.showDialog(context, 1, "CSV出力", spec_St + "の記録を出力します。", "OK", "キャンセル");
//            String state = Environment.getExternalStorageState();
//            if (Environment.MEDIA_MOUNTED.equals(state)) {
//                // File exportDir = new File("/storage/sdcard/Android/data/com.example.sleepsaver/Download");
//                // String exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
//                File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                if (!exportDir.exists()) {
//                    exportDir.mkdirs();
//                    Toast.makeText(context, "mkdirs", Toast.LENGTH_LONG).show();
//                }
//
//                File file;
//                PrintWriter printWriter = null;
//                try {
//                    file = new File(exportDir, "test1File.csv");
//                    boolean test = file.createNewFile();
//                    if (!test) {
//                        Toast.makeText(context, "false", Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(context, "true", Toast.LENGTH_LONG).show();
//                    }
//                    FileOutputStream fos = new FileOutputStream(file, true);
//                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
//                    printWriter = new PrintWriter(osw);
//                    printWriter.print("test");
//                    printWriter.println();
//                    Toast.makeText(context, "OK", Toast.LENGTH_LONG).show();
//                } catch (FileNotFoundException exc) {
//                    Toast.makeText(context, "アクセス権限がありません", Toast.LENGTH_LONG).show();
//                } catch (Exception e) {
//                    Toast.makeText(context, "CSV出力に失敗しました", Toast.LENGTH_LONG).show();
//                } finally {
//                    if (printWriter != null) {
//                        printWriter.close();
//                    }
//                }
//            }
        }
    }

    // resultsNumを返すためのメソッド
//    public int getResultsNum() {
//        return resultsNum;
//    }

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

        // APIレベルによってタイムピッカーの表示方法を分ける
        if (Build.VERSION.SDK_INT >= 23) {
            // 自作のタイムピッカーを表示
            LayoutInflater inflater = getLayoutInflater();
            View originalDialog = inflater.inflate(R.layout.dialog_original_time_picker, (ViewGroup) findViewById(R.id.dialog_root));

            originalTimePicker = originalDialog.findViewById(R.id.originalTimePicker);
            originalTimePicker.setIs24HourView(true);
            if (state) {
                originalTimePicker.setHour(hour_line);
                originalTimePicker.setMinute(minute_line);
            } else {
                originalTimePicker.setHour(hour_line2);
                originalTimePicker.setMinute(minute_line2);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
            builder.setView(originalDialog);
            builder.setTitle(switchingText + "切り替え時刻");
            builder.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String line_St = "--:--";
                            if (state == true) {
                                hour_line = originalTimePicker.getHour();
                                minute_line = originalTimePicker.getMinute();
                                stay_up_line = timeHandler.time_to_number(hour_line, minute_line);
                                line_St = timeHandler.timeString(hour_line, minute_line);
                            } else {
                                hour_line2 = originalTimePicker.getHour();
                                minute_line2 = originalTimePicker.getMinute();
                                sleeping_line = timeHandler.time_to_number(hour_line2, minute_line2);
                                line_St = timeHandler.timeString(hour_line2, minute_line2);
                            }
                            button.setSummary(line_St + "\n確実に" + textSure + "ている時刻を指定してください。");

                            dialog.dismiss();
                        }
                    }
            );
            builder.setNegativeButton(
                    "キャンセル",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 値をリセット
                            if (state == true) {
                                hour_line = timeHandler.number_to_time(stay_up_line)[0];
                                minute_line = timeHandler.number_to_time(stay_up_line)[1];
                            } else {
                                hour_line2 = timeHandler.number_to_time(sleeping_line)[0];
                                minute_line2 = timeHandler.number_to_time(sleeping_line)[1];
                            }

                            dialog.dismiss();
                        }
                    }
            );
            builder.show();
        } else {
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
    }

    // それぞれの目標ボタンを押したときに呼ばれるメソッド
    public void targetButton(final PreferenceScreen button, final int target_state, final TimePickerDialog timePickerDialog) {

        // サマリーに表示するために目標睡眠時間ボタンを取得
        slp_target_btn = (PreferenceScreen) findPreference("sleeping_target");

        String targetText = "";
//        final String textSure;
        if (target_state == 3) {
            targetText = "就寝時刻";
//            textSure = "起き";
        } else if (target_state == 4){
            targetText = "起床時刻";
//            textSure = "寝";
        } else if (target_state == 5) {
            targetText = "睡眠時間";
        }

        // APIレベルによってタイムピッカーの表示方法を分ける
        if (Build.VERSION.SDK_INT >= 23) {
            // 自作のタイムピッカーを表示
            LayoutInflater inflater = getLayoutInflater();
            View originalDialog = inflater.inflate(R.layout.dialog_original_time_picker, (ViewGroup) findViewById(R.id.dialog_root));

            originalTimePicker = originalDialog.findViewById(R.id.originalTimePicker);
            originalTimePicker.setIs24HourView(true);
            if (target_state == 3) {
                originalTimePicker.setHour(gtb_target_hour);
                originalTimePicker.setMinute(gtb_target_minute);
            } else if (target_state == 4) {
                originalTimePicker.setHour(gu_target_hour);
                originalTimePicker.setMinute(gu_target_minute);
            } else if (target_state == 5) {
                originalTimePicker.setHour(slp_target_hour);
                originalTimePicker.setMinute(slp_target_minute);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
            builder.setView(originalDialog);
            builder.setTitle("目標" + targetText);
            builder.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Calendar cal_summary;
                            String target_St = "--:--";
                            if (target_state == 3) {
                                gtb_target_hour = originalTimePicker.getHour();
                                gtb_target_minute = originalTimePicker.getMinute();
                                gtb_target = timeHandler.time_to_number(gtb_target_hour, gtb_target_minute);
                                target_St = timeHandler.timeString(gtb_target_hour, gtb_target_minute);
                                // slp_targetがマイナスの時、差を計算してサマリーに表示
                                if (slp_target < 0) {
                                    cal_summary = timeHandler.diff_gu_gtb(gu_target, gtb_target);
                                    slp_target_btn.setSummary(timeHandler.timeString(cal_summary.get(Calendar.HOUR_OF_DAY), cal_summary.get(Calendar.MINUTE)));
                                }
                            } else if (target_state == 4) {
                                gu_target_hour = originalTimePicker.getHour();
                                gu_target_minute = originalTimePicker.getMinute();
                                gu_target = timeHandler.time_to_number(gu_target_hour, gu_target_minute);
                                target_St = timeHandler.timeString(gu_target_hour, gu_target_minute);
                                // slp_targetがマイナスの時、差を計算してサマリーに表示
                                if (slp_target < 0) {
                                    cal_summary = timeHandler.diff_gu_gtb(gu_target, gtb_target);
                                    slp_target_btn.setSummary(timeHandler.timeString(cal_summary.get(Calendar.HOUR_OF_DAY), cal_summary.get(Calendar.MINUTE)));
                                }
                            } else if (target_state == 5) {
                                slp_which = 1;
                                slp_target_hour = originalTimePicker.getHour();
                                slp_target_minute = originalTimePicker.getMinute();
                                slp_target = timeHandler.time_to_number(slp_target_hour, slp_target_minute);
                                target_St = timeHandler.timeString(slp_target_hour, slp_target_minute);
                            }
                            button.setSummary(target_St);

                            dialog.dismiss();
                        }
                    }
            );
            builder.setNegativeButton(
                    "キャンセル",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 値をリセット
                            if (target_state == 3) {
                                gtb_target_hour = timeHandler.number_to_time(gtb_target)[0];
                                gtb_target_minute = timeHandler.number_to_time(gtb_target)[1];
                            } else if (target_state == 4) {
                                gu_target_hour = timeHandler.number_to_time(gu_target)[0];
                                gu_target_minute = timeHandler.number_to_time(gu_target)[1];
                            } else if (target_state == 5) {
                                slp_target_hour = timeHandler.number_to_time(Math.abs(slp_target))[0];
                                slp_target_minute = timeHandler.number_to_time(Math.abs(slp_target))[1];
                            }
                            dialog.dismiss();
                        }
                    }
            );
            builder.show();
        } else {
            // タイムピッカーを表示
            timePickerDialog.setTitle("目標" + targetText);
            timePickerDialog.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Calendar cal_summary;
                            String target_St = "--:--";
                            if (target_state == 3) {
                                gtb_target = timeHandler.time_to_number(gtb_target_hour, gtb_target_minute);
                                target_St = timeHandler.timeString(gtb_target_hour, gtb_target_minute);
                                // slp_targetがマイナスの時、差を計算してサマリーに表示
                                if (slp_target < 0) {
                                    cal_summary = timeHandler.diff_gu_gtb(gu_target, gtb_target);
                                    slp_target_btn.setSummary(timeHandler.timeString(cal_summary.get(Calendar.HOUR_OF_DAY), cal_summary.get(Calendar.MINUTE)));
                                }
                            } else if (target_state == 4) {
                                gu_target = timeHandler.time_to_number(gu_target_hour, gu_target_minute);
                                target_St = timeHandler.timeString(gu_target_hour, gu_target_minute);
                                // slp_targetがマイナスの時、差を計算してサマリーに表示
                                if (slp_target < 0) {
                                    cal_summary = timeHandler.diff_gu_gtb(gu_target, gtb_target);
                                    slp_target_btn.setSummary(timeHandler.timeString(cal_summary.get(Calendar.HOUR_OF_DAY), cal_summary.get(Calendar.MINUTE)));
                                }
                            } else if (target_state == 5) {
                                slp_which = 1;
                                slp_target = timeHandler.time_to_number(slp_target_hour, slp_target_minute);
                                target_St = timeHandler.timeString(slp_target_hour, slp_target_minute);
                            }
                            button.setSummary(target_St);

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
                            if (target_state == 3) {
                                gtb_target_hour = timeHandler.number_to_time(gtb_target)[0];
                                gtb_target_minute = timeHandler.number_to_time(gtb_target)[1];
                            } else if (target_state == 4) {
                                gu_target_hour = timeHandler.number_to_time(gu_target)[0];
                                gu_target_minute = timeHandler.number_to_time(gu_target)[1];
                            } else if (target_state == 5) {
                                slp_target_hour = timeHandler.number_to_time(Math.abs(slp_target))[0];
                                slp_target_minute = timeHandler.number_to_time(Math.abs(slp_target))[1];
                            }
                            dialogInterface.dismiss();
                        }
                    }
            );
            timePickerDialog.show();
        }
    }

    // それぞれのデフォルト時刻ボタンを押したときに呼ばれるメソッド
    public int defaultButton(final PreferenceScreen button, final boolean state, final int time_default) {
        // 引数time_defaultから時刻を抽出
        final int hour_default = timeHandler.number_to_time(time_default % 10000)[0];
        final int minute_default = timeHandler.number_to_time(time_default % 10000)[1];
        time_defaultSt = timeHandler.timeString(hour_default, minute_default);
        // 選択肢に表示
        defaultSt[3] = time_defaultSt + "(自分で指定)";

        // ダイアログのタイトル
        String dialogTitle = "起床";
        if (!state) {
            dialogTitle = "就寝";
        }

        // 引数time_defaultから選択されている項目を抽出
        defaultWhich = (time_default - (time_default % 10000)) / 10000;

        AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
        final String finalDialogTitle = dialogTitle;
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                defaultWhich = which;
                switch (defaultWhich) {
                    case 0:
                        default_time = time_default % 10000;
                        break;
                    case 1:
                        default_time = (time_default % 10000) + 10000;
                        break;
                    case 2:
                        default_time = (time_default % 10000) + 20000;
                        break;
                    case 3:
                        // 自作のタイムピッカーを表示
                        LayoutInflater inflater = getLayoutInflater();
                        View originalDialog = inflater.inflate(R.layout.dialog_original_time_picker, (ViewGroup) findViewById(R.id.dialog_root));

                        originalTimePicker = originalDialog.findViewById(R.id.originalTimePicker);
                        originalTimePicker.setIs24HourView(true);
                        // APIレベルによって分ける
                        if (Build.VERSION.SDK_INT >= 23) {
                            originalTimePicker.setHour(hour_default);
                            originalTimePicker.setMinute(minute_default);
                        } else {
                            originalTimePicker.setCurrentHour(hour_default);
                            originalTimePicker.setCurrentMinute(minute_default);
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
                        builder.setView(originalDialog);
                        builder.setTitle("押し忘れ入力時の初期表示時刻(" + finalDialogTitle + ")");
                        builder.setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // ピッカーの時刻を取得
                                        int default_hour;
                                        int default_minute;
                                        if (Build.VERSION.SDK_INT >= 23) { // APIレベルによって分ける
                                            default_hour = originalTimePicker.getHour();
                                            default_minute = originalTimePicker.getMinute();
                                        } else {
                                            default_hour = originalTimePicker.getCurrentHour();
                                            default_minute = originalTimePicker.getCurrentMinute();
                                        }

                                        // 保存する形式に変換
                                        default_time = timeHandler.time_to_number(default_hour, default_minute) + 30000;

                                        // 表示する形式に変換
                                        time_defaultSt = timeHandler.timeString(default_hour, default_minute);

                                        // サマリーに表示
                                        defaultSt[3] = time_defaultSt + "(自分で指定)";
                                        button.setSummary(defaultSt[defaultWhich]);
                                        // 結果を代入
                                        if (state) {
                                            default_gu = default_time;
                                        } else {
                                            default_gtb = default_time;
                                        }

                                        dialog.dismiss();
                                    }
                                }
                        );
                        builder.setNegativeButton(
                                "キャンセル",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }
                        );
                        builder.show();
                        break;
                }
                alertDialog3.dismiss();

                // 自分で指定以外
                if (defaultWhich < 3) {
                    // サマリーに表示
                    button.setSummary(defaultSt[defaultWhich]);
                    // 結果を代入
                    if (state) {
                        default_gu = default_time;
                    } else {
                        default_gtb = default_time;
                    }
                }
            }
        };
        builder.setTitle("押し忘れ入力時の初期表示時刻(" + dialogTitle + ")").setSingleChoiceItems(defaultSt, defaultWhich, onClickListener);
        alertDialog3 = builder.show();
        return default_time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        timeHandler = (TimeHandler) this.getApplication();

        SharedPreferences sp = PrefActivity.this.getSharedPreferences("pref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        // 表示範囲を取得
        int resultsNum = sp.getInt("results", 0);
        timeHandler.setResultsNum(resultsNum);

        // 表示範囲の選択肢
        final String[] resultsSt = {"すべて表示", "過去1週間", "過去2週間", "過去3週間", "過去4週間", "指定日～今日", "指定日1～指定日2"};
        setChoices(timeHandler.getResultsNum());
        // setChoices(resultsNum);

        // 表示範囲の指定日をDBから取得
        Cursor cursor = db.query("RangeTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        cursor.moveToFirst();
        timeHandler.setSpec_year(cursor.getInt(1));
        timeHandler.setSpec_month(cursor.getInt(2));
        timeHandler.setSpec_date(cursor.getInt(3));
        cursor.moveToNext();
        timeHandler.setSpec_year1(cursor.getInt(1));
        timeHandler.setSpec_month1(cursor.getInt(2));
        timeHandler.setSpec_date1(cursor.getInt(3));
        cursor.moveToNext();
        timeHandler.setSpec_year2(cursor.getInt(1));
        timeHandler.setSpec_month2(cursor.getInt(2));
        timeHandler.setSpec_date2(cursor.getInt(3));
        cursor.close();

        // 表示範囲ボタン
        resultsBtn = (PreferenceScreen) findPreference("results");
        if (resultsWhich < 5) {
            resultsBtn.setSummary("記録の表示範囲: " + resultsSt[resultsWhich]);
        } else {
            if (resultsWhich == 5) {
                spec_St = timeHandler.dateString(timeHandler.getSpec_year(), timeHandler.getSpec_month() + 1, timeHandler.getSpec_date()) + "～今日";
            } else if (resultsWhich == 6) {
                spec_St = timeHandler.dateString(timeHandler.getSpec_year1(), timeHandler.getSpec_month1() + 1, timeHandler.getSpec_date1()) + "～" + timeHandler.dateString(timeHandler.getSpec_year2(), timeHandler.getSpec_month2() + 1, timeHandler.getSpec_date2());
            }
            resultsBtn.setSummary("記録の表示範囲: " + spec_St);
        }
        resultsBtn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        setRange(PrefActivity.this, 0, timeHandler);

//                        AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
//                        DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                resultsWhich = i;
//                                switch (resultsWhich) {
//                                    case 0:
//                                        resultsNum = 0;
//                                        break;
//                                    case 1:
//                                        resultsNum = 7;
//                                        break;
//                                    case 2:
//                                        resultsNum = 14;
//                                        break;
//                                    case 3:
//                                        resultsNum = 21;
//                                        break;
//                                    case 4:
//                                        resultsNum = 28;
//                                        break;
//                                    case 5:
//                                        // 指定日～今日のカスタムデイトピッカーダイアログ
//                                        CustomDatePickerDialog datePickerDialog;
//                                        CustomDatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
//                                            @Override
//                                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//
//                                            }
//                                        };
//                                        if (spec_year == 0 && spec_month == 0 && spec_date == 0) {
//                                            Calendar cal_now = Calendar.getInstance();
//                                            cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(PrefActivity.this));
//                                            spec_year = cal_now.get(Calendar.YEAR);
//                                            spec_month = cal_now.get(Calendar.MONTH);
//                                            spec_date = cal_now.get(Calendar.DAY_OF_MONTH);
//                                        }
//                                        datePickerDialog = new CustomDatePickerDialog(PrefActivity.this, listener, spec_year, spec_month, spec_date);
//
////                                        designateDate(resultsBtn, 0, datePickerDialog);
//
//                                        break;
//                                    case 6:
//                                        // 指定日1のカスタムデイトピッカーダイアログ
//                                        CustomDatePickerDialog1 datePickerDialog1;
//                                        CustomDatePickerDialog1.OnDateSetListener listener1 = new DatePickerDialog.OnDateSetListener() {
//                                            @Override
//                                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//
//                                            }
//                                        };
//                                        if (spec_year1 == 0 && spec_month1 == 0 && spec_date1 == 0) {
//                                            Calendar cal_now = Calendar.getInstance();
//                                            cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(PrefActivity.this));
//                                            spec_year1 = cal_now.get(Calendar.YEAR);
//                                            spec_month1 = cal_now.get(Calendar.MONTH);
//                                            spec_date1 = cal_now.get(Calendar.DAY_OF_MONTH);
//                                        }
//                                        datePickerDialog1 = new CustomDatePickerDialog1(PrefActivity.this, listener1, spec_year1, spec_month1, spec_date1);
//
////                                        designateDate(resultsBtn, 1, datePickerDialog1);
//
//                                        break;
//                                }
//                                if (resultsWhich < 5) {
//                                    resultsBtn.setSummary("記録の表示範囲: " + resultsSt[resultsWhich]);
//                                }
//                                alertDialog.dismiss();
//                            }
//                        };
//                        builder.setTitle("表示範囲").setSingleChoiceItems(resultsSt, resultsWhich, onDialogClickListener);
//                        alertDialog = builder.show();
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

        // 目標就寝時刻を取得
        gtb_target = sp.getInt("go_to_bed_target", 0);
        gtb_target_hour = timeHandler.number_to_time(gtb_target)[0];
        gtb_target_minute = timeHandler.number_to_time(gtb_target)[1];
        gtb_target_St = timeHandler.timeString(gtb_target_hour, gtb_target_minute);

        // 目標就寝時刻ボタン
        final PreferenceScreen gtb_target_btn = (PreferenceScreen) findPreference("go_to_bed_target");
        gtb_target_btn.setSummary(gtb_target_St);
        gtb_target_btn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        CustomTimePickerDialog3 timePickerDialog;
                        CustomTimePickerDialog3.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                            }
                        };
                        timePickerDialog = new CustomTimePickerDialog3(PrefActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, gtb_target_hour, gtb_target_minute, true);

                        targetButton(gtb_target_btn, 3, timePickerDialog);

                        return true;
                    }
                }
        );

        // 目標起床時刻を取得
        gu_target = sp.getInt("get_up_target", 800);
        gu_target_hour = timeHandler.number_to_time(gu_target)[0];
        gu_target_minute = timeHandler.number_to_time(gu_target)[1];
        gu_target_St = timeHandler.timeString(gu_target_hour, gu_target_minute);

        // 目標起床時刻ボタン
        final PreferenceScreen gu_target_btn = (PreferenceScreen) findPreference("get_up_target");
        gu_target_btn.setSummary(gu_target_St);
        gu_target_btn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        CustomTimePickerDialog4 timePickerDialog;
                        CustomTimePickerDialog4.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                            }
                        };
                        timePickerDialog = new CustomTimePickerDialog4(PrefActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, gu_target_hour, gu_target_minute, true);

                        targetButton(gu_target_btn, 4, timePickerDialog);

                        return true;
                    }
                }
        );

        // slp_targetの絶対値を格納するための変数
        int abs_slp_target;
        // 目標睡眠時間を取得
        slp_target = sp.getInt("sleeping_target", 800);
        // 目標睡眠時間ボタン取得
        slp_target_btn = (PreferenceScreen) findPreference("sleeping_target");
        // 就寝・起床の差の場合はマイナスの値、プラスは自分で指定の場合
        if (slp_target < 0) {
            slp_which = 0;
            cal_diff_target = timeHandler.diff_gu_gtb(gu_target, gtb_target);
            slp_target_St = timeHandler.timeString(cal_diff_target.get(Calendar.HOUR_OF_DAY), cal_diff_target.get(Calendar.MINUTE));
            // 絶対値を取り、時と分に分ける
            abs_slp_target = Math.abs(slp_target);
            slp_target_hour = timeHandler.number_to_time(abs_slp_target)[0];
            slp_target_minute = timeHandler.number_to_time(abs_slp_target)[1];
        } else {
            slp_which = 1;
            slp_target_hour = timeHandler.number_to_time(slp_target)[0];
            slp_target_minute = timeHandler.number_to_time(slp_target)[1];
            slp_target_St = timeHandler.timeString(slp_target_hour, slp_target_minute);
        }
        // サマリーに表示
        slp_target_btn.setSummary(slp_target_St);
        // 目標睡眠時間ボタン押下時の処理
        slp_target_btn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // 目標起床時刻と目標就寝時刻の差を計算
                        cal_diff_target = timeHandler.diff_gu_gtb(gu_target, gtb_target);
//                        final Calendar cal_gu_target = Calendar.getInstance();
//                        cal_gu_target.set(0, 0, 0, gu_target_hour, gu_target_minute);
//                        cal_gu_target.add(Calendar.HOUR, 0 - gtb_target_hour);
//                        cal_gu_target.add(Calendar.MINUTE, 0 - gtb_target_minute);
                        final String diff_gu_gtb_St = timeHandler.timeString(cal_diff_target.get(Calendar.HOUR_OF_DAY), cal_diff_target.get(Calendar.MINUTE));

                        // 選択肢として表示するためにStringを再設定
                        slp_target_St = timeHandler.timeString(slp_target_hour, slp_target_minute);

                        // 起床・就寝の差と目標睡眠時間が同じかどうか
//                        if (slp_target_St.equals(diff_gu_gtb_St)) {
//                            slp_which = 0;
//                        } else {
//                            slp_which = 1;
//                        }

                        // アラートダイアログに選択肢として表示するString配列
                        String[] slp_option_St = {diff_gu_gtb_St + "(起床時刻-就寝時刻)", slp_target_St + "(自分で指定)"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
                        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        slp_which = 0;
                                        if (slp_target > 0) {
                                            slp_target = 0 - slp_target;
                                        }
                                        slp_target_btn.setSummary(diff_gu_gtb_St);
                                        break;
                                    case 1:
                                        CustomTimePickerDialog5 timePickerDialog;
                                        CustomTimePickerDialog5.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                                            }
                                        };
                                        timePickerDialog = new CustomTimePickerDialog5(PrefActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, slp_target_hour, slp_target_minute, true);

                                        targetButton(slp_target_btn, 5, timePickerDialog);
                                        break;
                                }
                                alertDialog2.dismiss();
                            }
                        };
                        builder.setTitle("目標睡眠時間").setSingleChoiceItems(slp_option_St, slp_which, onClickListener);
                        alertDialog2 = builder.show();

                        return true;
                    }
                }
        );

        // 押し忘れ入力時のデフォルト時刻
        // 就寝
        default_gtb = sp.getInt("default_gtb", 0);
        // 時刻を抽出
        int hour_default_gtb = timeHandler.number_to_time(default_gtb % 10000)[0];
        int minute_default_gtb = timeHandler.number_to_time(default_gtb % 10000)[1];
        time_defaultSt = timeHandler.timeString(hour_default_gtb, minute_default_gtb);
        // 選択されている項目を抽出
        defaultWhich = (default_gtb - (default_gtb % 10000)) / 10000;
        final PreferenceScreen default_gtb_btn = (PreferenceScreen) findPreference("default_gtb");
        // サマリーに表示
        defaultSt[3] = time_defaultSt + "(自分で指定)";
        default_gtb_btn.setSummary(defaultSt[defaultWhich]);
        default_gtb_btn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        defaultButton(default_gtb_btn, false, default_gtb);

                        return true;
                    }
                }
        );

        // 押し忘れ入力時のデフォルト時刻
        // 起床
        default_gu = sp.getInt("default_gu", 0);
        // 時刻を抽出
        int hour_default_gu = timeHandler.number_to_time(default_gu % 10000)[0];
        int minute_default_gu = timeHandler.number_to_time(default_gu % 10000)[1];
        time_defaultSt = timeHandler.timeString(hour_default_gu, minute_default_gu);
        // 選択されている項目を抽出
        defaultWhich = (default_gu - (default_gu % 10000)) / 10000;
        final PreferenceScreen default_gu_btn = (PreferenceScreen) findPreference("default_gu");
        // サマリーに表示
        defaultSt[3] = time_defaultSt + "(自分で指定)";
        default_gu_btn.setSummary(defaultSt[defaultWhich]);
        default_gu_btn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        defaultButton(default_gu_btn, true, default_gu);

                        return true;
                    }
                }
        );

        // 充電切り替え時のポップアップ画面表示
        display_popup = sp.getBoolean("display_popup", false);
        // 表示or非表示のチェックボックス
        final CheckBoxPreference display_popup_btn = (CheckBoxPreference) findPreference("display_popup");
        // 反転のスイッチ
        final SwitchPreference inversion_btn = (SwitchPreference) findPreference("inversion");
        if (display_popup) {
            display_popup_btn.setChecked(true);
            display_popup_btn.setSummary("充電の状態が変わったとき記録画面を表示する:ON");
            inversion_btn.setEnabled(true);
        } else {
            display_popup_btn.setChecked(false);
            display_popup_btn.setSummary("充電の状態が変わったとき記録画面を表示する:OFF");
            inversion_btn.setEnabled(false);
        }
        // チェックボックス押下時の処理
        display_popup_btn.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if ((boolean)o) {
                            display_popup = true;
                            preference.setSummary("充電の状態が変わったとき記録画面を表示する:ON");
                            inversion_btn.setEnabled(true);
                        } else {
                            display_popup = false;
                            preference.setSummary("充電の状態が変わったとき記録画面を表示する:OFF");
                            inversion_btn.setEnabled(false);
                        }
                        return true;
                    }
                }
        );
        // 反転の状態取得
        inversion = sp.getBoolean("inversion", false);
        if (!inversion) {
            inversion_btn.setChecked(false);
            inversion_btn.setSummary("接続時:就寝、切断時:起床");
        } else {
            inversion_btn.setChecked(true);
            inversion_btn.setSummary("接続時:起床、切断時:就寝");
        }
        // スイッチ押下時の処理
        inversion_btn.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (!(boolean)o) {
                            inversion = false;
                            preference.setSummary("接続時:就寝、切断時:起床");
                        } else {
                            inversion = true;
                            preference.setSummary("接続時:起床、切断時:就寝");
                        }
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
                                        editor.putInt("results", timeHandler.getResultsNum());
                                        // 今日～指定日の指定日
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put("year", timeHandler.getSpec_year());
                                        contentValues.put("month", timeHandler.getSpec_month());
                                        contentValues.put("date", timeHandler.getSpec_date());
                                        db.update("RangeTable", contentValues, "id=" + 0, null);
                                        // 指定日1
                                        ContentValues contentValues1 = new ContentValues();
                                        contentValues1.put("year", timeHandler.getSpec_year1());
                                        contentValues1.put("month", timeHandler.getSpec_month1());
                                        contentValues1.put("date", timeHandler.getSpec_date1());
                                        db.update("RangeTable", contentValues1, "id=" + 1, null);
                                        // 指定日2
                                        ContentValues contentValues2 = new ContentValues();
                                        contentValues2.put("year", timeHandler.getSpec_year2());
                                        contentValues2.put("month", timeHandler.getSpec_month2());
                                        contentValues2.put("date", timeHandler.getSpec_date2());
                                        db.update("RangeTable", contentValues2, "id=" + 2, null);

                                        // 起床→就寝切り替え時刻
                                        editor.putInt("stay_up_line", stay_up_line);
                                        // 就寝→起床切り替え時刻
                                        editor.putInt("sleeping_line", sleeping_line);

                                        // 目標就寝時刻
                                        editor.putInt("go_to_bed_target", gtb_target);
                                        // 目標起床時刻
                                        editor.putInt("get_up_target", gu_target);
                                        // 目標睡眠時間
                                        editor.putInt("sleeping_target", slp_target);

                                        // 押し忘れ入力時のデフォルト時刻
                                        // 就寝
                                        editor.putInt("default_gtb", default_gtb);
                                        // 起床
                                        editor.putInt("default_gu", default_gu);

                                        // 充電切り替え時のポップアップ画面表示有無
                                        editor.putBoolean("display_popup", display_popup);
                                        // ポップアップ画面の就寝・起床反転
                                        editor.putBoolean("inversion", inversion);

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
        int gtb_target_back = sp.getInt("go_to_bed_target", 0);
        int gu_target_back = sp.getInt("get_up_target", 800);
        int slp_target_back = sp.getInt("sleeping_target", 800);
        int default_gtb_back = sp.getInt("default_gtb", 0);
        int default_gu_back = sp.getInt("default_gu", 0);
        boolean display_popup_back = sp.getBoolean("display_popup", false);
        boolean inversion_back = sp.getBoolean("inversion", false);
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
        if (results_back == timeHandler.getResultsNum() && stay_up_back == stay_up_line && sleeping_back == sleeping_line
                && year_back == timeHandler.getSpec_year() && month_back == timeHandler.getSpec_month() && date_back == timeHandler.getSpec_date()
                && year_back1 == timeHandler.getSpec_year1() && month_back1 == timeHandler.getSpec_month1() && date_back1 == timeHandler.getSpec_date1()
                && year_back2 == timeHandler.getSpec_year2() && month_back2 == timeHandler.getSpec_month2() && date_back2 == timeHandler.getSpec_date2()
                && gtb_target_back == gtb_target && gu_target_back == gu_target && slp_target_back == slp_target
                && default_gtb_back == default_gtb && default_gu_back == default_gu
                && display_popup_back == display_popup && inversion_back == inversion) {
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
