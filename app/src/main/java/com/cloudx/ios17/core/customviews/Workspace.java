package com.cloudx.ios17.core.customviews;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cloudx.ios17.core.customviews.pageindicators.PageIndicatorDots;

import com.cloudx.ios17.core.customviews.pageindicators.PageIndicatorDots;
import com.cloudx.ios17.core.customviews.pageindicators.PageIndicatorDots;
import com.cloudx.ios17.features.launcher.LauncherActivity;
import com.cloudx.ios17.core.customviews.pageindicators.PageIndicatorDots;

public class Workspace extends PagedView<PageIndicatorDots> implements View.OnTouchListener {

    private static final String TAG = "Workspace";
    private static final int DEFAULT_PAGE = 0;
    private final LauncherActivity mLauncher;
    private LayoutTransition mLayoutTransition;

    public Workspace(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Workspace(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);

        mLauncher = LauncherActivity.getLauncher(context);
        setHapticFeedbackEnabled(false);
        initWorkspace();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    private void initWorkspace() {
        mCurrentPage = DEFAULT_PAGE;
        setClipToPadding(false);
        setupLayoutTransition();

        // setWallpaperDimension();
    }

    private void setupLayoutTransition() {
        // We want to show layout transitions when pages are deleted, to close the gap.
        mLayoutTransition = new LayoutTransition();
        mLayoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING);
        mLayoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        mLayoutTransition.disableTransitionType(LayoutTransition.APPEARING);
        mLayoutTransition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        setLayoutTransition(mLayoutTransition);
    }

    void enableLayoutTransitions() {
        setLayoutTransition(mLayoutTransition);
    }

    void disableLayoutTransitions() {
        setLayoutTransition(null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
