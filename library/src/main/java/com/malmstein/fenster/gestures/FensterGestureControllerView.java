package com.malmstein.fenster.gestures;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class FensterGestureControllerView extends View implements FensterEventsListener {

    private GestureDetector gestureDetector;
    private FensterEventsListener listener;

    public FensterGestureControllerView(Context context) {
        super(context);
    }

    public FensterGestureControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FensterGestureControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mayNotifyGestureDetector(event);
        return true;
    }

    private void mayNotifyGestureDetector(MotionEvent event){
        gestureDetector.onTouchEvent(event);
    }

    public void setFensterEventsListener(FensterEventsListener listener){
        gestureDetector = new GestureDetector(getContext(), new FensterGestureListener(listener));
    }

    @Override
    public void onTap() {

    }
}
