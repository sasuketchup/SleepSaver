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

    // 日付変更時刻を格納する変数(4桁→それぞれ)
    int date_line;
    int hour_line;
    int minute_line;

    String date_line_St;

    AlertDialog alertDialog;

    int resultsWhich;

    // ピッカーの時刻を変更したときに変数に代入するようにするために、TimePickerDialogを継承したクラス
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

        // 日付変更時刻を取得
        date_line = sp.getInt("date_line", 0);
        minute_line = date_line % 100;
        hour_line = (date_line - minute_line) / 100;
        date_line_St = timeHandler.timeString(hour_line, minute_line);

        // 日付変更時刻ボタン
        final PreferenceScreen date_line_btn = (PreferenceScreen) findPreference("date_line");
        date_line_btn.setSummary("日付を切り替える時刻: " + date_line_St + "\n確実に就寝している時刻を指定してください");
        date_line_btn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // タイムピッカーを表示
                        CustomTimePickerDialog timePickerDialog;
                        CustomTimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                            }
                        };
                        timePickerDialog = new CustomTimePickerDialog(PrefActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, hour_line, minute_line, true);
                        timePickerDialog.setTitle("日付変更時刻");
                        timePickerDialog.setButton(
                                DialogInterface.BUTTON_POSITIVE,
                                "設定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        date_line = (hour_line * 100) + minute_line;
                                        date_line_St = timeHandler.timeString(hour_line, minute_line);
                                        date_line_btn.setSummary("日付を切り替える時刻: " + date_line_St + "\n確実に就寝している時刻を指定してください");

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
                                        minute_line = date_line % 100;
                                        hour_line = (date_line - minute_line) / 100;

                                        dialogInterface.dismiss();
                                    }
                                }
                        );
                        timePickerDialog.show();
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

                                        // 日付変更時刻
                                        editor.putInt("date_line", date_line);

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
