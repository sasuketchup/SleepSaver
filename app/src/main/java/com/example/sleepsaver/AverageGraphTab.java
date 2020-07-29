package com.example.sleepsaver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.util.Calendar;

public class AverageGraphTab extends Fragment {

    TimeHandler timeHandler = new TimeHandler();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_average_graph, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BarChart averageChart = view.findViewById(R.id.average_chart);

        TextView periodText = view.findViewById(R.id.ave_period);
        periodText.setText("準備中...");

        MyOpenHelper helper = new MyOpenHelper(getContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        SharedPreferences sp = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        // 起床→就寝切り替え時刻を取得
        int stay_up_line = sp.getInt("stay_up_line", 1200);
        int hour_line = timeHandler.number_to_time(stay_up_line)[0];
        // 目標を取得
        int gtb_target = sp.getInt("go_to_bed_target", 0);
        int gu_target = sp.getInt("get_up_target", 800);
        // 目標起床時刻
        int gu_target_hour = timeHandler.number_to_time(gu_target)[0];
        int gu_target_minute = timeHandler.number_to_time(gu_target)[1];
        // 目標就寝時刻
        int gtb_target_hour = timeHandler.number_to_time(gtb_target)[0];
        int gtb_target_minute = timeHandler.number_to_time(gtb_target)[1];

        // x軸
        XAxis xAxis = averageChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // y軸(左)
        YAxis ylAxis = averageChart.getAxisLeft();

        // y軸(右)非表示
        averageChart.getAxisRight().setEnabled(false);

        // 目標時刻のラインを表示
        int gtb_target_hour2 = gtb_target_hour;
        if (gtb_target_hour >= hour_line) {
            gtb_target_hour2 = gtb_target_hour - 24; // 目標就寝時刻が0時より前の場合-24する
        }
        LimitLine gu_targetLine = new LimitLine((gu_target_hour * 60) + gu_target_minute);
        LimitLine gtb_targetLine = new LimitLine((gtb_target_hour2 * 60) + gtb_target_minute);
        gu_targetLine.setLineColor(Color.GREEN);
        gtb_targetLine.setLineColor(Color.GREEN);
        gu_targetLine.setLineWidth(1);
        gtb_targetLine.setLineWidth(1);
        ylAxis.addLimitLine(gu_targetLine);
        ylAxis.addLimitLine(gtb_targetLine);

        // 今日の曜日を取得する
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(getContext()));
        int current_week = calendar.get(Calendar.DAY_OF_WEEK);

        Cursor cursor = db.query("DateTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);

        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

        cursor1.moveToLast();
        cursor2.moveToLast();
        cursor2.moveToPrevious();

        // データの個数から週の数を計算して配列の長さに
        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");
        // そもそもデータの個数がcurrent_weekに満たない場合
        if (idCount < current_week) {
            current_week = (int) idCount;
        }
        // 配列の長さ(つまり週の数)を決める変数
        int num_of_weeks;
        // idCountからcurrent_weekを引いた値が7で割り切れない(つまり最も古い週のデータがちょうど7つでない)場合
        if ((((int)idCount - current_week) % 7) != 0) {
            num_of_weeks = (((int)idCount - current_week) / 7) + 2; // 今週と最も古い週の分を足す
        } else { // 割り切れる(つまり最も古い週のデータがちょうど7つの)場合
            num_of_weeks = (((int)idCount - current_week) / 7) + 1; // 今週の分を足す(データの個数がcurrent_weekに満たない場合もこれに含まれる)
        }
        // 配列の長さが12(グラフへの最大表示個数)より多い場合は12にする
        if (num_of_weeks > 12) {
            num_of_weeks = 12;
        }
        // 週ごとの平均
        int ave_timeGU[] = new int[num_of_weeks];
        int ave_timeGTB[] = new int[num_of_weeks];
        // 平均を算出するための合計と個数
        // 起床
        int sumGU[] = new int[num_of_weeks];
        int countGU[] = new int[num_of_weeks];
        // 就寝
        int sumGTB[] = new int[num_of_weeks];
        int countGTB[] = new int[num_of_weeks];

        for (int i = 0; i < current_week; i++) {
            int hourGU = cursor1.getInt(1);
            int minuteGU = cursor1.getInt(2);

            int hourGTB;
            int minuteGTB;
            // データがcurrent_weekちょうどかそれ以下の場合は最後の就寝記録を空(-1)にする
            if (idCount <= current_week  && i == (current_week - 1)) {
                hourGTB = -1;
                minuteGTB = -1;
            } else {
                hourGTB = cursor2.getInt(1);
                minuteGTB = cursor2.getInt(2);
            }

            // (起床) 記録がある場合
            if (hourGU != -1) {
                countGU[0]++;
                sumGU[0] = sumGU[0] + (hourGU * 60) + minuteGU;
            }
            // (就寝) 記録がある場合
            if (hourGTB != -1) {
                countGTB[0]++;

                // 結果が0時を過ぎている場合、24を加算
                if (hourGTB < hour_line) {
                    hourGTB = hourGTB + 24;
                }

                sumGTB[0] =sumGTB[0] + (hourGTB * 60) + minuteGTB;
            }

            cursor1.moveToPrevious();
            cursor2.moveToPrevious();
        }

        // (起床)分母が0でないとき
        if (countGU[0] > 0) {
            ave_timeGU[0] = sumGU[0] / countGU[0];
        } else { // 分母が0のときは2000(ありえない値)
            ave_timeGU[0] = 2000;
        }
        // (就寝)分母が0でないとき
        if (countGTB[0] > 0) {
            ave_timeGTB[0] = sumGTB[0] / countGTB[0];
            // 就寝時刻が0時より前の場合-24時間する
            if (ave_timeGTB[0] >= (hour_line * 60)) {
                ave_timeGTB[0] = ave_timeGTB[0] - 1440;
            }
        } else { // 分母が0のときは2000(ありえない値)
            ave_timeGTB[0] = 2000;
        }
    }
}
