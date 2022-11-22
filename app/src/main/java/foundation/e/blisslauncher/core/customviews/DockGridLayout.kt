package foundation.e.blisslauncher.core.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.GridLayout
import foundation.e.blisslauncher.BlissLauncher
import foundation.e.blisslauncher.core.blur.BlurViewDelegate
import foundation.e.blisslauncher.core.blur.BlurWallpaperProvider
import foundation.e.blisslauncher.core.utils.OffsetParent

class DockGridLayout
@JvmOverloads
constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    GridLayout(mContext, attrs, defStyleAttr), Insettable, OffsetParent {

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
        val deviceProfile = BlissLauncher.getApplication(mContext).deviceProfile
        val lp = layoutParams as InsettableRelativeLayout.LayoutParams
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
