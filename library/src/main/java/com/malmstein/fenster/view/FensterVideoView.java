package com.malmstein.fenster.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;

import com.malmstein.fenster.R;
import com.malmstein.fenster.controller.FensterPlayerController;
import com.malmstein.fenster.play.FensterPlayer;
import com.malmstein.fenster.play.FensterVideoStateListener;

import java.io.IOException;
import java.util.Map;

/**
 * Displays a video file.  The VideoView class
 * can load images from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that
 * it can be used in any layout manager, and provides various display options
 * such as scaling and tinting.<p>
 * <p/>
 * <em>Note: VideoView does not retain its full state when going into the
 * background.</em>  In particular, it does not restore the current play state,
 * play position, selected tracks added via
 * {@link android.app.Activity#onSaveInstanceState} and
 * {@link android.app.Activity#onRestoreInstanceState}.<p>
 * Also note that the audio session id (from {@link #getAudioSessionId}) may
 * change from its previously returned value when the VideoView is restored.
 */

public class FensterVideoView extends TextureView implements MediaController.MediaPlayerControl, FensterPlayer {

    public static final String TAG = "TextureVideoView";
    public static final int VIDEO_BEGINNING = 0;

    public enum ScaleType {
        SCALE_TO_FIT, CROP
    }

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int MILLIS_IN_SEC = 1000;

    // collaborators / delegates / composites .. discuss
    private final VideoSizeCalculator videoSizeCalculator;
    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    private ScaleType mScaleType;

    private Uri mUri;

    private AssetFileDescriptor mAssetFileDescriptor;
    private Map<String, String> mHeaders;
    private SurfaceTexture mSurfaceTexture;
    private MediaPlayer mMediaPlayer = null;
    private FensterPlayerController fensterPlayerController;
    private OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private int mAudioSession;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private int mCurrentBufferPercentage;
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    private FensterVideoStateListener onPlayStateListener;

    private AlertDialog errorDialog;

