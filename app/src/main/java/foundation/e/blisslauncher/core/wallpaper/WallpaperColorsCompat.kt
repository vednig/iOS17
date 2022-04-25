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
package foundation.e.blisslauncher.core.wallpaper

import androidx.annotation.IntDef

data class WallpaperColorsCompat(
    val primaryColor: Int,
    @ColorsHints val colorHints: Int
) {

    companion object {

        const val HINT_SUPPORTS_DARK_TEXT = 0x1
        const val HINT_SUPPORTS_DARK_THEME = 0x2

        @IntDef(
            value = [HINT_SUPPORTS_DARK_TEXT, HINT_SUPPORTS_DARK_THEME],
            flag = true
        )
        @Retention(
            AnnotationRetention.SOURCE
        )
        annotation class ColorsHints
    }
}
