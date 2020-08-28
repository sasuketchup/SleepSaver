package com.example.sleepsaver;

import android.graphics.Canvas;
import android.graphics.Path;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

public class MyChartRenderer extends LineChartRenderer {
    public MyChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    protected void drawLinearFill(Canvas c, LineDataSet dataSet) {
        mRenderPaint.setColor(dataSet.getFillColor());
    }

    private Path generateFilledPath(List<Entry> entries, int from, int to) {
        float phaseY = mAnimator.getPhaseY();

        Path filled = new Path();
        filled.moveTo(entries.get(from).getX(), entries.get(0).getY());
        filled.lineTo(entries.get(from).getX(), entries.get(from).getY() * phaseY);

        for (int x = from + 1; x <= to; x++) {

        }

        return filled;
    }
}
