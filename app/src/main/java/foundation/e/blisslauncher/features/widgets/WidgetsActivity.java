package foundation.e.blisslauncher.features.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import foundation.e.blisslauncher.BlissLauncher;
import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.customviews.RoundedWidgetView;
import foundation.e.blisslauncher.core.customviews.WidgetHost;
import foundation.e.blisslauncher.core.database.DatabaseManager;
import foundation.e.blisslauncher.core.database.model.WidgetItem;
import foundation.e.blisslauncher.core.executors.AppExecutors;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class WidgetsActivity extends Activity implements AddedWidgetsAdapter.OnActionClickListener {

    private AddedWidgetsAdapter mAddedWidgetsAdapter;

    private AppWidgetManager mAppWidgetManager;
    private WidgetHost mAppWidgetHost;

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private static final int REQUEST_PICK_APPWIDGET = 455;
    private static final int REQUEST_CREATE_APPWIDGET = 189;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widgets);

        mAppWidgetManager = BlissLauncher.getApplication(this).getAppWidgetManager();
        mAppWidgetHost = BlissLauncher.getApplication(this).getAppWidgetHost();

        RecyclerView addedWidgets = findViewById(R.id.added_widgets_recycler_view);
        addedWidgets.setLayoutManager(new LinearLayoutManager(this));
        addedWidgets.setHasFixedSize(false);
        addedWidgets.setNestedScrollingEnabled(false);
        addedWidgets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mAddedWidgetsAdapter = new AddedWidgetsAdapter(this, metrics.densityDpi);
        addedWidgets.setAdapter(mAddedWidgetsAdapter);

        refreshRecyclerView();

        findViewById(R.id.add_widget_button).setOnClickListener(view -> selectWidget());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    private void refreshRecyclerView() {
        int[] widgetIds = mAppWidgetHost.getAppWidgetIds();
        mCompositeDisposable.add(DatabaseManager.getManager(this).getWidgets(widgetIds)
                .subscribeOn(Schedulers.from(AppExecutors.getInstance().diskIO()))
                .observeOn(AndroidSchedulers.mainThread()).subscribe(this::setWidgetItems));
    }

    private void setWidgetItems(List<WidgetItem> widgetItems) {
        List<Widget> widgets = new ArrayList<>();
        for (WidgetItem item : widgetItems) {
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(item.id);
            if (appWidgetInfo != null) {
                Widget widget = new Widget();
                widget.id = item.id;
                widget.info = appWidgetInfo;
                widgets.add(widget);
            }
        }
        mAddedWidgetsAdapter.setAppWidgetProviderInfos(widgets);
    }

    @Override
    public void removeWidget(int id) {
        mAppWidgetHost.deleteAppWidgetId(id);
        WidgetManager.getInstance().enqueueRemoveId(id);
    }

    void selectWidget() {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(this, WidgetPicker.class);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    void addEmptyData(Intent pickIntent) {
        ArrayList customInfo = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList customExtras = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                removeWidget(appWidgetId);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo != null && appWidgetInfo.configure != null) {
            /*
             * Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
             * intent.setComponent(appWidgetInfo.configure);
             * intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
             * startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
             */
            startAppWidgetConfigureActivitySafely(appWidgetId);
        } else {
            createWidget(data);
        }
    }

    public void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        RoundedWidgetView hostView = (RoundedWidgetView) mAppWidgetHost.createView(getApplicationContext(), appWidgetId,
                appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        WidgetManager.getInstance().enqueueAddWidget(hostView);
        refreshRecyclerView();
    }

    void startAppWidgetConfigureActivitySafely(int appWidgetId) {
        try {
            mAppWidgetHost.startAppWidgetConfigureActivityForResult(this, appWidgetId, 0, REQUEST_CREATE_APPWIDGET,
                    null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }
    }
}
