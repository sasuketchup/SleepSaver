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
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

public class MonthAverageGraphTab extends Fragment {

    TimeHandler timeHandler = new TimeHandler();

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

        // まずすべてのデータを配列に格納
        int month[] = new int[(int)idCount]; // 月
        int timeGU[] = new int[(int)idCount]; // 起床時刻
        int timeGTB[] = new int[(int)idCount - 1]; // 就寝時刻
        for (int i = 0; i <idCount; i++) {
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
        for (int i = 0; i < idCount - 1; i++) {
            int hourGTB = cursor2.getInt(1);
            int minuteGTB = cursor2.getInt(2);

            // 結果が0時を過ぎている場合、24を加算
            if (hourGTB < hour_line) {
                hourGTB = hourGTB + 24;
            }

            if (minuteGTB == -1) { // データが空の時
                timeGTB[i] = 2000; // ありえない値
            } else { // データがあるとき
                timeGTB[i] = (hourGTB * 60) + minuteGTB;
            }

            cursor2.moveToPrevious();
        }

        cursor.close();
        cursor1.close();
        cursor2.close();

        // 合計とカウントを初期化
        int sumGU = 0;
        int countGU = 0;
        int sumGTB = 0;
        int countGTB = 0;

        int i = 0;
        while (month[i] == month[i - 1]) {

            i++;
        }
    }
}
