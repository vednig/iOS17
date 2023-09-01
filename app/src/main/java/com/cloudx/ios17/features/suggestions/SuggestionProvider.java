package com.cloudx.ios17.features.suggestions;

import io.reactivex.Single;

public interface SuggestionProvider {

    Single<SuggestionsResult> query(String query);
}
