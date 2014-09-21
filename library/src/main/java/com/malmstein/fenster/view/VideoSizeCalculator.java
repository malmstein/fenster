package com.malmstein.fenster.view;

import android.view.SurfaceHolder;
import android.view.View;

public class VideoSizeCalculator {

    private Dimens dimens;

    private int mVideoWidth;
    private int mVideoHeight;

    public VideoSizeCalculator() {
        dimens = new Dimens();
    }

    public void setVideoSize(int mVideoWidth, int mVideoHeight) {
        this.mVideoWidth = mVideoWidth;
        this.mVideoHeight = mVideoHeight;
    }

    public boolean hasASizeYet() {
        return mVideoWidth > 0 && mVideoHeight > 0;
    }

    protected Dimens measure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = View.getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (hasASizeYet()) {

            int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        }
        dimens.width = width;
        dimens.height = height;
        return dimens;
    }

    public boolean currentSizeIs(int w, int h) {
        return mVideoWidth == w && mVideoHeight == h;
    }

    public void updateHolder(SurfaceHolder holder) {
        holder.setFixedSize(mVideoWidth, mVideoHeight);
    }

    static class Dimens {
        int width;
        int height;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

}
