package com.malmstein.fenster.gestures;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class FensterGestureControllerView extends View {

    private GestureDetector gestureDetector;
    private FensterGestureListener gestureListener;

    public FensterGestureControllerView(Context context) {
        super(context);
    }

    public FensterGestureControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FensterGestureControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFensterEventsListener(final FensterEventsListener fensterEventsListener) {
        gestureListener = new FensterGestureListener(fensterEventsListener, ViewConfiguration.get(getContext()));
        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
