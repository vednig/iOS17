package com.cloudx.ios17.features.launcher;

public interface OnSwipeDownListener {

    void onSwipeStart();

    void onSwipe(int position);

    void onSwipeFinish();
}
