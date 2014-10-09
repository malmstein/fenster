package com.malmstein.fenster.seekbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;

import com.malmstein.fenster.helper.BrightnessHelper;

public class BrightnessSeekBar extends SeekBar {

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

    public BrightnessSeekBar(Context context) {
        super(context);
    }

    public BrightnessSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrightnessSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(BrightnessSeekBar.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(final AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(BrightnessSeekBar.class.getName());
    }

    public void initialise(Listener brightnessListener) {
        this.setMax(MAX_BRIGHTNESS);
        this.setOnSeekBarChangeListener(brightnessSeekListener);
        this.brightnessListener = brightnessListener;
    }

    public void setBrightness(int brightness) {
        if (brightness < MIN_BRIGHTNESS) {
            brightness = MIN_BRIGHTNESS;
        } else if (brightness > MAX_BRIGHTNESS) {
            brightness = MAX_BRIGHTNESS;
        }

        BrightnessHelper.setBrightness(getContext(), brightness);
    }

    public void manuallyUpdate(int update) {
        brightnessSeekListener.onProgressChanged(this, update, true);
    }

    public interface Listener {
        void onBrigthnessStartedDragging();

        void onBrightnessFinishedDragging();
    }
}
