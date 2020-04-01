package com.example.sleepsaver;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PrefActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pref);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content,new PrefFragment())
                .commit();
    }

    public static class PrefFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);
        }
    }
}
