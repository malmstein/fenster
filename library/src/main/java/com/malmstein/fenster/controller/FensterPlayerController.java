package com.malmstein.fenster.controller;

public interface FensterPlayerController {

    void setMediaPlayer(FensterPlayer fensterPlayer);

    void setEnabled(boolean value);

    void show(int timeInMilliSeconds);

    void show();

    void hide();

    void setVisibilityListener(FensterPlayerControllerVisibilityListener visibilityListener);

}
