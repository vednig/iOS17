package foundation.e.blisslauncher.core.blur

import android.content.Context
import android.graphics.Bitmap
import com.hoko.blur.HokoBlur

typealias BitmapPair = Pair<Bitmap, Bitmap>

class BlurWallpaperFilter(private val context: Context) : WallpaperFilter<BitmapPair> {

    private var blurRadiusSmall = 8
    private var blurRadiusLarge = 25

    override fun apply(wallpaper: Bitmap): WallpaperFilter.ApplyTask<BitmapPair> {
        return WallpaperFilter.ApplyTask.create { emitter ->
            var wallpaperSmallBlur: Bitmap? = null
            var wallpaperLargeBlur: Bitmap? = null
            try {
                wallpaperSmallBlur = HokoBlur.with(context)
                    .scheme(HokoBlur.SCHEME_NATIVE)
                    .mode(HokoBlur.MODE_STACK)
                    .radius(blurRadiusSmall)
                    .sampleFactor(8f)
                    .forceCopy(false)
                    .needUpscale(true)
                    .processor()
                    .blur(wallpaper)
                wallpaperLargeBlur = HokoBlur.with(context)
                    .scheme(HokoBlur.SCHEME_NATIVE)
                    .mode(HokoBlur.MODE_STACK)
                    .radius(blurRadiusLarge)
                    .sampleFactor(16f)
                    .forceCopy(false)
                    .needUpscale(true)
                    .processor()
                    .blur(wallpaper)
                emitter.onSuccess(Pair(wallpaperSmallBlur, wallpaperLargeBlur))
            } catch (t: Throwable) {
                wallpaperSmallBlur?.recycle()
                wallpaperLargeBlur?.recycle()
                emitter.onError(t)
            }
        }
    }
}
