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
package com.cloudx.ios17.core.utils

import android.content.Context
import com.cloudx.ios17.R
import com.cloudx.ios17.core.wallpaper.WallpaperColorsCompat.Companion.HINT_SUPPORTS_DARK_TEXT
import com.cloudx.ios17.core.wallpaper.WallpaperManagerCompat

@JvmOverloads
fun getActivityThemeRes(
    context: Context,
    colorHints: Int = WallpaperManagerCompat.getInstance(context).colorHints
): Int {
    val supportsDarkText = (colorHints and HINT_SUPPORTS_DARK_TEXT) != 0
    return if (supportsDarkText) R.style.HomeScreenTheme_DarkText else R.style.HomeScreenTheme
}

fun isWorkspaceDarkText(context: Context) = getAttrBoolean(context, R.attr.isWorkspaceDarkText)

fun getAttrBoolean(context: Context, attr: Int): Boolean {
    val ta = context.obtainStyledAttributes(intArrayOf(attr))
    val value = ta.getBoolean(0, false)
    ta.recycle()
    return value
}
