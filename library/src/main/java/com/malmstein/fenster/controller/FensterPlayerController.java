package com.malmstein.fenster.controller;

import android.view.View;

public interface FensterPlayerController {

    void setMediaPlayer(FensterPlayer fensterPlayer);

    void setEnabled(boolean value);

    void setAnchorView(View view);

    void show(int timeInMilliSeconds);

    void show();

    void hide();

    void setVisibilityListener(FensterPlayerControllerVisibilityListener visibilityListener);

}
