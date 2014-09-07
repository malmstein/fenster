package com.malmstein.fenster.play;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malmstein.fenster.R;
import com.malmstein.fenster.controller.FensterPlayerControllerVisibilityListener;
import com.malmstein.fenster.controller.FensterPlayerController;
import com.malmstein.fenster.controller.FensterVideoStateListener;
import com.malmstein.fenster.controller.FullScreenMediaFensterPlayerController;
import com.malmstein.fenster.view.LoadingView;
import com.malmstein.fenster.view.TextureVideoView;

public class FensterVideoFragment extends Fragment implements FensterVideoStateListener {

    private View contentView;
    private TextureVideoView textureView;
    private FensterPlayerController fensterPlayerController;
    private LoadingView loadingView;

    public FensterVideoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_video_texture, container);
        textureView = (TextureVideoView) contentView.findViewById(R.id.play_video_texture);
        fensterPlayerController = (FensterPlayerController) contentView.findViewById(R.id.play_video_controller);
        loadingView = (LoadingView) contentView.findViewById(R.id.play_video_loading);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVideo();
    }

    private void initVideo() {
        textureView.setMediaController(fensterPlayerController);
        textureView.setOnPlayStateListener(this);
    }

    public void playExampleVideo() {
        textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                FullScreenMediaFensterPlayerController.DEFAULT_VIDEO_START);
        textureView.start();
    }

    public void initVisibilityListener(FensterPlayerControllerVisibilityListener visibilityListener) {
        fensterPlayerController.setVisibilityListener(visibilityListener);
    }

    private void hideLoadingView() {
        fensterPlayerController.hide();
        loadingView.showLoading();
    }

    private void showLoadingView(){
        loadingView.hideLoading();
    }

    @Override
    public void onFirstVideoFrameRendered() {
        fensterPlayerController.show();
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
