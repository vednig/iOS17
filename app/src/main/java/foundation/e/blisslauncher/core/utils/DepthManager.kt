package foundation.e.blisslauncher.core.utils

import android.app.WallpaperManager
import android.os.IBinder
import android.view.View
import foundation.e.blisslauncher.features.launcher.LauncherActivity

class DepthManager(private val launcher: LauncherActivity) {

    private val wallpaperManager = launcher.getSystemService(WallpaperManager::class.java)!!
    private val setWallpaperZoomOut = WallpaperManager::class.java.getMethod(
        "setWallpaperZoomOut", IBinder::class.java, Float::class.java)

    init {
        launcher.rootView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                updateDepth()
            }

            override fun onViewDetachedFromWindow(v: View) {
            }
        })
    }

    fun updateDepth() {
        val windowToken = launcher.rootView.windowToken
        if (windowToken != null) {
            setWallpaperZoomOut.invoke(wallpaperManager, windowToken, 1)
        }
    }
}
