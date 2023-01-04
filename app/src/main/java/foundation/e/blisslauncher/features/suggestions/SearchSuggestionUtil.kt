/*
 * Copyright © ECORP SAS 2022.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
package foundation.e.blisslauncher.features.suggestions

import android.content.Context
import android.net.Uri

class SearchSuggestionUtil {
    fun getSuggestionProvider(context: Context): SuggestionProvider {
        val defaultSearchEngine = defaultSearchEngine(context)

        return with(defaultSearchEngine) {
            when {
                contains(Provider.QWANT.key, true) -> QwantProvider()
                else -> DuckDuckGoProvider()
            }
        }
    }

    fun getUriForQuery(context: Context, query: String): Uri {
        val defaultSearchEngine = defaultSearchEngine(context)

        return with(defaultSearchEngine) {
            val url =
                when {
                    contains(Provider.QWANT.key, true) -> "${Provider.QWANT.url}?q=$query"
                    contains(Provider.DUCKDUCKGO.key, true) -> "${Provider.DUCKDUCKGO.url}?q=$query"
                    contains(Provider.MOJEEK.key, true) -> "${Provider.MOJEEK.url}search?q=$query"
                    else -> "${Provider.SPOT.url}?q=$query"
                }

            Uri.parse(url)
        }
    }

    private fun defaultSearchEngine(context: Context): String {
        val contentResolver = context.contentResolver
        val uri =
            Uri.parse("content://foundation.e.browser.provider")
                .buildUpon()
                .appendPath("search_engine")
                .build()

        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor.use {
            return if (it != null && it.moveToFirst()) {
                it.getString(0)
            } else {
                ""
            }
        }
    }

    private enum class Provider(val key: String, val url: String) {
        DUCKDUCKGO("duckduckgo", "https://duckduckgo.com/"),
        QWANT("qwant", "https://www.qwant.com/"),
        SPOT("spot", "https://spot.murena.io/"),
        MOJEEK("mojeek", "https://www.mojeek.com/")
    }
}
