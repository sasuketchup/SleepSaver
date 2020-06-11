package com.example.sleepsaver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class EverydayGraphTab extends Fragment {

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
        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");
        if (idCount > 14) {
            idCount = 14;
        }

        // テーブルから取得したデータをセット
        for (int i = 0; i < idCount; i++) {
            int hourGU = cursor1.getInt(1);
            int minuteGU = cursor1.getInt(2);

            int hourGTB = cursor2.getInt(1);
            int minuteGTB = cursor2.getInt(2);

            valuesGU.add(new Entry(i, (hourGU * 60) + minuteGU, null, null));

            valuesGTB.add(new Entry(i, (hourGTB * 60) + minuteGTB, null, null));

            cursor1.moveToPrevious();
            cursor2.moveToPrevious();
        }
        cursor1.close();
        cursor2.close();

        LineDataSet setGU;
        LineDataSet setGTB;

        setGU = new LineDataSet(valuesGU, "起床時刻");
        setGTB = new LineDataSet(valuesGTB, "就寝時刻");

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setGU);
        dataSets.add(setGTB);

        LineData lineData = new LineData(dataSets);

        everydayChart.setData(lineData);

        TextView textView5 = (TextView)view.findViewById(R.id.textView5);
        textView5.setText("!!!");
    }
}
