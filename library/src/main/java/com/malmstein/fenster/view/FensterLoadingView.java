package com.malmstein.fenster.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.malmstein.fenster.R;

public class FensterLoadingView extends FrameLayout {

    public FensterLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FensterLoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_loading, this);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }

}
