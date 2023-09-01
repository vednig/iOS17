package com.cloudx.ios17.core.customviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import com.cloudx.ios17.core.blur.BlurViewDelegate
import com.cloudx.ios17.core.blur.BlurWallpaperProvider

open class BlurLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val delegate = BlurViewDelegate(this, BlurWallpaperProvider.blurConfigWidget, attrs)

    init {
        setWillNotDraw(false)
        clipToOutline = true

        outlineProvider = delegate.outlineProvider
    }

    override fun onDraw(canvas: Canvas) {
        delegate.draw(canvas)
        super.onDraw(canvas)
    }
}
