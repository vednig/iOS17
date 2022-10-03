package foundation.e.blisslauncher.features.suggestions;

import foundation.e.blisslauncher.core.database.model.LauncherItem;
import java.util.List;

public class SuggestionsResult {

    public static final int TYPE_LAUNCHER_ITEM = 567;
    public static final int TYPE_NETWORK_ITEM = 568;

    private List<String> networkItems;
    private List<LauncherItem> launcherItems;
    public String queryText;
    public int type = -1;

    public SuggestionsResult(String queryText) {
        this.queryText = queryText;
    }

    public List<String> getNetworkItems() {
        return networkItems;
    }

    public void setNetworkItems(List<String> networkItems) {
        this.networkItems = networkItems;
        this.type = TYPE_NETWORK_ITEM;
    }

    public List<LauncherItem> getLauncherItems() {
        return launcherItems;
    }

    public void setLauncherItems(List<LauncherItem> launcherItems) {
        this.launcherItems = launcherItems;
        this.type = TYPE_LAUNCHER_ITEM;
    }
}
