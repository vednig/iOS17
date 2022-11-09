package foundation.e.blisslauncher.features.suggestions;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class SearchSuggestionUtil {

    private static final String SPOT_URL = "https://spot.murena.io/";

    public SuggestionProvider getSuggestionProvider(Context context) {
        String defaultSearchEngine = defaultSearchEngine(context);
        if (defaultSearchEngine != null && defaultSearchEngine.length() > 0) {
            defaultSearchEngine = defaultSearchEngine.toLowerCase();
            if (defaultSearchEngine.contains("qwant")) {
                return new QwantProvider();
            } else {
                return new DuckDuckGoProvider();
            }
        } else {
            return new DuckDuckGoProvider();
        }
    }

    public Uri getUriForQuery(Context context, String query) {
        String defaultSearchEngine = defaultSearchEngine(context);
        if (defaultSearchEngine != null && defaultSearchEngine.length() > 0) {
            defaultSearchEngine = defaultSearchEngine.toLowerCase();
            if (defaultSearchEngine.contains("qwant")) {
                return Uri.parse("https://www.qwant.com/?q=" + query);
            } else if (defaultSearchEngine.contains("duckduckgo")) {
                return Uri.parse("https://duckduckgo.com/?q=" + query);
            } else {
                return Uri.parse(SPOT_URL + "?q=" + query);
            }
        } else {
            return Uri.parse(SPOT_URL + "?q=" + query);
        }
    }

    private String defaultSearchEngine(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://foundation.e.browser.provider").buildUpon().appendPath("search_engine").build();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return "";
        }
    }
}
