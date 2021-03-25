package com.example.sleepsaver;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

public class MyChartRenderer extends LineChartRenderer {

    public MyChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    //This method is same as it's parent implemntation
    @Override
    protected void drawLinearFill(Canvas c, ILineDataSet dataSet, Transformer trans, XBounds bounds) {
        final Path filled = mGenerateFilledPathBuffer;

        final int startingIndex = bounds.min;
        final int endingIndex = bounds.range + bounds.min;
        final int indexInterval = 128;

        int currentStartIndex = 0;
        int currentEndIndex = indexInterval;
        int iterations = 0;

        // Doing this iteratively in order to avoid OutOfMemory errors that can happen on large bounds sets.
        do {
            currentStartIndex = startingIndex + (iterations * indexInterval);
            currentEndIndex = currentStartIndex + indexInterval;
            currentEndIndex = currentEndIndex > endingIndex ? endingIndex : currentEndIndex;

            if (currentStartIndex <= currentEndIndex) {
                generateFilledPath(dataSet, currentStartIndex, currentEndIndex, filled);

                trans.pathValueToPixel(filled);

                final Drawable drawable = dataSet.getFillDrawable();
                if (drawable != null) {

                    drawFilledPath(c, filled, drawable);
                } else {

                    drawFilledPath(c, filled, Color.BLUE, 100);
                }
            }

            iterations++;

        } while (currentStartIndex <= currentEndIndex);
    }

    //This is where we define the area to be filled.
    private void generateFilledPath(final ILineDataSet dataSet, final int startIndex, final int endIndex, final Path outputPath) {

        //Call the custom method to retrieve the dataset for other line
        final List<Entry> boundaryEntry = ((MyFillFormatter)dataSet.getFillFormatter()).getFillLineBoundary();

        final float phaseY = mAnimator.getPhaseY();
        final Path filled = outputPath;
        filled.reset();

        final Entry entry = dataSet.getEntryForIndex(startIndex);

        filled.moveTo(entry.getX(), boundaryEntry.get(0).getY());
        filled.lineTo(entry.getX(), entry.getY() * phaseY);

        // create a new path
        Entry currentEntry = null;
        Entry previousEntry = null;
        for (int x = startIndex + 1; x <= endIndex; x++) {

            currentEntry = dataSet.getEntryForIndex(x);
            filled.lineTo(currentEntry.getX(), currentEntry.getY() * phaseY);

        }

        // close up
        if (currentEntry != null && previousEntry!= null) {
            filled.lineTo(currentEntry.getX(), previousEntry.getY());
        }

        //Draw the path towards the other line
        for (int x = endIndex ; x > startIndex; x--) {
//            if (boundaryEntry.get(x) != null) {
                previousEntry = boundaryEntry.get(x);
//            }
            filled.lineTo(previousEntry.getX(), previousEntry.getY() * phaseY);
        }

        filled.close();
    }
}

//public class MyChartRenderer extends LineChartRenderer {
//    public MyChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
//        super(chart, animator, viewPortHandler);
//    }
//
//    @Override
//    protected void drawLinearFill(Canvas c, LineDataSet dataSet, Transformer trans, XBounds bounds) {
//        mRenderPaint.setColor(dataSet.getFillColor());
//    }
//
//    private Path generateFilledPath(List<Entry> entries, int from, int to) {
//        float phaseY = mAnimator.getPhaseY();
//
//        Path filled = new Path();
//        filled.moveTo(entries.get(from).getX(), entries.get(0).getY());
//        filled.lineTo(entries.get(from).getX(), entries.get(from).getY() * phaseY);
//
//        for (int x = from + 1; x <= to; x++) {
//
//        }
//
//        return filled;
//    }
//}
