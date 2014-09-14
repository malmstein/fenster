package com.malmstein.fenster.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.malmstein.fenster.controller.FensterPlayerControllerVisibilityListener;
import com.malmstein.fenster.play.FensterVideoFragment;

public class GestureMediaPlayerActivity extends Activity implements FensterPlayerControllerVisibilityListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_test);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        initVideo();
    }

    private void initVideo(){
        findVideoFragment().setVisibilityListener(this);
        findVideoFragment().playExampleVideo();
    }

    private FensterVideoFragment findVideoFragment(){
        return (FensterVideoFragment) getFragmentManager().findFragmentById(R.id.play_demo_fragment);
    }

    @Override
    public void onControlsVisibilityChange(boolean value) {
        setSystemUiVisibility(value);
    }

    private void setSystemUiVisibility(final boolean visible) {
        int newVis = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        if (!visible) {
            newVis |= View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(newVis);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(final int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) == 0) {
                    findVideoFragment().showFensterController();
                }
            }
        });
    }
}
