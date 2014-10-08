package com.malmstein.fenster.seekbar;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;

public class BrigthnessSeekBar extends SeekBar {

    public static final int MAX_BRIGHTNESS = 255;
    public static final int MIN_BRIGHTNESS = 0;
    public final OnSeekBarChangeListener brightnessSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int brightness, boolean fromUser) {
            setBrightness(brightness);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            brightnessListener.onBrigthnessStartedDragging();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            brightnessListener.onBrightnessFinishedDragging();
        }
    };
    private Listener brightnessListener;

    public BrigthnessSeekBar(Context context) {
        super(context);
    }

    public BrigthnessSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrigthnessSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(BrigthnessSeekBar.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(final AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(BrigthnessSeekBar.class.getName());
    }

    public void initialize(Listener brightnessListener) {
        this.setMax(MAX_BRIGHTNESS);
        this.setOnSeekBarChangeListener(brightnessSeekListener);
        this.brightnessListener = brightnessListener;
    }

    public void setBrightness(int brightness) {

        //constrain the value of brightness
        if (brightness < MIN_BRIGHTNESS) {
            brightness = MIN_BRIGHTNESS;
        } else if (brightness > MAX_BRIGHTNESS) {
            brightness = MAX_BRIGHTNESS;
        }

        ContentResolver cResolver = getContext().getContentResolver();
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);

    }

    public interface Listener {
        void onBrigthnessStartedDragging();

        void onBrightnessFinishedDragging();
    }
}
