package com.malmstein.fenster.controller;

public interface FensterVideoStateListener {

    void onFirstVideoFrameRendered();

    void onPlay();

    void onBuffer();

    boolean onStopWithExternalError(int position);

}
