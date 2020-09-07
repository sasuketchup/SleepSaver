package com.example.sleepsaver;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.util.Calendar;

public class MonthAverageGraphTab extends Fragment {

    TimeHandler timeHandler = new TimeHandler();

    EverydayGraphTab everydayGraphTab = new EverydayGraphTab();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_average_graph,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LineChart monthAveChart = view.findViewById(R.id.average_chart);

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
        XAxis xAxis = monthAveChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // y軸(左)
        YAxis ylAxis = monthAveChart.getAxisLeft();

        // y軸(右)非表示
        monthAveChart.getAxisRight().setEnabled(false);

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

        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");
        Cursor cursor = db.query("DateTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

        cursor.moveToLast();
        cursor1.moveToLast();
        cursor2.moveToLast();
        cursor2.moveToPrevious();

        // 月数を計算するために今日の年月日を取得
        int current_year = cursor.getInt(1);
        int current_month = cursor.getInt(2);
        int current_date = cursor.getInt(3);

        // まずすべてのデータを配列に格納
        int month[] = new int[(int)idCount + 1]; // 月
        int timeGU[] = new int[(int)idCount]; // 起床時刻
        int timeGTB[] = new int[(int)idCount]; // 就寝時刻
        for (int i = 0; i < idCount; i++) {
            month[i] = cursor.getInt(2);
            int hourGU = cursor1.getInt(1);
            int minuteGU = cursor1.getInt(2);

            if (minuteGU == -1) { // データが空の時
                timeGU[i] = 2000; // ありえない値
            } else { // データがあるとき
                timeGU[i] = (hourGU * 60) + minuteGU;
            }

            cursor.moveToPrevious();
            cursor1.moveToPrevious();
        }
        for (int i = 0; i < idCount; i++) {
            int hourGTB;
            int minuteGTB;

            // 最後の就寝記録を空に
            if (i == (int)idCount - 1) {
                hourGTB = -1;
                minuteGTB = -1;
            } else {
                hourGTB = cursor2.getInt(1);
                minuteGTB = cursor2.getInt(2);
            }

            if (minuteGTB == -1) { // データが空の時
                timeGTB[i] = 2000; // ありえない値
            } else { // データがあるとき

                // 結果が0時を過ぎている場合、24を加算
                if (hourGTB < hour_line) {
                    hourGTB = hourGTB + 24;
                }

                timeGTB[i] = (hourGTB * 60) + minuteGTB;
            }

            cursor2.moveToPrevious();
        }

        // 比較の時最後に参照できるように-1を代入
        month[(int)idCount] = -1;

        // 月数を計算するために最古の年月日を取得
        cursor.moveToFirst();
        int oldest_year = cursor.getInt(1);
        int oldest_month = cursor.getInt(2);
        int oldest_date = cursor.getInt(3);

        cursor.close();
        cursor1.close();
        cursor2.close();

        // 月数を計算
        int countMonth = ((current_year - oldest_year) * 12) + (current_month - oldest_month) + 1;
        // 一年(12ヶ月)より多い場合は12に
        if (countMonth > 12) {
            countMonth = 12;
        }

        int j = 0;
        int ave_timeGU[] = new int[countMonth];
        int ave_timeGTB[] = new int[countMonth];
        for (int i = 0; i < countMonth; i++) {
            // 合計とカウントを初期化
            int sumGU = 0;
            int countGU = 0;
            int sumGTB = 0;
            int countGTB = 0;

            do {
                if (timeGU[j] != 2000) {
                    countGU++;
                    sumGU = sumGU + timeGU[j];
                }
                if (timeGTB[j] != 2000) {
                    countGTB++;
                    sumGTB = sumGTB + timeGTB[j];
                }
                j++;
            } while (month[j] == month[j - 1]); // 月が変わらない間くり返す

            // (起床)分母が0でないとき
            if (countGU > 0) {
                ave_timeGU[i] = sumGU / countGU;
            } else { // 分母が0のときは2000(ありえない値)
                ave_timeGU[i] = 2000;
            }
            // (就寝)分母が0でないとき
            if (countGTB > 0) {
                ave_timeGTB[i] = sumGTB / countGTB;
                // 就寝時刻が0時より前の場合-24時間する
                if (ave_timeGTB[i] >= (hour_line * 60)) {
                    ave_timeGTB[i] = ave_timeGTB[i] - 1440;
                }
            } else { // 分母が0のときは2000(ありえない値)
                ave_timeGTB[i] = 2000;
            }
        }

        // グラフに表示
        everydayGraphTab.displayGraph(getContext(), 3, monthAveChart, countMonth, ave_timeGU, ave_timeGTB, xAxis, ylAxis, gu_target_hour, gu_target_minute, gtb_target_hour2, gtb_target_minute);

        // 凡例
        LegendEntry legendGU = new LegendEntry("起床時刻", Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.CYAN);
        LegendEntry legendGTB = new LegendEntry("就寝時刻", Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.MAGENTA);
        LegendEntry legendCGU = new LegendEntry("目標達成(起床)", Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.BLUE);
        LegendEntry legendCGTB = new LegendEntry("目標達成(就寝)", Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.RED);
        LegendEntry legendTL = new LegendEntry("目標起床時刻：" + timeHandler.timeString(gu_target_hour, gu_target_minute) + ",目標就寝時刻：" + timeHandler.timeString(gtb_target_hour, gtb_target_minute), Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.GREEN);
        Legend legend = monthAveChart.getLegend();
        legend.setCustom(new LegendEntry[]{legendGU, legendGTB, legendCGU, legendCGTB, legendTL});
        legend.setWordWrapEnabled(true);
    }
}
