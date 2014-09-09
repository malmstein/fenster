package com.malmstein.fenster.gestures;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class FensterGestureListener implements GestureDetector.OnGestureListener {

    public static final String TAG = "FensterGestureListener";
    private final FensterEventsListener listener;

    public FensterGestureListener(FensterEventsListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        listener.onTap();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // Touch has been long enough to indicate a long press.
        // Does not indicate motion is complete yet (no up event necessarily)
        Log.i(TAG, "Long Press");
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.i(TAG, "Scroll");
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Fling event occurred.  Notification of this one happens after an "up" event.
        Log.i(TAG, "Fling");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // User performed a down event, and hasn't moved yet.
        Log.i(TAG, "Show Press");
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // "Down" event - User touched the screen.
        Log.i(TAG, "Down");
        return false;
    }

}
