package foundation.e.blisslauncher.core.blur

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

class BlurDrawable internal constructor(private val blurWallpaperProvider: BlurWallpaperProvider) :
    Drawable(), BlurWallpaperProvider.Listener {

    private var blurAlpha = 255
    private val blurPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private var blurBitmap: Bitmap? = null
        set(value) {
            if (field != value) {
                field = value
                blurPaint.shader =
                    value?.let { BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
            }
        }

    private val blurBounds = RectF()
    private var offsetX = 0f
    private var offsetY = 0f

    override fun draw(canvas: Canvas) {
        val width = blurBounds.right.toInt() - blurBounds.left.toInt()
        val height = blurBounds.bottom.toInt() - blurBounds.top.toInt()
        if (width <= 0 || height <= 0) return
        if (blurAlpha == 0) return
        blurBitmap = blurWallpaperProvider.wallpapers?.second

        if (blurBitmap == null) {
            blurBitmap = blurWallpaperProvider.placeholder
        }

        val left = blurBounds.left + offsetX
        val top = blurBounds.top + offsetY
        val right = blurBounds.right + offsetX
        val bottom = blurBounds.bottom + offsetY
        canvas.translate(-left, -top)
        canvas.drawRect(left, top, right, bottom, blurPaint)
        canvas.translate(left, top)
    }

    override fun setAlpha(alpha: Int) {
        blurAlpha = alpha
        blurPaint.alpha = alpha
    }

    override fun getAlpha(): Int {
        return blurAlpha
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) =
        setBlurBounds(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

    fun setBlurBounds(left: Float, top: Float, right: Float, bottom: Float) {
        if (blurBounds.left != left ||
            blurBounds.top != top ||
            blurBounds.right != right ||
            blurBounds.bottom != bottom
        ) {
            blurBounds.set(left, top, right, bottom)
        }
    }

    fun setOffsets(offsetX: Float, offsetY: Float) {
        this.offsetX = offsetX
        this.offsetY = offsetY
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun onWallpaperChanged() {
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    fun startListening() = blurWallpaperProvider.addListener(this)

    fun stopListening() = blurWallpaperProvider.removeListener(this)
}
