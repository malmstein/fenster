package com.malmstein.fenster.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.malmstein.fenster.PlayerController;
import com.malmstein.fenster.TextureVideoView;

public class DemoActivity extends Activity implements PlayerController.NavigationListener, PlayerController.VisibilityListener {

    private TextureVideoView textureView;
    private PlayerController playerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo);

        bindViews();
        initVideo();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                PlayerController.DEFAULT_VIDEO_START);
        textureView.start();
    }

    private void bindViews() {
        textureView = (TextureVideoView) findViewById(R.id.play_video_texture);
        playerController = (PlayerController) findViewById(R.id.play_video_controller);
    }

    private void initVideo() {
        playerController.setNavigationListener(this);
        playerController.setVisibilityListener(this);
        textureView.setMediaController(playerController);
        textureView.setOnPlayStateListener(playerController);
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
                    playerController.show();
                }
            }
        });
    }

    @Override
    public void onNextSelected() {
        Toast.makeText(this, "Next track selected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPreviousSelected() {
        Toast.makeText(this, "Previous track selected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onControlsVisibilityChange(final boolean value) {
        setSystemUiVisibility(value);
    }
}
