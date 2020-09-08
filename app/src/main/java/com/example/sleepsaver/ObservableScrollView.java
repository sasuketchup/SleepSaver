package com.example.sleepsaver;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {
    public interface ScrollViewListener {
        void onScrollChanged(int x, int y, int oldX, int oldY);
    }
    private ScrollViewListener scrollViewListener = null;
    public ObservableScrollView(Context context) {
        super(context);
    }
    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setOnScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        scrollViewListener.onScrollChanged(x, y, oldX, oldY);
    }
}
