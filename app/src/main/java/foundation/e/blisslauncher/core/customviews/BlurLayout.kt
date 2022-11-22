package foundation.e.blisslauncher.core.customviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import foundation.e.blisslauncher.core.blur.BlurViewDelegate
import foundation.e.blisslauncher.core.blur.BlurWallpaperProvider

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
