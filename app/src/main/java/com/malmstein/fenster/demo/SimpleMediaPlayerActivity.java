package com.malmstein.fenster.demo;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;

import com.malmstein.fenster.controller.FensterPlayerControllerVisibilityListener;
import com.malmstein.fenster.controller.SimpleMediaFensterPlayerController;
import com.malmstein.fenster.view.FensterVideoView;

public class SimpleMediaPlayerActivity extends Activity implements FensterPlayerControllerVisibilityListener {

    public static final String KEY_LOCAL_FILE = BuildConfig.APPLICATION_ID + "KEY_LOCAL_FILE";
    private FensterVideoView textureView;
    private SimpleMediaFensterPlayerController fullScreenMediaPlayerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simple_media_player);

        bindViews();
        initVideo();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getIntent().hasExtra(KEY_LOCAL_FILE)) {
            AssetFileDescriptor assetFileDescriptor = getResources().openRawResourceFd(R.raw.big_buck_bunny);
            textureView.setVideo(assetFileDescriptor);
        } else {
            textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        }

        textureView.start();
    }

    private void bindViews() {
        textureView = (FensterVideoView) findViewById(R.id.fen__play_video_texture);
        fullScreenMediaPlayerController = (SimpleMediaFensterPlayerController) findViewById(R.id.fen__play_video_controller);
    }

    private void initVideo() {
        fullScreenMediaPlayerController.setVisibilityListener(this);
        textureView.setMediaController(fullScreenMediaPlayerController);
        textureView.setOnPlayStateListener(fullScreenMediaPlayerController);
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
                    fullScreenMediaPlayerController.show();
                }
            }
        });
    }

    @Override
    public void onControlsVisibilityChange(final boolean value) {
        setSystemUiVisibility(value);
    }
}
