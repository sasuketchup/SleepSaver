package com.example.sleepsaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
                                editor.putInt("results", resultsNum);
                                editor.commit();

                                alertDialog.dismiss();
                            }
                        };
                        builder.setTitle("表示件数").setSingleChoiceItems(resultsSt, which, onDialogClickListener);
                        alertDialog = builder.show();
                        return true;
                    }
                }
        );
    }

//    public static class PrefFragment extends PreferenceFragment {
//
//        int resultsNum;
//
//        AlertDialog alertDialog;
//
//        int which;
//
//        SharedPreferences sp = PrefFragment.this.getActivity().getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
//
//        SharedPreferences.Editor editor = sp.edit();
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.pref);
//
//            PreferenceScreen resultsBtn = (PreferenceScreen) findPreference("results");
//            resultsBtn.setOnPreferenceClickListener(
//                    new Preference.OnPreferenceClickListener() {
//                        @Override
//                        public boolean onPreferenceClick(Preference preference) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                            String[] resultsSt = {"5", "10", "すべて表示"};
//                            DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    which = i;
//                                    switch (which) {
//                                        case 0:
//                                            resultsNum = 5;
//                                            break;
//                                        case 1:
//                                            resultsNum = 10;
//                                            break;
//                                        case 2:
//                                            resultsNum = 0;
//                                            break;
//                                    }
//                                    editor.putInt("results", resultsNum);
//                                    editor.commit();
//
//                                    alertDialog.dismiss();
//                                }
//                            };
//                            builder.setTitle("表示件数").setSingleChoiceItems(resultsSt, which, onDialogClickListener);
//                            alertDialog = builder.show();
//                            return true;
//                        }
//                    }
//            );
//        }
//    }
}