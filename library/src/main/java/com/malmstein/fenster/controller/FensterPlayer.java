package com.malmstein.fenster.controller;

public interface FensterPlayer {
    void start();

    void pause();

    int getDuration();

    /**
     * @return current playback position in milliseconds.
     */
    int getCurrentPosition();

    void seekTo(int pos);

    boolean isPlaying();

    int getBufferPercentage();

    boolean canPause();

    boolean canSeekBackward();

    boolean canSeekForward();

    /**
     * Get the audio session id for the player used by this VideoView. This can be used to
     * apply audio effects to the audio track of a video.
     *
     * @return The audio session, or 0 if there was an error.
     */
    int getAudioSessionId();

}