    public FensterVideoView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FensterVideoView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        applyCustomAttributes(context, attrs);
        videoSizeCalculator = new VideoSizeCalculator();
        initVideoView();
    }

    private void applyCustomAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FensterVideoView);
        if (typedArray == null) {
            return;
        }
        int[] attrsValues = {R.attr.scaleType};
        TypedArray scaleTypedArray = context.obtainStyledAttributes(attrs, attrsValues);
        if (scaleTypedArray != null) {
            try {
                int scaleTypeId = typedArray.getInt(0, 0);
                mScaleType = ScaleType.values()[scaleTypeId];
            } finally {
                typedArray.recycle();
            }
        } else {
            mScaleType = ScaleType.SCALE_TO_FIT;
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        VideoSizeCalculator.Dimens dimens = videoSizeCalculator.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(dimens.getWidth(), dimens.getHeight());
    }

    @Override
    public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(FensterVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(final AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(FensterVideoView.class.getName());
    }

    public int resolveAdjustedSize(final int desiredSize, final int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    private void initVideoView() {
        videoSizeCalculator.setVideoSize(0, 0);

        setSurfaceTextureListener(mSTListener);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        setOnInfoListener(onInfoToPlayStateListener);
    }

    private void disableFileDescriptor() {
        mAssetFileDescriptor = null;
    }

    public void setVideo(final String path) {
        disableFileDescriptor();
        setVideo(Uri.parse(path), VIDEO_BEGINNING);
    }

    public void setVideo(final String url, final int seekInSeconds) {
        disableFileDescriptor();
        setVideo(Uri.parse(url), seekInSeconds);
    }

    public void setVideo(final Uri uri, final int seekInSeconds) {
        disableFileDescriptor();
        setVideoURI(uri, null, seekInSeconds);
    }

    public void setVideo(final AssetFileDescriptor assetFileDescriptor) {
        mAssetFileDescriptor = assetFileDescriptor;
        setVideoURI(null, null, VIDEO_BEGINNING);
    }

    public void setVideo(final AssetFileDescriptor assetFileDescriptor, final int seekInSeconds) {
        mAssetFileDescriptor = assetFileDescriptor;
        setVideoURI(null, null, seekInSeconds);
    }

    /**
     * Set the scale type of the video, needs to be set after setVideo() has been called
     *
     * @param scaleType
     */
    private void setScaleType(ScaleType scaleType) {
        switch (scaleType) {
            case SCALE_TO_FIT:
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                break;
            case CROP:
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                break;
        }
    }

    private void setVideoURI(final Uri uri, final Map<String, String> headers, final int seekInSeconds) {
        Log.d(TAG, "start playing: " + uri);
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = seekInSeconds * 1000;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            setKeepScreenOn(false);
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    private void openVideo() {
        if (notReadyForPlaybackJustYetWillTryAgainLater()) {
            return;
        }
        tellTheMusicPlaybackServiceToPause();

        // we shouldn't clear the target state, because somebody might have called start() previously
        release(false);
        try {
            mMediaPlayer = new MediaPlayer();

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;

            setDataSource();
            setScaleType(mScaleType);

            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (final IOException | IllegalArgumentException ex) {
            notifyUnableToOpenContent(ex);
        }
    }

    private void setDataSource() throws IOException {
        if (mAssetFileDescriptor != null) {
            mMediaPlayer.setDataSource(
                    mAssetFileDescriptor.getFileDescriptor(),
                    mAssetFileDescriptor.getStartOffset(),
                    mAssetFileDescriptor.getLength()
            );
        } else {
            mMediaPlayer.setDataSource(getContext(), mUri, mHeaders);
        }
    }

    private boolean notReadyForPlaybackJustYetWillTryAgainLater() {
        return mSurfaceTexture == null;
    }

    private void tellTheMusicPlaybackServiceToPause() {
        // these constants need to be published somewhere in the framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        getContext().sendBroadcast(i);
    }

    private void notifyUnableToOpenContent(final Exception ex) {
        Log.w("Unable to open content:" + mUri, ex);
        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;
        mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
    }

    public void setMediaController(final FensterPlayerController controller) {
        hideMediaController();
        fensterPlayerController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && fensterPlayerController != null) {
            fensterPlayerController.setMediaPlayer(this);
//            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
//            fensterPlayerController.setAnchorView(anchorView);
            fensterPlayerController.setEnabled(isInPlaybackState());
        }
    }

    private MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(final MediaPlayer mp, final int width, final int height) {
            videoSizeCalculator.setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());
            if (videoSizeCalculator.hasASizeYet()) {
                requestLayout();
            }
        }
    };

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;

            mCanPause = true;
            mCanSeekBack = true;
            mCanSeekForward = true;

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (fensterPlayerController != null) {
                fensterPlayerController.setEnabled(true);
            }
            videoSizeCalculator.setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }

            if (mTargetState == STATE_PLAYING) {
                start();
                showMediaController();
            } else if (pausedAt(seekToPosition)) {
                showStickyMediaController();
            }
        }
    };

    private boolean pausedAt(final int seekToPosition) {
        return !isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0);
    }

    private void showStickyMediaController() {
        if (fensterPlayerController != null) {
            fensterPlayerController.show(0);
        }
    }

    private OnCompletionListener mCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(final MediaPlayer mp) {
            setKeepScreenOn(false);
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            hideMediaController();
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    private OnInfoListener mInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(final MediaPlayer mp, final int arg1, final int arg2) {
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, arg1, arg2);
            }
            return true;
        }
    };

    private OnErrorListener mErrorListener = new OnErrorListener() {
        @Override
        public boolean onError(final MediaPlayer mp, final int frameworkError, final int implError) {
            Log.d(TAG, "Error: " + frameworkError + "," + implError);
            if (mCurrentState == STATE_ERROR) {
                return true;
            }
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            hideMediaController();

            if (allowPlayStateToHandle(frameworkError)) {
                return true;
            }

            if (allowErrorListenerToHandle(frameworkError, implError)) {
                return true;
            }

            handleError(frameworkError);
            return true;
        }
    };

    private void hideMediaController() {
        if (fensterPlayerController != null) {
            fensterPlayerController.hide();
        }
    }

    private void showMediaController() {
        if (fensterPlayerController != null) {
            fensterPlayerController.show();
        }
    }

    private boolean allowPlayStateToHandle(final int frameworkError) {
        if (frameworkError == MediaPlayer.MEDIA_ERROR_UNKNOWN || frameworkError == MediaPlayer.MEDIA_ERROR_IO) {
            Log.e(TAG, "TextureVideoView error. File or network related operation errors.");
            if (hasPlayStateListener()) {
                return onPlayStateListener.onStopWithExternalError(mMediaPlayer.getCurrentPosition() / MILLIS_IN_SEC);
            }
        }
        return false;
    }

    private boolean allowErrorListenerToHandle(final int frameworkError, final int implError) {
        if (mOnErrorListener != null) {
            return mOnErrorListener.onError(mMediaPlayer, frameworkError, implError);
        }

        return false;
    }

    private void handleError(final int frameworkError) {
        if (getWindowToken() != null) {
            if (errorDialog != null && errorDialog.isShowing()) {
                Log.d(TAG, "Dismissing last error dialog for a new one");
                errorDialog.dismiss();
            }
            errorDialog = createErrorDialog(this.getContext(), mOnCompletionListener, mMediaPlayer, getErrorMessage(frameworkError));
            errorDialog.show();
        }
    }

    private static AlertDialog createErrorDialog(final Context context, final OnCompletionListener completionListener, final MediaPlayer mediaPlayer, final int errorMessage) {
        return new AlertDialog.Builder(context)
                .setMessage(errorMessage)
                .setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int whichButton) {
                                    /* If we get here, there is no onError listener, so
                                     * at least inform them that the video is over.
                                     */
                                if (completionListener != null) {
                                    completionListener.onCompletion(mediaPlayer);
                                }
                            }
                        }
                )
                .setCancelable(false)
                .create();
    }

    private static int getErrorMessage(final int frameworkError) {
        int messageId = R.string.play_error_message;

        if (frameworkError == MediaPlayer.MEDIA_ERROR_IO) {
            Log.e(TAG, "TextureVideoView error. File or network related operation errors.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_MALFORMED) {
            Log.e(TAG, "TextureVideoView error. Bitstream is not conforming to the related coding standard or file spec.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.e(TAG, "TextureVideoView error. Media server died. In this case, the application must release the MediaPlayer object and instantiate a new one.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
            Log.e(TAG, "TextureVideoView error. Some operation takes too long to complete, usually more than 3-5 seconds.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.e(TAG, "TextureVideoView error. Unspecified media player error.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
            Log.e(TAG, "TextureVideoView error. Bitstream is conforming to the related coding standard or file spec, but the media framework does not support the feature.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            Log.e(TAG, "TextureVideoView error. The video is streamed and its container is not valid for progressive playback i.e the video's index (e.g moov atom) is not at the start of the file.");
            messageId = R.string.play_progressive_error_message;
        }
        return messageId;
    }

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(final MediaPlayer mp, final int percent) {
            mCurrentBufferPercentage = percent;
        }
    };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(final MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(final OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(final OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    private void setOnInfoListener(final OnInfoListener l) {
        mOnInfoListener = l;
    }

    private SurfaceTextureListener mSTListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            mSurfaceTexture = surface;
            openVideo();
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = videoSizeCalculator.currentSizeIs(width, height);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            mSurfaceTexture = null;
            hideMediaController();
            release(true);
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
            mSurfaceTexture = surface;
        }
    };

    /*
     * release the media player in any state
     */
    private void release(final boolean clearTargetState) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (clearTargetState) {
                mTargetState = STATE_IDLE;
            }
        }
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent ev) {
        if (isInPlaybackState() && fensterPlayerController != null) {
            fensterPlayerController.show();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && fensterPlayerController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    showMediaController();
                } else {
                    start();
                    hideMediaController();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    hideMediaController();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    showMediaController();
                }
                return true;
            } else {
                fensterPlayerController.show();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            setKeepScreenOn(true);
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
                setKeepScreenOn(false);
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }

        return -1;
    }

    /**
     * @return current position in milliseconds
     */
    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getCurrentPositionInSeconds() {
        return getCurrentPosition() / MILLIS_IN_SEC;
    }

    @Override
    public void seekTo(final int millis) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(millis);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = millis;
        }
    }

    public void seekToSeconds(final int seconds) {
        seekTo(seconds * MILLIS_IN_SEC);
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(final MediaPlayer mp) {
                Log.i(TAG, "seek completed");
            }
        });
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    private final OnInfoListener onInfoToPlayStateListener = new OnInfoListener() {

        @Override
        public boolean onInfo(final MediaPlayer mp, final int what, final int extra) {
            if (noPlayStateListener()) {
                return false;
            }

            if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
                onPlayStateListener.onFirstVideoFrameRendered();
                onPlayStateListener.onPlay();
            }
            if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
                onPlayStateListener.onBuffer();
            }
            if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
                onPlayStateListener.onPlay();
            }

            return false;
        }
    };

    private boolean noPlayStateListener() {
        return !hasPlayStateListener();
    }

    private boolean hasPlayStateListener() {
        return onPlayStateListener != null;
    }

    public void setOnPlayStateListener(final FensterVideoStateListener onPlayStateListener) {
        this.onPlayStateListener = onPlayStateListener;
    }

}
