package com.malmstein.fenster.seekbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;

public class VolumeSeekBar extends SeekBar {

    public final OnSeekBarChangeListener volumeSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int vol, boolean fromUser) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            volumeListener.onVolumeStartedDragging();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            volumeListener.onVolumeFinishedDragging();
        }
    };

    private AudioManager audioManager;
    private Listener volumeListener;
    private BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateVolumeProgress();
        }
    };

    public VolumeSeekBar(Context context) {
        super(context);
    }

    public VolumeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VolumeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(VolumeSeekBar.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(final AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(VolumeSeekBar.class.getName());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerVolumeReceiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        unregisterVolumeReceiver();
        super.onDetachedFromWindow();
    }

    public void initialise(final Listener volumeListener) {
        this.audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        this.volumeListener = volumeListener;

        this.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        this.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        this.setOnSeekBarChangeListener(volumeSeekListener);
    }

    private void updateVolumeProgress() {
        this.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    private void registerVolumeReceiver() {
        getContext().registerReceiver(volumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
    }

    private void unregisterVolumeReceiver() {
        getContext().unregisterReceiver(volumeReceiver);
    }

    public void manuallyUpdateVolume(int update) {
        volumeSeekListener.onProgressChanged(this, update, true);
    }

    public interface Listener {
        void onVolumeStartedDragging();

        void onVolumeFinishedDragging();
    }

}
