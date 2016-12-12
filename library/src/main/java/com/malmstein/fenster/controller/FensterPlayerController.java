package com.malmstein.fenster.controller;

import com.malmstein.fenster.play.FensterPlayer;

public interface FensterPlayerController {

    void setMediaPlayer(FensterPlayer fensterPlayer);

    void setEnabled(boolean value);

    void show(int timeInMilliSeconds);

    void show();

    void hide();

    void setVisibilityListener(FensterPlayerControllerVisibilityListener visibilityListener);

    boolean isShowing();

    boolean isFirstTimeLoading();

}
