package com.example.sleepsaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
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

import androidx.fragment.app.FragmentActivity;

public class PrefActivity extends PreferenceActivity {

    int resultsNum;

    AlertDialog alertDialog;

    int which;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        SharedPreferences sp = PrefActivity.this.getSharedPreferences("pref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        // 表示件数を取得
        resultsNum = sp.getInt("results", 0);
        switch (resultsNum) {
            case 5:
                which = 0;
                break;
            case 10:
                which = 1;
                break;
            case 0:
                which = 2;
                break;
        }

        // 表示件数
        PreferenceScreen resultsBtn = (PreferenceScreen) findPreference("results");
        resultsBtn.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PrefActivity.this);
                        String[] resultsSt = {"5", "10", "すべて表示"};
                        DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                which = i;
                                switch (which) {
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
                                alertDialog.dismiss();
                            }
                        };
                        builder.setTitle("表示件数").setSingleChoiceItems(resultsSt, which, onDialogClickListener);
                        alertDialog = builder.show();
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

                                        editor.commit();

                                        Intent intent = new Intent(PrefActivity.this, MainActivity.class);
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
