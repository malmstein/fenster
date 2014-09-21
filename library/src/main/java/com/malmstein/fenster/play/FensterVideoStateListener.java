package com.malmstein.fenster.play;

public interface FensterVideoStateListener {

    void onFirstVideoFrameRendered();

    void onPlay();

    void onBuffer();

    boolean onStopWithExternalError(int position);

}
