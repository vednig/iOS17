package com.cloudx.ios17.core;

import android.os.Handler;
import android.os.SystemClock;

public class Alarm implements Runnable {
    // if we reach this time and the alarm hasn't been cancelled, call the listener
    private long mAlarmTriggerTime;

    // if we've scheduled a call to run() (ie called mHandler.postDelayed), this
    // variable is true.
    // We use this to avoid having multiple pending callbacks
    private boolean mWaitingForCallback;

    private Handler mHandler;
    private OnAlarmListener mAlarmListener;
    private boolean mAlarmPending = false;

    public Alarm() {
        mHandler = new Handler();
    }

    public void setOnAlarmListener(OnAlarmListener alarmListener) {
        mAlarmListener = alarmListener;
    }

    // Sets the alarm to go off in a certain number of milliseconds. If the alarm is
    // already set,
    // it's overwritten and only the new alarm setting is used
    public void setAlarm(long millisecondsInFuture) {
        long currentTime = SystemClock.uptimeMillis();
        mAlarmPending = true;
        long oldTriggerTime = mAlarmTriggerTime;
        mAlarmTriggerTime = currentTime + millisecondsInFuture;

        // If the previous alarm was set for a longer duration, cancel it.
        if (mWaitingForCallback && oldTriggerTime > mAlarmTriggerTime) {
            mHandler.removeCallbacks(this);
            mWaitingForCallback = false;
        }
        if (!mWaitingForCallback) {
            mHandler.postDelayed(this, mAlarmTriggerTime - currentTime);
            mWaitingForCallback = true;
        }
    }

    public void cancelAlarm() {
        mAlarmPending = false;
    }

    // this is called when our timer runs out
    @Override
    public void run() {
        mWaitingForCallback = false;
        if (mAlarmPending) {
            long currentTime = SystemClock.uptimeMillis();
            if (mAlarmTriggerTime > currentTime) {
                // We still need to wait some time to trigger spring loaded mode--
                // post a new callback
                mHandler.postDelayed(this, Math.max(0, mAlarmTriggerTime - currentTime));
                mWaitingForCallback = true;
            } else {
                mAlarmPending = false;
                if (mAlarmListener != null) {
                    mAlarmListener.onAlarm(this);
                }
            }
        }
    }

    public boolean alarmPending() {
        return mAlarmPending;
    }

    public interface OnAlarmListener {
        void onAlarm(Alarm alarm);
    }
}
