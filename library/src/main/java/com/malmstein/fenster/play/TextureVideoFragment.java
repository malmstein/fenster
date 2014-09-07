package com.malmstein.fenster.play;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malmstein.fenster.R;
import com.malmstein.fenster.controller.ControllerVisibilityListener;
import com.malmstein.fenster.controller.FullScreenMediaPlayerController;
import com.malmstein.fenster.controller.VideoController;
import com.malmstein.fenster.controller.VideoStateListener;
import com.malmstein.fenster.view.LoadingView;
import com.malmstein.fenster.view.TextureVideoView;

public class TextureVideoFragment extends Fragment implements VideoStateListener {

    private View contentView;
    private TextureVideoView textureView;
    private VideoController playerController;
    private LoadingView loadingView;

    public TextureVideoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_video_texture, container);
        textureView = (TextureVideoView) contentView.findViewById(R.id.play_video_texture);
        playerController = (VideoController) contentView.findViewById(R.id.play_video_controller);
        loadingView = (LoadingView) contentView.findViewById(R.id.play_video_loading);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVideo();
    }

    private void initVideo() {
        textureView.setMediaController(playerController);
        textureView.setOnPlayStateListener(this);
    }

    public void playExampleVideo() {
        textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                FullScreenMediaPlayerController.DEFAULT_VIDEO_START);
        textureView.start();
    }

    public void initVisibilityListener(ControllerVisibilityListener visibilityListener) {
        playerController.setVisibilityListener(visibilityListener);
    }

    private void hideLoadingView() {
        playerController.hide();
        loadingView.showLoading();
    }

    private void showLoadingView(){
        loadingView.hideLoading();
    }

    @Override
    public void onFirstVideoFrameRendered() {
        playerController.show();
    }

    @Override
    public void onPlay() {
        hideLoadingView();
    }

    @Override
    public void onBuffer() {
        showLoadingView();
    }

    @Override
    public boolean onStopWithExternalError(int position) {
        return false;
    }
}
