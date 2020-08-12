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
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        XAxis xAxis = everydayChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // y軸(左)
        YAxis ylAxis = everydayChart.getAxisLeft();

        // y軸(右)非表示
        everydayChart.getAxisRight().setEnabled(false);

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

        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

        cursor1.moveToLast();
        cursor2.moveToLast();
        cursor2.moveToPrevious();

//        // データの値を格納するArrayList
//        ArrayList<Entry> valuesGU = new ArrayList<>();
//        ArrayList<Entry> valuesGTB = new ArrayList<>();
//
//        // 点の色を格納するList
//        List<Integer> circleColorGU = new ArrayList<>();
//        List<Integer> circleColorGTB = new ArrayList<>();
//
//        // 値テキストの色を格納するList
//        List<Integer> textColorGU = new ArrayList<>();
//        List<Integer> textColorGTB = new ArrayList<>();

        // データの行数を取得し表示する期間と比較
        long idCount2 = DatabaseUtils.queryNumEntries(db, "DateTable");
        idCount = idCount2;
        if (idCount2 > 14) {
            idCount = 14;
        }

        // テーブルからデータを取得
        int timeGU[] = new int[(int) idCount];
        int timeGTB[] = new int[(int) idCount];
        for (int i = 0; i < idCount; i++) {
            int hourGU = cursor1.getInt(1);
            int minuteGU = cursor1.getInt(2);

            int hourGTB;
            int minuteGTB;
            // データが2週間ちょうどかそれ以下の場合は最後の就寝記録を空(-1)にする
            if (idCount2 <= 14 && i == (idCount - 1)) {
                hourGTB = -1;
                minuteGTB = -1;
            } else {
                hourGTB = cursor2.getInt(1);
                minuteGTB = cursor2.getInt(2);
            }

            // 就寝時刻が0時より前の場合-24する
            if (hourGTB >= hour_line) {
                hourGTB = hourGTB - 24;
            }

            if (minuteGU == -1) { // データが空の時
                timeGU[i] = 2000; // ありえない値
            } else { // データがあるとき
                timeGU[i] = (hourGU * 60) + minuteGU;
            }
            if (minuteGTB == -1) { // データが空の時
                timeGTB[i] = 2000; // ありえない値
            } else { // データがあるとき
                timeGTB[i] = (hourGTB * 60) + minuteGTB;
            }

            cursor1.moveToPrevious();
            cursor2.moveToPrevious();
        }
        cursor1.close();
        cursor2.close();

        // グラフに表示
        displayGraph(getContext(), 1, everydayChart, (int)idCount, timeGU, timeGTB, xAxis, ylAxis, gu_target_hour, gu_target_minute, gtb_target_hour2, gtb_target_minute);

//        // テーブルから取得したデータを古い方からセット
//        for (int i = 0; i < idCount; i++) {
//            if (timeGU[(int) idCount - i - 1] != 2000) { // 空(2000)でないとき
//                valuesGU.add(new Entry(i, timeGU[(int) idCount - i - 1], null, null));
//                if (timeGU[(int) idCount - i - 1] <= ((gu_target_hour * 60) + gu_target_minute)) {
//                    // 目標達成
//                    circleColorGU.add(Color.BLUE);
//                    textColorGU.add(Color.BLUE);
//                } else {
//                    // それ以外
//                    circleColorGU.add(Color.CYAN);
//                    textColorGU.add(Color.BLACK);
//                }
//            }
//            if (timeGTB[(int) idCount - i - 1] != 2000) { // 空(2000)でないとき
//                valuesGTB.add(new Entry(i, timeGTB[(int) idCount - i - 1], null, null));
//                if (timeGTB[(int) idCount - i - 1] <= ((gtb_target_hour2 * 60) + gtb_target_minute)) {
//                    // 目標達成
//                    circleColorGTB.add(Color.RED);
//                    textColorGTB.add(Color.RED);
//                } else {
//                    // それ以外
//                    circleColorGTB.add(Color.MAGENTA);
//                    textColorGTB.add(Color.BLACK);
//                }
//            }
//        }
//
//        LineDataSet setGU;
//        LineDataSet setGTB;
//
//        setGU = new LineDataSet(valuesGU, "起床時刻");
//        setGTB = new LineDataSet(valuesGTB, "就寝時刻");

        // 凡例
        LegendEntry legendGU = new LegendEntry("起床時刻", Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.CYAN);
        LegendEntry legendGTB = new LegendEntry("就寝時刻", Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.MAGENTA);
        LegendEntry legendST = new LegendEntry("睡眠時間", Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.argb(130, 0, 0, 255));
        LegendEntry legendTL = new LegendEntry("目標起床時刻：" + timeHandler.timeString(gu_target_hour, gu_target_minute) + ",目標就寝時刻：" + timeHandler.timeString(gtb_target_hour, gtb_target_minute), Legend.LegendForm.DEFAULT, 10f, 2f, null, Color.GREEN);
        Legend legend = everydayChart.getLegend();
        legend.setCustom(new LegendEntry[]{legendGU, legendGTB, legendST, legendTL});
        legend.setWordWrapEnabled(true);

