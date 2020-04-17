package com.example.sleepsaver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper {

    public MyOpenHelper(Context context) {
        super(context, "SleepDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // version1
        db.execSQL("create table DateTable(id integer primary key, year integer, month integer, date integer);");
        db.execSQL("create table GetUpTable(id integer primary key, hour integer, minute integer);");
        db.execSQL("create table GoToBedTable(id integer primary key, hour integer, minute integer);");

        // version2
        version2(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // version2
        if (oldVersion <= 1 && newVersion >= 2) {
            version2(db);
        }
    }

    // version2で追加
    private void version2(SQLiteDatabase db) {
        // 表示範囲を指定する日付を保存するRangeTable作成
        db.execSQL("create table RangeTable(id integer primary key, year integer, month integer, date integer);");
        // 指定日～今日の指定日(id = 0)
        db.execSQL("insert into RangeTable(id, year, month, date) values(0, 0, 0, 0);");
        // 指定日～指定日の指定日1(古い方)(id = 1)
        db.execSQL("insert into RangeTable(id, year, month, date) values(1, 0, 0, 0);");
        // 指定日～指定日の指定日2(新しい方)(id = 2)
        db.execSQL("insert into RangeTable(id, year, month, date) values(2, 0, 0, 0);");
    }
}
