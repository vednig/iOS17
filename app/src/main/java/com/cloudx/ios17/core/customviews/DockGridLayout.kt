package com.cloudx.ios17.core.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.GridLayout
import com.cloudx.ios17.BlissLauncher
import com.cloudx.ios17.core.blur.BlurViewDelegate
import com.cloudx.ios17.core.blur.BlurWallpaperProvider
import com.cloudx.ios17.core.utils.OffsetParent

class DockGridLayout
@JvmOverloads
constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    GridLayout(mContext, attrs, defStyleAttr), com.cloudx.ios17.core.customviews.Insettable, OffsetParent {

    private val offsetParentDelegate = OffsetParent.OffsetParentDelegate()

    override val offsetX: Float
        get() = translationX
    override val offsetY: Float
        get() = translationY

    private val blurDelegate = BlurViewDelegate(this, BlurWallpaperProvider.blurConfigDock, attrs)

    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        blurDelegate.draw(canvas)
        super.onDraw(canvas)
    }

    override fun setInsets(insets: Rect) {
        val deviceProfile = com.cloudx.ios17.BlissLauncher.getApplication(mContext).deviceProfile
        val lp = layoutParams as com.cloudx.ios17.core.customviews.InsettableRelativeLayout.LayoutParams
        lp.height = deviceProfile.hotseatCellHeightPx + insets.bottom
        setPadding(
            deviceProfile.iconDrawablePaddingPx / 2,
            0,
            deviceProfile.iconDrawablePaddingPx / 2,
            insets.bottom
        )
        layoutParams = lp
    }

    override fun setTranslationX(translationX: Float) {
        super.setTranslationX(translationX)
        offsetParentDelegate.notifyOffsetChanged()
    }

    override fun setTranslationY(translationY: Float) {
        super.setTranslationY(translationY)
        offsetParentDelegate.notifyOffsetChanged()
    }

    override fun addOnOffsetChangeListener(listener: OffsetParent.OnOffsetChangeListener) {
        offsetParentDelegate.addOnOffsetChangeListener(listener)
    }

    override fun removeOnOffsetChangeListener(listener: OffsetParent.OnOffsetChangeListener) {
        offsetParentDelegate.removeOnOffsetChangeListener(listener)
    }
}
