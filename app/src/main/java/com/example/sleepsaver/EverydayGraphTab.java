package com.example.sleepsaver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Calendar;

public class EverydayGraphTab extends Fragment {

    TimeHandler timeHandler = new TimeHandler();

    long idCount;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_everyday_graph,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LineChart everydayChart = view.findViewById(R.id.everyday_chart);

        MyOpenHelper helper = new MyOpenHelper(getContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        SharedPreferences sp = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
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
        XAxis xAxis = everydayChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // y軸(左)
        YAxis ylAxis = everydayChart.getAxisLeft();

        // 目標時刻のラインを表示
        LimitLine gu_targetLine = new LimitLine((gu_target_hour * 60) + gu_target_minute, "目標起床時刻" + timeHandler.timeString(gu_target_hour, gu_target_minute));
        LimitLine gtb_targetLine = new LimitLine((gtb_target_hour * 60) + gtb_target_minute, "目標就寝時刻" + timeHandler.timeString(gtb_target_hour, gtb_target_minute));
        ylAxis.addLimitLine(gu_targetLine);
        ylAxis.addLimitLine(gtb_targetLine);

        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

        cursor1.moveToLast();
        cursor2.moveToLast();
        cursor2.moveToPrevious();

//        int data[] = {116, 111, 112, 121, 102, 83,
//                99, 101, 74, 105, 120, 112,
//                109, 102, 107, 93, 82, 99, 110,
//        };

        ArrayList<Entry> valuesGU = new ArrayList<>();
        ArrayList<Entry> valuesGTB = new ArrayList<>();

        // データの行数を取得し表示する期間と比較
        idCount = DatabaseUtils.queryNumEntries(db, "DateTable");
        if (idCount > 14) {
            idCount = 14;
        }

        // テーブルからデータを取得
        int timeGU[] = new int[(int) idCount];
        int timeGTB[] = new int[(int) idCount];
        for (int i = 0; i < idCount; i++) {
            int hourGU = cursor1.getInt(1);
            int minuteGU = cursor1.getInt(2);

            int hourGTB = cursor2.getInt(1);
            int minuteGTB = cursor2.getInt(2);

            timeGU[i] = (hourGU * 60) + minuteGU;
            timeGTB[i] = (hourGTB * 60) + minuteGTB;

            cursor1.moveToPrevious();
            cursor2.moveToPrevious();
        }
        cursor1.close();
        cursor2.close();

        // テーブルから取得したデータを古い方からセット
        for (int i = 0; i < idCount; i++) {
            valuesGU.add(new Entry(i, timeGU[(int) (idCount) - i - 1], null, null));
            valuesGTB.add(new Entry(i, timeGTB[(int) idCount - i - 1], null, null));
        }

        LineDataSet setGU;
        LineDataSet setGTB;

        setGU = new LineDataSet(valuesGU, "起床時刻");
        setGTB = new LineDataSet(valuesGTB, "就寝時刻");

        // (起床)プロットされる値を時刻に
        setGU.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return timeHandler.minutes_to_timeString((int)value);
            }
        });
        // (就寝)プロットされる値を時刻に
        setGTB.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return timeHandler.minutes_to_timeString((int)value);
            }
        });

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setGU);
        dataSets.add(setGTB);

        LineData lineData = new LineData(dataSets);

        everydayChart.setData(lineData);

        // x軸のラベルを日付に
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String dateLabel = "";
                if ((value % 1) == 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, (int)value - ((int)idCount - 1));
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int date = calendar.get(Calendar.DAY_OF_MONTH);
                    dateLabel = month + "/" + date;
                }
                return dateLabel;
            }
        });

        // y軸のラベルを時刻に
//        String labels[] = new String[ylAxis.getLabelCount()];
//        for (int i = 0; i < ylAxis.getLabelCount(); i++) {
//            String labelGet = ylAxis.getFormattedLabel(i);
//            int labelGetInt = Integer.parseInt(labelGet.replaceAll("[^0-9]", ""));
//            int labelMinute = labelGetInt % 60;
//            int labelHour = (labelGetInt - labelMinute) / 60;
//            labels[i] = timeHandler.timeString(labelHour, labelMinute);
//        }
//        ylAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        ylAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return timeHandler.minutes_to_timeString((int)value);
            }
        });

        TextView textView5 = (TextView)view.findViewById(R.id.textView5);
        textView5.setText("");
    }
}
