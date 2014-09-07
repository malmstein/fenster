package com.malmstein.fenster.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.malmstein.fenster.R;

public class LoadingView extends FrameLayout {

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_loading, this);
    }

    public void showLoading() {
        setVisibility(VISIBLE);
    }

    public void hideLoading() {
        setVisibility(GONE);
    }

}
