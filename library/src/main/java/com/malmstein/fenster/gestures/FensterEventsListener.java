package com.malmstein.fenster.gestures;

import android.view.MotionEvent;

public interface FensterEventsListener {

    void onTap();

    void onHorizontalScroll(MotionEvent event, float delta);

    void onVerticalScroll(MotionEvent event, float delta);

    void onSwipeRight();

    void onSwipeLeft();

    void onSwipeBottom();

    void onSwipeTop();

}
