package com.malmstein.fenster.demo;

import android.app.Activity;
import android.os.Bundle;

import com.malmstein.fenster.controller.SimpleMediaFensterPlayerController;
import com.malmstein.fenster.view.FensterVideoView;

public class ScaleMediaPlayerActivity extends Activity {

    private FensterVideoView textureView;
    private SimpleMediaFensterPlayerController fullScreenMediaPlayerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scale_media_player);

        bindViews();
        initVideo();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        textureView.start();
    }

    private void bindViews() {
        textureView = (FensterVideoView) findViewById(R.id.fen__play_video_texture);
        fullScreenMediaPlayerController = (SimpleMediaFensterPlayerController) findViewById(R.id.fen__play_video_controller);
    }

    private void initVideo() {
        textureView.setMediaController(fullScreenMediaPlayerController);
        textureView.setOnPlayStateListener(fullScreenMediaPlayerController);
    }

}
