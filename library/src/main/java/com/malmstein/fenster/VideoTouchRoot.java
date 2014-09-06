package com.malmstein.fenster;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.malmstein.fenster.gestures.FensterEventsListener;
import com.malmstein.fenster.gestures.FensterGestureListener;

/**
 * A custom layout we put as a layout root to get notified about any screen touches.
 */
public final class VideoTouchRoot extends FrameLayout {

    public static final int MIN_INTERCEPTION_TIME = 1000;

    private FensterEventsListener fensterEventsListener;

    private GestureDetector gestureDetector;
    private FensterGestureListener gestureListener;

    public VideoTouchRoot(final Context context) {
        super(context);
    }

    public VideoTouchRoot(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTouchRoot(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getActionMasked();

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                return false;
            case (MotionEvent.ACTION_MOVE):
                return true;
            case (MotionEvent.ACTION_UP):
                return false;
            case (MotionEvent.ACTION_CANCEL):
                return false;
            case (MotionEvent.ACTION_OUTSIDE):
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void setFensterEventsListener(final FensterEventsListener receiver) {
        this.fensterEventsListener = receiver;
        gestureListener = new FensterGestureListener(fensterEventsListener, ViewConfiguration.get(getContext()));
        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }
}