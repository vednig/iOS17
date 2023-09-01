package com.cloudx.ios17.features.launcher;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudx.ios17.core.customviews.BlissFrameLayout;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.core.customviews.BlissFrameLayout;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.R;
import com.cloudx.ios17.core.customviews.BlissFrameLayout;
import com.cloudx.ios17.core.database.model.LauncherItem;
import com.cloudx.ios17.features.suggestions.AutoCompleteAdapter;
import com.cloudx.ios17.features.suggestions.SuggestionsResult;
import com.cloudx.ios17.core.customviews.BlissFrameLayout;
import com.cloudx.ios17.core.database.model.LauncherItem;
import io.reactivex.observers.DisposableObserver;
import java.util.ArrayList;

public class SearchInputDisposableObserver extends DisposableObserver<SuggestionsResult> {

    private AutoCompleteAdapter networkSuggestionAdapter;
    private LauncherActivity launcherActivity;
    private ViewGroup appSuggestionsViewGroup;

    public SearchInputDisposableObserver(LauncherActivity activity, RecyclerView.Adapter autoCompleteAdapter,
            ViewGroup viewGroup) {
        this.networkSuggestionAdapter = (AutoCompleteAdapter) autoCompleteAdapter;
        this.launcherActivity = activity;
        this.appSuggestionsViewGroup = viewGroup;
    }

    @Override
    public void onNext(SuggestionsResult suggestionsResults) {
        if (suggestionsResults.type == SuggestionsResult.TYPE_NETWORK_ITEM) {
            networkSuggestionAdapter.updateSuggestions(suggestionsResults.getNetworkItems(),
                    suggestionsResults.queryText);
        } else if (suggestionsResults.type == SuggestionsResult.TYPE_LAUNCHER_ITEM) {
            ((ViewGroup) appSuggestionsViewGroup.findViewById(R.id.suggestedAppGrid)).removeAllViews();
            appSuggestionsViewGroup.findViewById(R.id.openUsageAccessSettings).setVisibility(View.GONE);
            appSuggestionsViewGroup.findViewById(R.id.suggestedAppGrid).setVisibility(View.VISIBLE);
            for (LauncherItem launcherItem : suggestionsResults.getLauncherItems()) {
                BlissFrameLayout blissFrameLayout = launcherActivity.prepareSuggestedApp(launcherItem);
                launcherActivity.addAppToGrid(appSuggestionsViewGroup.findViewById(R.id.suggestedAppGrid),
                        blissFrameLayout);
            }
        } else {
            launcherActivity.refreshSuggestedApps(appSuggestionsViewGroup, true);
            networkSuggestionAdapter.updateSuggestions(new ArrayList<>(), suggestionsResults.queryText);
        }
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {
    }
}
