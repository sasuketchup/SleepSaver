package com.example.sleepsaver;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DataActivity extends AppCompatActivity {

    MainActivity mainActivity = new MainActivity();
    TimeHandler timeHandler = new TimeHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        SharedPreferences sp = getSharedPreferences("pref", MODE_PRIVATE);
        // 期間を取得
        int results = sp.getInt("results", 0);

        // 起床→就寝切り替え時刻を取得
        int stay_up_line = sp.getInt("stay_up_line", 1200);
        int hour_line = timeHandler.number_to_time(stay_up_line)[0];

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        // データの行数を取得
        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");

        // 指定日1～2の時、期間を決めるための変数
        int diff_now_spec1 = (int) (idCount - 1);
        int diff_now_spec2 = 0;

        // 指定日1～指定日2(resultsが-2)の場合
        if (results == -2) {
            // 今日と指定日1、2の差日数を計算
            diff_now_spec1 = mainActivity.spec12_today(db, DataActivity.this)[0];
            diff_now_spec2 = mainActivity.spec12_today(db, DataActivity.this)[1];
        }

        // 指定日～今日(resultsが-1)の場合に指定日と今日の差分を計算
        if (results == -1) {
            // 指定日と今日の差を計算し、表示件数に代入
            results = mainActivity.spec_today(db, DataActivity.this);
        }

        // 対象の期間を計算
        if (results > 0) {
            if (idCount > results) {
                idCount = results;
            }
        }

        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

        int sumGU = 0;
        int countGU = 0;
        int sumGTB = 0;
        int countGTB = 0;

        cursor1.moveToLast();
        cursor2.moveToLast();
        for (int i=0;i<idCount;i++) {
            int hourGU = cursor1.getInt(1);
            int minuteGU = cursor1.getInt(2);
            int hourGTB = cursor2.getInt(1);
            int minuteGTB = cursor2.getInt(2);

            // (起床)記録がある場合
            if (hourGU != -1) {
                countGU++;
                // 時を分に換算
                int convert_minutesGU = (hourGU * 60) + minuteGU;
                // 合計に加算
                sumGU = sumGU + convert_minutesGU;
            }
            // (就寝)記録がある場合
            if (hourGTB != -1) {
                countGTB++;
                // 0時を過ぎている場合、24を加算
                if (hourGTB < hour_line) {
                    hourGTB = hourGTB + 24;
                }
                // 時を分に換算
                int convert_minutesGTB = (hourGTB * 60) + minuteGTB;
                // 合計に加算
                sumGTB = sumGTB + convert_minutesGTB;
            }

            cursor1.moveToPrevious();
            cursor2.moveToPrevious();
        }
        cursor1.close();
        cursor2.close();

        // 平均値の計算(少数切り捨て)
        int averageGU = sumGU / countGU;
        int averageGTB = sumGTB / countGTB;
        // 時と分に分ける
        int ave_minuteGU = averageGU % 60;
        int ave_hourGU = (averageGU - ave_minuteGU) / 60;
        int ave_minuteGTB = averageGTB % 60;
        int ave_hourGTB = (averageGTB - ave_minuteGTB) / 60;
        // 24時以上の場合24を引いて戻す
        if (ave_hourGU >= 24) {
            ave_hourGU = ave_hourGU - 24;
        }
        if (ave_hourGTB >= 24) {
            ave_hourGTB = ave_hourGTB - 24;
        }
        // 表示形式に整理
        String aveGU_St = timeHandler.timeString(ave_hourGU, ave_minuteGU);
        String aveGTB_St = timeHandler.timeString(ave_hourGTB, ave_minuteGTB);
        // TextViewに表示
        TextView aveGU_text = findViewById(R.id.ave_GU);
        TextView aveGTB_text = findViewById(R.id.ave_GTB);
        aveGU_text.setText(aveGU_St);
        aveGTB_text.setText(aveGTB_St);

    }
}
