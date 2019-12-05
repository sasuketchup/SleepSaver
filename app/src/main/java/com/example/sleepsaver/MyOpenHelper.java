package com.example.sleepsaver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper {

    public MyOpenHelper(Context context) {
        super(context, "SleepDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table GetUpTable(id integer primary key, year integer, month integer, date integer, hour integer, minute integer);");
        db.execSQL("create table GoToBedTable(id integer primary key, year integer, month integer, date integer, hour integer, minute integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
