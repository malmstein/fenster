package com.malmstein.fenster.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.malmstein.fenster.R;
import com.malmstein.fenster.play.FensterPlayer;
import com.malmstein.fenster.play.FensterVideoStateListener;
import com.malmstein.fenster.view.FensterTouchRoot;

import java.util.Formatter;
import java.util.Locale;

/**
 * Controller to manage syncing the ui models with the UI Controls and MediaPlayer.
 * <p/>
 * Note that the ui models have a narrow scope (i.e. chapter list, piece navigation),
 * their interaction is orchestrated by this controller.ø
 * <p/>
 * It's actually a view currently, as is the android MediaController.
 * (which is a bit odd and should be subject to change.)
 */
public final class SimpleMediaFensterPlayerController extends FrameLayout implements FensterPlayerController, FensterVideoStateListener, FensterTouchRoot.OnTouchReceiver {

    public static final String TAG = "PlayerController";
    public static final int DEFAULT_VIDEO_START = 0;
    private static final int DEFAULT_TIMEOUT = 5000;

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private FensterPlayerControllerVisibilityListener visibilityListener;
    private FensterPlayer mFensterPlayer;
    private boolean mShowing;
    private boolean mDragging;

    private boolean mLoading;
    private boolean mFirstTimeLoading = true;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private View bottomControlsRoot;
    private View controlsRoot;
    private ProgressBar mProgress;
    private TextView mEndTime;
    private TextView mCurrentTime;

    private ImageButton mPauseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private ProgressBar loadingView;
    private int lastPlayedSeconds = -1;

    public SimpleMediaFensterPlayerController(final Context context) {
        this(context, null);
    }

    public SimpleMediaFensterPlayerController(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleMediaFensterPlayerController(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.fen__view_simple_media_controller, this);
        initControllerView();
    }

    @Override
    public void setMediaPlayer(final FensterPlayer fensterPlayer) {
        mFensterPlayer = fensterPlayer;
        updatePausePlay();
    }

    @Override
    public void setVisibilityListener(final FensterPlayerControllerVisibilityListener visibilityListener) {
        this.visibilityListener = visibilityListener;
    }

