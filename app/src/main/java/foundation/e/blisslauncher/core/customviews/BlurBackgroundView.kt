package foundation.e.blisslauncher.core.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import foundation.e.blisslauncher.core.blur.BlurWallpaperProvider
import foundation.e.blisslauncher.core.blur.ShaderBlurDrawable
import foundation.e.blisslauncher.core.runOnMainThread

class BlurBackgroundView(context: Context, attrs: AttributeSet?) : View(context, attrs), Insettable,
    BlurWallpaperProvider.Listener {

    private val blurWallpaperProvider by lazy { BlurWallpaperProvider.getInstance(context) }

    private var fullBlurDrawable: ShaderBlurDrawable? = null
    private var blurAlpha = 255

    private val blurDrawableCallback by lazy {
        object : Drawable.Callback {
            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            }

            override fun invalidateDrawable(who: Drawable) {
                runOnMainThread { invalidate() }
            }

            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            }
        }
    }

    init {
        createFullBlurDrawable()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        BlurWallpaperProvider.getInstance(context).addListener(this)
        fullBlurDrawable?.startListening()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        BlurWallpaperProvider.getInstance(context).removeListener(this)
        fullBlurDrawable?.stopListening()
    }

    override fun onDraw(canvas: Canvas) {
        fullBlurDrawable?.apply {
            alpha = blurAlpha
            this.draw(canvas)
        }
        canvas.drawARGB((blurAlpha * 0.01).toInt(), 0, 0, 0)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed) {
            fullBlurDrawable?.setBounds(left, top, right, bottom)
        }
    }

    private fun createFullBlurDrawable() {
        fullBlurDrawable?.let { if (isAttachedToWindow) it.stopListening() }
        fullBlurDrawable = blurWallpaperProvider.createDrawable().apply {
                callback = blurDrawableCallback
                setBounds(left, top, right, bottom)
                if (isAttachedToWindow) startListening()
            }
    }

    override fun onEnabledChanged() {
        createFullBlurDrawable()
    }

    override fun setInsets(insets: Rect) {}
}
