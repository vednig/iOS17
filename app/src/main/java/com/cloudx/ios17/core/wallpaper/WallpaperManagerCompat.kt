/*
 * Copyright 2022 /e/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudx.ios17.core.wallpaper

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.cloudx.ios17.core.utils.SingletonHolder
import com.cloudx.ios17.core.utils.ensureOnMainThread
import com.cloudx.ios17.core.utils.useApplicationContext
import com.cloudx.ios17.core.wallpaper.WallpaperColorsCompat.Companion.HINT_SUPPORTS_DARK_THEME
import java.lang.reflect.Method
import timber.log.Timber

class WallpaperManagerCompat(val context: Context) {

    private val TAG = "WallpaperManagerCompat"

    private val listeners = mutableListOf<OnColorsChangedListener>()
    private val wallpaperManager = context.getSystemService(WallpaperManager::class.java)!!

    var wallpaperColors: WallpaperColorsCompat? = null
        private set

    val colorHints
        get() = wallpaperColors?.colorHints ?: 0
    val supportsDarkTheme
        get() = (colorHints and HINT_SUPPORTS_DARK_THEME) != 0

    private var wcColorHintsMethod: Method? = null

    init {
        try {
            wcColorHintsMethod = WallpaperColors::class.java.getDeclaredMethod("getColorHints")
        } catch (exc: Exception) {
            Timber.tag(TAG).e(exc, "getColorHints not available")
        }
        wallpaperManager.addOnColorsChangedListener(
            { colors, which ->
                if ((which and WallpaperManager.FLAG_SYSTEM) != 0) {
                    update(colors)
                    notifyChange()
                }
            },
            Handler(Looper.getMainLooper())
        )
        update(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM))
    }

    private fun update(wallpaperColors: WallpaperColors?) {
        if (wallpaperColors == null) {
            this.wallpaperColors = null
            return
        }
        var hints = 0
        wcColorHintsMethod?.let {
            try {
                hints = it.invoke(wallpaperColors) as Int
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "error calling color hints")
            }
        }
        this.wallpaperColors = WallpaperColorsCompat(wallpaperColors.primaryColor.toArgb(), hints)
    }

    fun addOnChangeListener(listener: OnColorsChangedListener) {
        listeners.add(listener)
    }

    fun removeOnChangeListener(listener: OnColorsChangedListener) {
        listeners.remove(listener)
    }

    private fun notifyChange() {
        listeners.toTypedArray().forEach { it.onColorsChanged() }
    }

    interface OnColorsChangedListener {
        fun onColorsChanged()
    }

    companion object :
        SingletonHolder<WallpaperManagerCompat, Context>(
            ensureOnMainThread(useApplicationContext(::WallpaperManagerCompat))
        )
}
