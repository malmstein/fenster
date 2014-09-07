package com.malmstein.fenster.controller;

import android.view.View;

public interface VideoController {

    void setMediaPlayer(Player player);

    void setEnabled(boolean value);

    void setAnchorView(View view);

    void show(int timeInMilliSeconds);

    void show();

    void hide();

    void setVisibilityListener(ControllerVisibilityListener visibilityListener);

}
