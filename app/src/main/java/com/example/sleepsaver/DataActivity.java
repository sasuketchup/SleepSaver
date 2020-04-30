package com.example.sleepsaver;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

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

        // 各種目標を取得
        int gtb_target = sp.getInt("go_to_bed_target", 0);
        int gu_target = sp.getInt("get_up_target", 800);
        int slp_target = sp.getInt("sleeping_target", 800);
        // 目標就寝時刻
        int gtb_target_hour = timeHandler.number_to_time(gtb_target)[0];
        int gtb_target_minute = timeHandler.number_to_time(gtb_target)[1];
        String gtb_target_St = timeHandler.timeString(gtb_target_hour, gtb_target_minute);
        // 目標起床時刻
        int gu_target_hour = timeHandler.number_to_time(gu_target)[0];
        int gu_target_minute = timeHandler.number_to_time(gu_target)[1];
        String gu_target_St = timeHandler.timeString(gu_target_hour, gu_target_minute);
        // 目標睡眠時間
        String slp_target_St;
        if (slp_target < 0) {
            Calendar cal_diff_target = timeHandler.diff_gu_gtb(gu_target, gtb_target);
            slp_target_St = timeHandler.timeString(cal_diff_target.get(Calendar.HOUR_OF_DAY), cal_diff_target.get(Calendar.MINUTE));
        } else {
            int slp_target_hour = timeHandler.number_to_time(slp_target)[0];
            int slp_target_minute = timeHandler.number_to_time(slp_target)[1];
            slp_target_St = timeHandler.timeString(slp_target_hour, slp_target_minute);
        }
        // 目標をTextViewに表示
        TextView targetGU_text = findViewById(R.id.target_GU);
        TextView targetGTB_text = findViewById(R.id.target_GTB);
        TextView targetST_text = findViewById(R.id.target_ST);
        targetGU_text.setText("(" + gu_target_St + ")");
        targetGTB_text.setText("(" + gtb_target_St + ")");
        targetST_text.setText("(" + slp_target_St + ")");

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

        // 平均を算出するための合計と個数
        // 起床
        int sumGU = 0;
        int countGU = 0;
        // 就寝
        int sumGTB = 0;
        int countGTB = 0;
        // 睡眠時間
        int sumST = 0;
        int countST = 0;

        cursor1.moveToLast();
        cursor2.moveToLast();
        for (int i=0;i<idCount;i++) {
            int hourGU = cursor1.getInt(1);
            int minuteGU = cursor1.getInt(2);
            int hourGTB = cursor2.getInt(1);
            int minuteGTB = cursor2.getInt(2);

            if (diff_now_spec2 <= i && i <= diff_now_spec1) {
                // (起床) 記録がある場合
                if (hourGU != -1) {
                    countGU++;
                    // 時を分に換算
                    int convert_minutesGU = (hourGU * 60) + minuteGU;
                    // 合計に加算
                    sumGU = sumGU + convert_minutesGU;
                }
                // (就寝) 記録がある場合
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
            }

            cursor1.moveToPrevious();
            cursor2.moveToPrevious();

            if (i < idCount - 1 && diff_now_spec2 <= i && i <= (diff_now_spec1 - 1)) {
                // 睡眠時間計算のため、一つ前の就寝時刻を取得
                int hourGTBPrevious = cursor2.getInt(1);
                int minuteGTBPrevious = cursor2.getInt(2);

                // 起床・就寝の値が揃っているとき
                if (hourGTBPrevious != -1 && hourGU != -1) {
                    countST++;

                    Calendar calST = Calendar.getInstance();
                    calST.set(0, 0, 0, hourGU, minuteGU);
                    calST.add(Calendar.HOUR, 0 - hourGTBPrevious);
                    calST.add(Calendar.MINUTE, 0 - minuteGTBPrevious);

                    int hourST = calST.get(Calendar.HOUR_OF_DAY);
                    int minuteST = calST.get(Calendar.MINUTE);

                    // 時間を分に換算
                    int convert_minutesST = (hourST * 60) + minuteST;
                    // 合計に加算
                    sumST = sumST + convert_minutesST;
                }
            }
        }
        cursor1.close();
        cursor2.close();

        // 平均値の計算(少数切り捨て)
        int averageGU = sumGU / countGU;
        int averageGTB = sumGTB / countGTB;
        int averageST = sumST / countST;
        // 時と分に分ける
        int ave_minuteGU = averageGU % 60;
        int ave_hourGU = (averageGU - ave_minuteGU) / 60;
        int ave_minuteGTB = averageGTB % 60;
        int ave_hourGTB = (averageGTB - ave_minuteGTB) / 60;
        int ave_minuteST = averageST % 60;
        int ave_hourST = (averageST - ave_minuteST) / 60;
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
        String aveST_St = timeHandler.timeString(ave_hourST, ave_minuteST);
        // TextViewに表示
        TextView aveGU_text = findViewById(R.id.ave_GU);
        TextView aveGTB_text = findViewById(R.id.ave_GTB);
        TextView aveST_text = findViewById(R.id.ave_ST);
        aveGU_text.setText(aveGU_St);
        aveGTB_text.setText(aveGTB_St);
        aveST_text.setText(aveST_St);

    }
}