    private void initControllerView() {
        mPauseButton = (ImageButton) findViewById(R.id.fen__media_controller_pause);
        mPauseButton.requestFocus();
        mPauseButton.setOnClickListener(mPauseListener);

        mNextButton = (ImageButton) findViewById(R.id.fen__media_controller_next);
        mPrevButton = (ImageButton) findViewById(R.id.fen__media_controller_previous);

        mProgress = (SeekBar) findViewById(R.id.fen__media_controller_progress);
        SeekBar seeker = (SeekBar) mProgress;
        seeker.setOnSeekBarChangeListener(mSeekListener);
        mProgress.setMax(1000);

        mEndTime = (TextView) findViewById(R.id.fen__media_controller_time);
        mCurrentTime = (TextView) findViewById(R.id.fen__media_controller_time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        FensterTouchRoot touchRoot = (FensterTouchRoot) findViewById(R.id.media_controller_touch_root);
        touchRoot.setOnTouchReceiver(this);

        bottomControlsRoot = findViewById(R.id.fen__media_controller_bottom_area);
        bottomControlsRoot.setVisibility(View.INVISIBLE);

        controlsRoot = findViewById(R.id.media_controller_controls_root);
        controlsRoot.setVisibility(View.INVISIBLE);

        loadingView = (ProgressBar) findViewById(R.id.fen__media_controller_loading_view);
    }


    /**
     * Show the controller on screen. It will go away
     * automatically after the default time of inactivity.
     */
    @Override
    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeInMilliSeconds The timeout in milliseconds. Use 0 to show
     *                           the controller until hide() is called.
     */
    @Override
    public void show(final int timeInMilliSeconds) {
        if (!mShowing) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            mShowing = true;
            setVisibility(View.VISIBLE);
        }

        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeInMilliSeconds != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeInMilliSeconds);
        }

        if (visibilityListener != null) {
            visibilityListener.onControlsVisibilityChange(true);
        }

    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public boolean isFirstTimeLoading() {
        return mFirstTimeLoading;
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {

        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                setVisibility(View.INVISIBLE);
            } catch (final IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
        if (visibilityListener != null) {
            visibilityListener.onControlsVisibilityChange(false);
        }
    }

    /**
     * hides the next/prev buttons
     */
    public void disableNavigationButtons() {
        mNextButton.setVisibility(View.GONE);
        mPrevButton.setVisibility(View.GONE);
    }

    /**
     *  set a custom color for the progress view
     *  works for android >= 21 only
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setSeekbarColor(ColorStateList color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        SeekBar seekbar = (SeekBar) mProgress;
        seekbar.setThumbTintList(color);
        seekbar.setProgressTintList(color);
    }

    private String stringForTime(final int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mFensterPlayer == null || mDragging) {
            return 0;
        }
        int position = mFensterPlayer.getCurrentPosition();
        int duration = mFensterPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mFensterPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null) {
            mEndTime.setText(stringForTime(duration));
        }
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime(position));
        }
        final int playedSeconds = position / 1000;
        if (lastPlayedSeconds != playedSeconds) {
            lastPlayedSeconds = playedSeconds;
        }
        return position;
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent ev) {
        show(DEFAULT_TIMEOUT);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(DEFAULT_TIMEOUT);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mFensterPlayer.isPlaying()) {
                mFensterPlayer.start();
                updatePausePlay();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mFensterPlayer.isPlaying()) {
                mFensterPlayer.pause();
                updatePausePlay();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(DEFAULT_TIMEOUT);
        return super.dispatchKeyEvent(event);
    }

    private void updatePausePlay() {
        if (mPauseButton == null) {
            return;
        }

        if (mFensterPlayer.isPlaying()) {
            mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void doPauseResume() {
        if (mFensterPlayer.isPlaying()) {
            mFensterPlayer.pause();
        } else {
            mFensterPlayer.start();
        }
        updatePausePlay();
    }

    @Override
    public void setEnabled(final boolean enabled) {

        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }

        if (mNextButton != null) {
            mNextButton.setEnabled(enabled);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    @Override
    public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SimpleMediaFensterPlayerController.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(final AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SimpleMediaFensterPlayerController.class.getName());
    }

    @Override
    public void onFirstVideoFrameRendered() {
        controlsRoot.setVisibility(View.VISIBLE);
        bottomControlsRoot.setVisibility(View.VISIBLE);
        mFirstTimeLoading = false;
    }

    @Override
    public void onPlay() {
        hideLoadingView();
    }

    @Override
    public void onBuffer() {
        showLoadingView();
    }

    @Override
    public boolean onStopWithExternalError(int position) {
        return false;
    }

    private void hideLoadingView() {
        hide();
        loadingView.setVisibility(View.GONE);

        mLoading = false;
    }

    private void showLoadingView() {
        mLoading = true;
        loadingView.setVisibility(View.VISIBLE);
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(final SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(final SeekBar bar, final int progress, final boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mFensterPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mFensterPlayer.seekTo((int) newposition);
            if (mCurrentTime != null) {
                mCurrentTime.setText(stringForTime((int) newposition));
            }
        }

        public void onStopTrackingTouch(final SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(DEFAULT_TIMEOUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    if (mFensterPlayer.isPlaying()) {
                        hide();
                    } else {
                        // re-schedule to check again
                        Message fadeMessage = obtainMessage(FADE_OUT);
                        removeMessages(FADE_OUT);
                        sendMessageDelayed(fadeMessage, DEFAULT_TIMEOUT);
                    }
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mFensterPlayer.isPlaying()) {
                        final Message message = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(message, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    private final OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(final View v) {
            doPauseResume();
            show(DEFAULT_TIMEOUT);
        }
    };

    /**
     * Called by ViewTouchRoot on user touches,
     * so we can avoid hiding the ui while the user is interacting.
     */
    @Override
    public void onControllerUiTouched() {
        if (mShowing) {
            Log.d(TAG, "controller ui touch received!");
            show();
        }
    }
}
