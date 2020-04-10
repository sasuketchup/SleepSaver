package com.example.sleepsaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.TimePicker;

import androidx.fragment.app.FragmentActivity;

public class PrefActivity extends PreferenceActivity {

    TimeHandler timeHandler = new TimeHandler();

    // 表示件数を格納する変数
    int resultsNum;

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
                "設定",
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

        // 表示件数の選択肢
        final String[] resultsSt = {"5件", "10件", "すべて表示"};
        // 表示件数を取得
        resultsNum = sp.getInt("results", 0);
        switch (resultsNum) {
            case 5:
                resultsWhich = 0;
                break;
            case 10:
                resultsWhich = 1;
                break;
            case 0:
                resultsWhich = 2;
                break;
        }

        // 表示件数ボタン
        final PreferenceScreen resultsBtn = (PreferenceScreen) findPreference("results");
        resultsBtn.setSummary("記録の表示件数: " + resultsSt[resultsWhich]);
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
                                        resultsNum = 5;
                                        break;
                                    case 1:
                                        resultsNum = 10;
                                        break;
                                    case 2:
                                        resultsNum = 0;
                                        break;
                                }
                                resultsBtn.setSummary("記録の表示件数: " + resultsSt[resultsWhich]);
                                alertDialog.dismiss();
                            }
                        };
                        builder.setTitle("表示件数").setSingleChoiceItems(resultsSt, resultsWhich, onDialogClickListener);
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

//                        timePickerDialog.setTitle("起床→就寝切り替え時刻");
//                        timePickerDialog.setButton(
//                                DialogInterface.BUTTON_POSITIVE,
//                                "設定",
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        stay_up_line = time_to_number(hour_line, minute_line);
//                                        stay_up_St = timeHandler.timeString(hour_line, minute_line);
//                                        stay_up_btn.setSummary(stay_up_St + "\n確実に起きている時刻を指定してください");
//
//                                        dialogInterface.dismiss();
//                                    }
//                                }
//                        );
//                        timePickerDialog.setButton(
//                                DialogInterface.BUTTON_NEGATIVE,
//                                "キャンセル",
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        // 値をリセット
//                                        hour_line = number_to_time(stay_up_line)[0];
//                                        minute_line = number_to_time(stay_up_line)[1];
//
//                                        dialogInterface.dismiss();
//                                    }
//                                }
//                        );
//                        timePickerDialog.show();
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
                                        // 表示件数
                                        editor.putInt("results", resultsNum);

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
}
