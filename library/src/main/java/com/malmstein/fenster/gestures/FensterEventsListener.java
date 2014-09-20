package com.malmstein.fenster.gestures;

import android.view.MotionEvent;

public interface FensterEventsListener {

    void onTap();

    void onHorizontalScroll(MotionEvent event);

    void onVerticalScroll(MotionEvent event);

    void onSwipeRight();

    void onSwipeLeft();

    void onSwipeBottom();

    void onSwipeTop();
}