//        // 就寝時刻ラインの色
//        setGTB.setColor(Color.MAGENTA);
//
//        // 点の色
//        setGU.setCircleColors(circleColorGU);
//        setGTB.setCircleColors(circleColorGTB);
//        // 点の塗りつぶし
//        setGU.setDrawCircleHole(false);
//        setGTB.setDrawCircleHole(false);
//
//        // 値テキストの色
//        setGU.setValueTextColors(textColorGU);
//        setGTB.setValueTextColors(textColorGTB);
//        // 値のテキストサイズ
//        setGU.setValueTextSize(9);
//        setGTB.setValueTextSize(9);
//
//        // 塗りつぶし
//        setGU.setDrawFilled(true);
//        setGTB.setDrawFilled(true);
//        setGU.setFillColor(Color.BLUE);
//        setGTB.setFillColor(Color.WHITE);
//        setGTB.setFillAlpha(130);

//        // (起床)プロットされる値を時刻に
//        setGU.setValueFormatter(new IValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                return timeHandler.minutes_to_timeString((int)value);
//            }
//        });
//        // (就寝)プロットされる値を時刻に
//        setGTB.setValueFormatter(new IValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                // 値がマイナスの時24時間足す
//                if (value < 0) {
//                    value = value + 1440;
//                }
//                return timeHandler.minutes_to_timeString((int)value);
//            }
//        });

//        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
//        dataSets.add(setGU);
//        dataSets.add(setGTB);
//
//        LineData lineData = new LineData(dataSets);

//        // データが空でないときのみ
//        if (!valuesGU.isEmpty() && !valuesGTB.isEmpty()) {
//            // グラフにデータをセット
//            everydayChart.setData(lineData);
//            // x軸のラベルを日付に
//            xAxis.setValueFormatter(new IAxisValueFormatter() {
//                @Override
//                public String getFormattedValue(float value, AxisBase axis) {
//                    String dateLabel = "";
//                    if ((value % 1) == 0) {
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(getContext()));
//                        calendar.add(Calendar.DAY_OF_MONTH, (int) value - ((int) idCount - 1));
//                        int month = calendar.get(Calendar.MONTH) + 1;
//                        int date = calendar.get(Calendar.DAY_OF_MONTH);
//                        dateLabel = month + "/" + date;
//                    }
//                    return dateLabel;
//                }
//            });
//
//            // y軸のラベルを時刻に
//            ylAxis.setValueFormatter(new IAxisValueFormatter() {
//                @Override
//                public String getFormattedValue(float value, AxisBase axis) {
//                    // 値がマイナスの時24時間足す
//                    if (value < 0) {
//                        value = value + 1440;
//                    }
//                    return timeHandler.minutes_to_timeString((int) value);
//                }
//            });
//        }

