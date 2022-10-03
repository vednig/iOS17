package foundation.e.blisslauncher.features.widgets;

import foundation.e.blisslauncher.core.customviews.RoundedWidgetView;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class WidgetManager {
    private static final WidgetManager ourInstance = new WidgetManager();

    private Queue<Integer> removeWidgetIds = new LinkedList<>();
    private Queue<RoundedWidgetView> addWidgetViews = new LinkedList<>();

    public static WidgetManager getInstance() {
        return ourInstance;
    }

    private WidgetManager() {
    }

    public void enqueueRemoveId(int id) {
        // If the widget is not yet created but scheduled to be created we have to
        // prevent the
        // creation, too.
        Iterator<RoundedWidgetView> it = addWidgetViews.iterator();
        while (it.hasNext()) {
            RoundedWidgetView view = it.next();
            if (id == view.getAppWidgetId()) {
                addWidgetViews.remove(view);
                break;
            }
        }
        removeWidgetIds.add(id);
    }

    public void enqueueAddWidget(RoundedWidgetView view) {
        addWidgetViews.add(view);
    }

    public Integer dequeRemoveId() {
        return removeWidgetIds.poll();
    }

    public RoundedWidgetView dequeAddWidgetView() {
        return addWidgetViews.poll();
    }
}
