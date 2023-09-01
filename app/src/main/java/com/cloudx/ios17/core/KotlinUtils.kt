package com.cloudx.ios17.core

import android.os.Handler
import android.os.Looper

val mainHandler by lazy { Handler(Looper.getMainLooper()) }

fun runOnMainThread(r: () -> Unit) {
    com.cloudx.ios17.core.runOnThread(com.cloudx.ios17.core.mainHandler, r)
}

fun runOnThread(handler: Handler, r: () -> Unit) {
    if (handler.looper.thread.id == Looper.myLooper()?.thread?.id) {
        r()
    } else {
        handler.post(r)
    }
}

inline fun <T> Iterable<T>.safeForEach(action: (T) -> Unit) {
    val tmp = ArrayList<T>()
    tmp.addAll(this)
    for (element in tmp) action(element)
}