//        TextView textView5 = (TextView)view.findViewById(R.id.textView5);
//        textView5.setText("");
    }

    // データをグラフに表示するメソッド
    public void displayGraph(final Context context, final int graphType, LineChart lineChart, final int num_of_data, int[] timeGU, int[] timeGTB, XAxis xAxis, YAxis ylAxis, int gu_target_hour, int gu_target_minute, int gtb_target_hour2, int gtb_target_minute) {
        // データの値を格納するArrayList
        ArrayList<Entry> valuesGU = new ArrayList<>();
        ArrayList<Entry> valuesGTB = new ArrayList<>();

        // 点の色を格納するList
        List<Integer> circleColorGU = new ArrayList<>();
        List<Integer> circleColorGTB = new ArrayList<>();

        // 値テキストの色を格納するList
        List<Integer> textColorGU = new ArrayList<>();
        List<Integer> textColorGTB = new ArrayList<>();

        // テーブルから取得したデータを古い方からセット
        for (int i = 0; i < num_of_data; i++) {
            if (timeGU[num_of_data - i - 1] != 2000) { // 空(2000)でないとき
                valuesGU.add(new Entry(i, timeGU[num_of_data - i - 1], null, null));
                if (timeGU[num_of_data - i - 1] <= ((gu_target_hour * 60) + gu_target_minute)) {
                    // 目標達成
                    circleColorGU.add(Color.BLUE);
                    textColorGU.add(Color.BLUE);
                } else {
                    // それ以外
                    circleColorGU.add(Color.CYAN);
                    textColorGU.add(Color.BLACK);
                }
            }
            if (timeGTB[num_of_data - i - 1] != 2000) { // 空(2000)でないとき
                valuesGTB.add(new Entry(i, timeGTB[num_of_data - i - 1], null, null));
                if (timeGTB[num_of_data - i - 1] <= ((gtb_target_hour2 * 60) + gtb_target_minute)) {
                    // 目標達成
                    circleColorGTB.add(Color.RED);
                    textColorGTB.add(Color.RED);
                } else {
                    // それ以外
                    circleColorGTB.add(Color.MAGENTA);
                    textColorGTB.add(Color.BLACK);
                }
            }
        }

        LineDataSet setGU;
        LineDataSet setGTB;

        setGU = new LineDataSet(valuesGU, "起床時刻");
        setGTB = new LineDataSet(valuesGTB, "就寝時刻");

        // 就寝時刻ラインの色
        setGTB.setColor(Color.MAGENTA);

        // 点の色
        setGU.setCircleColors(circleColorGU);
        setGTB.setCircleColors(circleColorGTB);
        // 点の塗りつぶし
        setGU.setDrawCircleHole(false);
        setGTB.setDrawCircleHole(false);

        // 値テキストの色
        setGU.setValueTextColors(textColorGU);
        setGTB.setValueTextColors(textColorGTB);
        // 値のテキストサイズ
        setGU.setValueTextSize(9);
        setGTB.setValueTextSize(9);

        // 直近2週間のグラフのみ塗りつぶし
        if (graphType == 1) {
            setGU.setDrawFilled(true);
            setGTB.setDrawFilled(true);
            setGU.setFillColor(Color.BLUE);
            setGTB.setFillColor(Color.WHITE);
            setGTB.setFillAlpha(130);
        } else {
            setGU.setDrawFilled(false);
            setGTB.setDrawFilled(false);
        }

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
                // 値がマイナスの時24時間足す
                if (value < 0) {
                    value = value + 1440;
                }
                return timeHandler.minutes_to_timeString((int)value);
            }
        });

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setGU);
        dataSets.add(setGTB);

        LineData lineData = new LineData(dataSets);

        // データが空でないときのみ
        if (!valuesGU.isEmpty() && !valuesGTB.isEmpty()) {
            // グラフにデータをセット
            lineChart.setData(lineData);
            // x軸のラベル
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    String xLabel = "";
                    if ((value % 1) == 0) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context));
                        if (graphType == 1) { // 直近2週間のグラフは日付に
                            calendar.add(Calendar.DAY_OF_MONTH, (int) value - (num_of_data - 1));
                            int month = calendar.get(Calendar.MONTH) + 1;
                            int date = calendar.get(Calendar.DAY_OF_MONTH);
                            xLabel = month + "/" + date;
                        } else if (graphType == 2) { // 週毎の平均グラフは何週前かに
                            xLabel = (num_of_data - 1) - (int)value + "週前";
                            if ((num_of_data - 1) - (int)value == 0) {
                                xLabel = "今週";
                            }
                        } else if (graphType == 3) { // 月毎の平均のグラフは月に
                            calendar.add(Calendar.MONTH, (int) value - (num_of_data - 1));
                            int month = calendar.get(Calendar.MONTH) + 1;
                            xLabel = month + "月";
                        }
                    }
                    return xLabel;
                }
            });

            // y軸のラベルを時刻に
            ylAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    // 値がマイナスの時24時間足す
                    if (value < 0) {
                        value = value + 1440;
                    }
                    return timeHandler.minutes_to_timeString((int) value);
                }
            });
        }
    }
}
