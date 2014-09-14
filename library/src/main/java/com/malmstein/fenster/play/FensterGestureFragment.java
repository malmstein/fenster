package com.malmstein.fenster.play;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.malmstein.fenster.R;
import com.malmstein.fenster.gestures.FensterEventsListener;
import com.malmstein.fenster.gestures.FensterGestureControllerView;

public class FensterGestureFragment extends Fragment implements FensterEventsListener {

    private View contentView;
    private FensterGestureControllerView gestureControllerView;
    private SeekBar horizontalProgress;
    private SeekBar verticalProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_fenster_gesture, container);
        gestureControllerView = (FensterGestureControllerView) contentView.findViewById(R.id.play_gesture_controller);
        gestureControllerView.setFensterEventsListener(this);
        horizontalProgress = (SeekBar) contentView.findViewById(R.id.play_gesture_horizontal_seekbar);
        verticalProgress = (SeekBar) contentView.findViewById(R.id.play_gesture_vertical_seekbar);
        return contentView;
    }

    @Override
    public void onTap() {

    }

    @Override
    public void onHorizontalScroll(int distance, MotionEvent e2) {
        trackTouchEvent(e2, horizontalProgress);
    }

    @Override
    public void onVerticalScroll(int distance, MotionEvent e2) {
        trackTouchEvent(e2, verticalProgress);
    }

    @Override
    public void onSwipeRight() {

    }

    @Override
    public void onSwipeLeft() {

    }

    @Override
    public void onSwipeBottom() {

    }

    @Override
    public void onSwipeTop() {

    }

    private void trackTouchEvent(MotionEvent event, SeekBar seekbar) {
        final int width = seekbar.getWidth();
        final int mPaddingRight = seekbar.getPaddingRight();
        final int mPaddingLeft = seekbar.getPaddingLeft();
        final int available = width - mPaddingLeft - mPaddingRight;
        int x = (int) event.getX();
        float scale;
        float progress = 0;

        if (x < mPaddingLeft) {
            scale = 0.0f;
        } else if (x > width - mPaddingRight) {
            scale = 1.0f;
        } else {
            scale = (float) (x - mPaddingLeft) / (float) available;
            progress = 0;
        }

        final int max = seekbar.getMax();
        progress += scale * max;

        seekbar.setProgress((int) progress);
    }

}
