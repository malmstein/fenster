package com.malmstein.fenster.gestures;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class FensterGestureListener implements GestureDetector.OnGestureListener {

    private static final String DEBUG_TAG = "FensterGestureListener";

    private final FensterEventsListener fensterEventsListener;
    private final ViewConfiguration viewConfiguration;

    public FensterGestureListener(FensterEventsListener touchController, ViewConfiguration viewConfiguration) {
        this.fensterEventsListener = touchController;
        this.viewConfiguration = viewConfiguration;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(DEBUG_TAG, "onDown: " + e.toString());
        fensterEventsListener.onTap();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(DEBUG_TAG, "onShowPress: " + e.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + e.toString());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(DEBUG_TAG, "onScroll: " + e1.toString() + e2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(DEBUG_TAG, "onLongPress: " + e.toString());
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + e1.toString() + e2.toString());
        return true;
    }

}
