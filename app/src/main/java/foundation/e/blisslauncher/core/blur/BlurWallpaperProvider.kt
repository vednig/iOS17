package foundation.e.blisslauncher.core.blur

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import foundation.e.blisslauncher.R
import foundation.e.blisslauncher.core.Utilities
import foundation.e.blisslauncher.core.runOnMainThread
import foundation.e.blisslauncher.core.safeForEach
import foundation.e.blisslauncher.core.utils.SingletonHolder
import foundation.e.blisslauncher.core.utils.ensureOnMainThread
import foundation.e.blisslauncher.core.utils.useApplicationContext
import java.util.ArrayList
import kotlin.math.max

class BlurWallpaperProvider(val context: Context) {

    private val wallpaperManager: WallpaperManager = WallpaperManager.getInstance(context)
    private val listeners = ArrayList<Listener>()
    private val displayMetrics = DisplayMetrics()

    var wallpapers: Pair<Bitmap, Bitmap>? = null
        private set(value) {
            if (field != value) {
                field?.first?.recycle()
                field?.second?.recycle()
                field = value
            }
        }
    var placeholder: Bitmap? = null
        private set(value) {
            if (field != value) {
                field?.recycle()
                field = value
            }
        }

    private val vibrancyPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

    private val mUpdateRunnable = Runnable { updateWallpaper() }

    private val wallpaperFilter = BlurWallpaperFilter(context)
    private var applyTask: WallpaperFilter.ApplyTask<Pair<Bitmap, Bitmap>>? = null

    private var updatePending = false

    init {
        isEnabled = getEnabledStatus()
        updateAsync()
    }

    private fun getEnabledStatus() = wallpaperManager.wallpaperInfo == null

    fun updateAsync() {
        Utilities.THREAD_POOL_EXECUTOR.execute(mUpdateRunnable)
    }

    private fun updateWallpaper() {
        if (applyTask != null) {
            updatePending = true
            return
        }

        // Prepare a placeholder before hand so that it can be used in case wallpaper is null
        val wm =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        display.getRealMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        placeholder = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(placeholder!!)
        canvas.drawColor(0x44000000)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("BWP", "NO permission granted")
            return
        }

        val enabled = getEnabledStatus()
        if (enabled != isEnabled) {
            isEnabled = enabled
            runOnMainThread {
                listeners.safeForEach(Listener::onEnabledChanged)
            }
        }

        if (!isEnabled) {
            wallpapers = null
            return
        }

        var wallpaper = try {
            Utilities.drawableToBitmap(wallpaperManager.drawable, true) as Bitmap
        } catch (e: Exception) {
            runOnMainThread {
                val msg = "Failed: ${e.message}"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                notifyWallpaperChanged()
            }
            return
        }
        wallpaper = scaleAndCropToScreenSize(wallpaper)
        wallpaper = applyVibrancy(wallpaper)
        applyTask = wallpaperFilter.apply(wallpaper).setCallback { result, error ->
            if (error == null) {
                this@BlurWallpaperProvider.wallpapers = result
                runOnMainThread(::notifyWallpaperChanged)
                wallpaper.recycle()
            } else {
                if (error is OutOfMemoryError) {
                    runOnMainThread {
                        Toast.makeText(context, context.getString(R.string.toast_failed), Toast.LENGTH_LONG).show()
                        notifyWallpaperChanged()
                    }
                }
                wallpaper.recycle()
            }
        }
        applyTask = null
        if (updatePending) {
            updatePending = false
            updateWallpaper()
        }
    }

    private fun notifyWallpaperChanged() {
        listeners.forEach(Listener::onWallpaperChanged)
    }

    private fun applyVibrancy(wallpaper: Bitmap?): Bitmap {
        val width = wallpaper!!.width
        val height = wallpaper.height
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas()
        canvas.setBitmap(bitmap)
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(1.25f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        vibrancyPaint.colorFilter = filter
        canvas.drawBitmap(wallpaper, 0f, 0f, vibrancyPaint)
        wallpaper.recycle()
        return bitmap
    }

    private fun scaleAndCropToScreenSize(wallpaper: Bitmap): Bitmap {
        val wm =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        display.getRealMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val widthFactor = width.toFloat() / wallpaper.width
        val heightFactor = height.toFloat() / wallpaper.height
        val upscaleFactor = Math.max(widthFactor, heightFactor)
        if (upscaleFactor <= 0) {
            return wallpaper
        }
        val scaledWidth =
            max(width.toFloat(), wallpaper.width * upscaleFactor).toInt()
        val scaledHeight =
            max(height.toFloat(), wallpaper.height * upscaleFactor).toInt()
        val scaledWallpaper =
            Bitmap.createScaledBitmap(wallpaper, scaledWidth, scaledHeight, false)
        val navigationBarHeight = 0
        /*if (BlissLauncher.getApplication(context).getDeviceProfile().hasSoftNavigationBar(context)) {

            int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }
        }*/
        val y: Int
        y = if (scaledWallpaper.height > height) {
            (scaledWallpaper.height - height) / 2
        } else 0

        return Bitmap.createBitmap(
            scaledWallpaper,
            0,
            y,
            width,
            height - navigationBarHeight
        )
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun createDrawable(): ShaderBlurDrawable {
        return ShaderBlurDrawable(this)
    }

    fun createBlurDrawable(): BlurDrawable {
        return BlurDrawable(this)
    }

    interface Listener {
        fun onWallpaperChanged() {}
        fun onEnabledChanged() {}
    }

    /*fun clear() {
        listener = null
        cancelPreTask(true)
        sInstance = null
    }*/

    companion object :
        SingletonHolder<BlurWallpaperProvider, Context>(ensureOnMainThread(useApplicationContext(::BlurWallpaperProvider))) {

        var isEnabled: Boolean = false
        private var sEnabledFlag: Int = 0

        fun isEnabled(flag: Int): Boolean {
            return isEnabled && sEnabledFlag and flag != 0
        }
    }
}
