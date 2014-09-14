package com.malmstein.fenster.gestures;

import android.view.MotionEvent;

public interface FensterEventsListener {

    void onTap();

    void onHorizontalScroll(int distance, MotionEvent e2);

    void onVerticalScroll(int distance, MotionEvent e2);

    void onSwipeRight();

    void onSwipeLeft();

    void onSwipeBottom();

    void onSwipeTop();
}
