package foundation.e.blisslauncher.features.launcher;

import static android.content.pm.ActivityInfo.CONFIG_ORIENTATION;
import static android.content.pm.ActivityInfo.CONFIG_SCREEN_SIZE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.app.usage.UsageStats;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.StrictMode;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jakewharton.rxbinding3.widget.RxTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import foundation.e.blisslauncher.BlissLauncher;
import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.Alarm;
import foundation.e.blisslauncher.core.DeviceProfile;
import foundation.e.blisslauncher.core.Preferences;
import foundation.e.blisslauncher.core.Utilities;
import foundation.e.blisslauncher.core.blur.BlurWallpaperProvider;
import foundation.e.blisslauncher.core.broadcast.ManagedProfileBroadcastReceiver;
import foundation.e.blisslauncher.core.broadcast.TimeChangeBroadcastReceiver;
import foundation.e.blisslauncher.core.broadcast.WallpaperChangeReceiver;
import foundation.e.blisslauncher.core.customviews.BlissDragShadowBuilder;
import foundation.e.blisslauncher.core.customviews.BlissFrameLayout;
import foundation.e.blisslauncher.core.customviews.BlissInput;
import foundation.e.blisslauncher.core.customviews.DockGridLayout;
import foundation.e.blisslauncher.core.customviews.HorizontalPager;
import foundation.e.blisslauncher.core.customviews.InsettableFrameLayout;
import foundation.e.blisslauncher.core.customviews.InsettableRelativeLayout;
import foundation.e.blisslauncher.core.customviews.PageIndicatorLinearLayout;
import foundation.e.blisslauncher.core.customviews.RoundedWidgetView;
import foundation.e.blisslauncher.core.customviews.SquareFrameLayout;
import foundation.e.blisslauncher.core.customviews.SquareImageView;
import foundation.e.blisslauncher.core.customviews.WidgetHost;
import foundation.e.blisslauncher.core.database.DatabaseManager;
import foundation.e.blisslauncher.core.database.model.ApplicationItem;
import foundation.e.blisslauncher.core.database.model.CalendarIcon;
import foundation.e.blisslauncher.core.database.model.FolderItem;
import foundation.e.blisslauncher.core.database.model.LauncherItem;
import foundation.e.blisslauncher.core.database.model.ShortcutItem;
import foundation.e.blisslauncher.core.database.model.WidgetItem;
import foundation.e.blisslauncher.core.events.AppAddEvent;
import foundation.e.blisslauncher.core.events.AppChangeEvent;
import foundation.e.blisslauncher.core.events.AppRemoveEvent;
import foundation.e.blisslauncher.core.events.EventRelay;
import foundation.e.blisslauncher.core.events.ShortcutAddEvent;
import foundation.e.blisslauncher.core.executors.AppExecutors;
import foundation.e.blisslauncher.core.utils.AppUtils;
import foundation.e.blisslauncher.core.utils.Constants;
import foundation.e.blisslauncher.core.utils.DepthManager;
import foundation.e.blisslauncher.core.utils.GraphicsUtil;
import foundation.e.blisslauncher.core.utils.ThemesKt;
import foundation.e.blisslauncher.core.utils.UserHandle;
import foundation.e.blisslauncher.core.wallpaper.WallpaperManagerCompat;
import foundation.e.blisslauncher.features.notification.NotificationRepository;
import foundation.e.blisslauncher.features.notification.NotificationService;
import foundation.e.blisslauncher.features.shortcuts.DeepShortcutManager;
import foundation.e.blisslauncher.features.shortcuts.ShortcutKey;
import foundation.e.blisslauncher.features.suggestions.AutoCompleteAdapter;
import foundation.e.blisslauncher.features.suggestions.SearchSuggestionUtil;
import foundation.e.blisslauncher.features.suggestions.SuggestionProvider;
import foundation.e.blisslauncher.features.suggestions.SuggestionsResult;
import foundation.e.blisslauncher.features.usagestats.AppUsageStats;
import foundation.e.blisslauncher.features.weather.DeviceStatusService;
import foundation.e.blisslauncher.features.weather.WeatherPreferences;
import foundation.e.blisslauncher.features.weather.WeatherSourceListenerService;
import foundation.e.blisslauncher.features.weather.WeatherUpdateService;
import foundation.e.blisslauncher.features.weather.WeatherUtils;
import foundation.e.blisslauncher.features.widgets.DefaultWidgets;
import foundation.e.blisslauncher.features.widgets.WidgetManager;
import foundation.e.blisslauncher.features.widgets.WidgetViewBuilder;
import foundation.e.blisslauncher.features.widgets.WidgetsActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.relex.circleindicator.CircleIndicator;

public class LauncherActivity extends AppCompatActivity
        implements
            AutoCompleteAdapter.OnSuggestionClickListener,
            OnSwipeDownListener,
            WallpaperManagerCompat.OnColorsChangedListener {

    private static final int WIDGET_PAGE = 0;
    public static final int REORDER_TIMEOUT = 350;
    private final static int EMPTY_LOCATION_DRAG = -999;
    private static final int REQUEST_PERMISSION_CALL_PHONE = 14;
    private static final int REQUEST_LOCATION_SOURCE_SETTING = 267;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 586;
    public static final String ACTION_LAUNCHER_RESUME = "foundation.e.blisslauncher.LauncherActivity.LAUNCHER_RESUME";
    public static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
    public static final String EXTRA_SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args";
    public static final String NOTIFICATION_SETTING = "enabled_notification_listeners";

    private ContentResolver contentResolver;
    private String permissionString;
    private BroadcastReceiver unlockReceiver;
    private ComponentName notificationComponentName;

    public static boolean longPressed;
    private final Alarm mReorderAlarm = new Alarm();
    private final Alarm mDockReorderAlarm = new Alarm();
    private HorizontalPager mHorizontalPager;
    private DockGridLayout mDock;
    private PageIndicatorLinearLayout mIndicator;
    private ViewGroup mFolderWindowContainer;
    private ViewPager mFolderAppsViewPager;
    private BlissInput mFolderTitleInput;
    private BlissInput mSearchInput;
    private View mProgressBar;
    private int currentPageNumber = 0;
    private float maxDistanceForFolderCreation;
    private List<GridLayout> pages;
    private boolean dragDropEnabled = true;
    private BlissFrameLayout movingApp;
    private BlissFrameLayout collidingApp;
    private boolean folderInterest;
    private Animation wobbleAnimation;
    private Animation wobbleReverseAnimation;
    private int scrollCorner;
    private int parentPage = -99;
    private boolean folderFromDock;
    private boolean isWobbling = false;
    private CompositeDisposable mCompositeDisposable;
    private CountDownTimer mWobblingCountDownTimer;
    private List<BlissFrameLayout> mCalendarIcons = new ArrayList<>();
    private TimeChangeBroadcastReceiver timeChangedReceiver;
    private boolean isUiDone = false;
    private Set<String> mAppsWithNotifications = new HashSet<>();

    private View mLauncherView;
    private DeviceProfile mDeviceProfile;
    private boolean mLongClickStartsDrag = true;
    private boolean isDragging;
    private BlissDragShadowBuilder dragShadowBuilder;
    private boolean allAppsDisplayed;
    private boolean forceRefreshSuggestedApps = false;

    private List<ApplicationItem> mSuggestedApps = new ArrayList<>();
    private FrameLayout swipeSearchContainer;
    private InsettableRelativeLayout workspace;
    private View blurLayer; // Blur layer for folders and search container.

    private FolderItem activeFolder;
    private BlissFrameLayout activeFolderView;
    private int activeDot;

    private static final String TAG = "LauncherActivity";
    private AppWidgetManager mAppWidgetManager;
    private WidgetHost mAppWidgetHost;
    private LinearLayout widgetContainer;

    private FrameLayout widgetsPage;
    private SearchInputDisposableObserver searchDisposableObserver;
    private AnimatorSet currentAnimator;
    private Rect startBounds;
    private Rect finalBounds;
    private float startScaleFinal;
    private boolean showSwipeSearch;
    private RoundedWidgetView activeRoundedWidgetView;

    // EventRelay to handle pass events related to app addition, deletion or
    // changed.
    private EventRelay events;
    private ManagedProfileBroadcastReceiver managedProfileReceiver;

    private int moveTo;
    private Configuration oldConfig;
    private WallpaperChangeReceiver wallpaperChangeReceiver;
    private GestureDetectorCompat mDetector;
    private AlertDialog enableLocationDialog;

    private DepthManager mDepthManager;

    private int mThemeRes = R.style.HomeScreenTheme;
    private LayoutInflater mLightLayoutInflater;

    private WindowInsetsControllerCompat mInsetsController;

    public static LauncherActivity getLauncher(Context context) {
        if (context instanceof LauncherActivity) {
            return (LauncherActivity) context;
        }
        return ((LauncherActivity) ((ContextWrapper) context).getBaseContext());
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareBroadcastReceivers();

        WallpaperManagerCompat.Companion.getInstance(this).addOnChangeListener(this);
        int themeRes = ThemesKt.getActivityThemeRes(this);
        if (themeRes != mThemeRes) {
            mThemeRes = themeRes;
            setTheme(themeRes);
        }

        contentResolver = getContentResolver();
        permissionString = Settings.Secure.getString(contentResolver, NOTIFICATION_SETTING);
        notificationComponentName = new ComponentName(this, NotificationService.class);

        // Broadcast receiver to handle notification badges permission when screen is
        // unlocked
        unlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (Intent.ACTION_USER_PRESENT.equalsIgnoreCase(action)
                        && Preferences.shouldShowNotificationDialog(context)) {
                    if (permissionString == null || !permissionString.contains(getPackageName())) {
                        showNotifPermissionDeniedDialog();
                    }
                }
            }
        };

        oldConfig = new Configuration(getResources().getConfiguration());
        mDeviceProfile = BlissLauncher.getApplication(this).getDeviceProfile();

        mAppWidgetManager = BlissLauncher.getApplication(this).getAppWidgetManager();
        mAppWidgetHost = BlissLauncher.getApplication(this).getAppWidgetHost();

        mLauncherView = LayoutInflater.from(this).inflate(foundation.e.blisslauncher.R.layout.activity_main, null);

        // BlissLauncher.getApplication(LauncherActivity.this).getAppProvider().reload();

        setContentView(mLauncherView);
        setupViews();

        WallpaperManager wm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
        wm.suggestDesiredDimensions(mDeviceProfile.widthPx, mDeviceProfile.heightPx);

        mProgressBar.setVisibility(View.VISIBLE);

        registerUnlockBroadcastReceiver();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE);
        }

        // Start NotificationService to add count badge to Icons
        NotificationService.requestRebind(new ComponentName(this, NotificationService.class));

        createOrUpdateIconGrid();
        addDefaultWidgets();

        if (Utilities.ATLEAST_R) {
            mDepthManager = new DepthManager(this);
        }

        if (ThemesKt.isWorkspaceDarkText(this)) {
            int flags = mLauncherView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            mLauncherView.setSystemUiVisibility(flags);
        }
        final ContextThemeWrapper lightContext = new ContextThemeWrapper(this, R.style.HomeScreenTheme);
        mLightLayoutInflater = getLayoutInflater().cloneInContext(lightContext);

        mInsetsController = new WindowInsetsControllerCompat(getWindow(), mLauncherView);
    }

    public void registerUnlockBroadcastReceiver() {
        IntentFilter unlockFilter = new IntentFilter();
        unlockFilter.addAction(Intent.ACTION_USER_PRESENT);
        this.registerReceiver(unlockReceiver, unlockFilter);
    }

    public View getRootView() {
        return mLauncherView;
    }

    private void setupViews() {
        workspace = mLauncherView.findViewById(R.id.workspace);
        wallpaperChangeReceiver = new WallpaperChangeReceiver(workspace);
        workspace.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                wallpaperChangeReceiver.setWindowToken(v.getWindowToken());
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                wallpaperChangeReceiver.setWindowToken(null);
            }
        });
        mHorizontalPager = mLauncherView.findViewById(R.id.pages_container);
        blurLayer = mLauncherView.findViewById(R.id.blur_layer);
        blurLayer.setAlpha(0f);

        mDock = mLauncherView.findViewById(R.id.dock);
        mIndicator = mLauncherView.findViewById(R.id.page_indicator);
        mFolderWindowContainer = mLauncherView.findViewById(R.id.folder_window_container);
        mFolderAppsViewPager = mLauncherView.findViewById(R.id.folder_apps);
        mFolderTitleInput = mLauncherView.findViewById(R.id.folder_title);
        mProgressBar = mLauncherView.findViewById(R.id.progressbar);
        swipeSearchContainer = mLauncherView.findViewById(R.id.swipe_search_container);
        maxDistanceForFolderCreation = (int) (0.45f * mDeviceProfile.iconSizePx);

        scrollCorner = mDeviceProfile.iconDrawablePaddingPx / 2;

        wobbleAnimation = AnimationUtils.loadAnimation(this, R.anim.wobble);
        wobbleReverseAnimation = AnimationUtils.loadAnimation(this, R.anim.wobble_reverse);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        workspace.setOnClickListener(v -> {
            if (swipeSearchContainer.getVisibility() == VISIBLE) {
                hideSwipeSearchContainer();
            }
        });
    }

    private void createOrUpdateIconGrid() {
        getCompositeDisposable().add(BlissLauncher.getApplication(this).getAppProvider().getAppsRepository()
                .getAppsRelay().distinctUntilChanged().observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<LauncherItem>>() {
                    @Override
                    public void onNext(List<LauncherItem> launcherItems) {
                        if (launcherItems == null || launcherItems.size() <= 0) {
                            BlissLauncher.getApplication(LauncherActivity.this).getAppProvider().reload(true);
                        } else if (!allAppsDisplayed) {
                            showApps(launcherItems);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    private void addDefaultWidgets() {
        int[] widgetIds = mAppWidgetHost.getAppWidgetIds();
        Set<ComponentName> existingProviders = new HashSet<>();
        for (int widgetId : widgetIds) {
            AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(widgetId);
            if (info == null)
                continue;
            existingProviders.add(info.provider);
        }

        if (!Preferences.getAddedEcloudWidget(this)) {
            ComponentName provider = DefaultWidgets.INSTANCE.getEcloudWidget();
            if (existingProviders.contains(provider) || allocateAndBindWidget(provider)) {
                Preferences.setAddedEcloudWidget(this);
            }
        }

        if (!Preferences.getAddedPrivacyWidget(this)) {
            ComponentName provider = DefaultWidgets.INSTANCE.getPrivacyWidget();
            if (existingProviders.contains(provider) || allocateAndBindWidget(provider)) {
                Preferences.setAddedPrivacyWidget(this);
            }
        }

        if (!Preferences.getAddedWeatherWidget(this)) {
            ComponentName provider = DefaultWidgets.INSTANCE.getWeatherWidget();
            if (existingProviders.contains(provider) || allocateAndBindWidget(provider)) {
                Preferences.setAddedWeatherWidget(this);
            }
        }
    }

    private boolean allocateAndBindWidget(ComponentName provider) {
        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        if (!mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider)) {
            mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            return false;
        }
        WidgetItem widgetItem = new WidgetItem(appWidgetId);
        widgetItem.order = 0;
        DatabaseManager.getManager(this).insertWidget(widgetItem);
        return true;
    }

    private void prepareBroadcastReceivers() {
        timeChangedReceiver = TimeChangeBroadcastReceiver.register(this);
        managedProfileReceiver = ManagedProfileBroadcastReceiver.register(this);
    }

    public CompositeDisposable getCompositeDisposable() {
        if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
            mCompositeDisposable = new CompositeDisposable();
        }
        return mCompositeDisposable;
    }

    public void updateAllCalendarIcons(Calendar calendar) {
        for (BlissFrameLayout blissIcon : mCalendarIcons) {
            CalendarIcon calendarIcon = new CalendarIcon(blissIcon.findViewById(R.id.calendar_month_textview),
                    blissIcon.findViewById(R.id.calendar_date_textview));
            updateCalendarIcon(calendarIcon, calendar);
        }
    }

    private void updateCalendarIcon(CalendarIcon calendarIcon, Calendar calendar) {
        calendarIcon.monthTextView.setText(Utilities.convertMonthToString());
        calendarIcon.dayTextView.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Preferences.shouldAskForNotificationAccess(this)) {
            if (Preferences.shouldShowNotificationDialog(this)) {
                showNotifPermissionDeniedDialog();
                if (unlockReceiver != null) {
                    this.unregisterReceiver(unlockReceiver);
                }
            }

            if (permissionString == null) {
                permissionString = "";
            } else {
                permissionString += ":";
            }

            permissionString += notificationComponentName.flattenToString();
        }

        if (mDepthManager != null) {
            mDepthManager.updateDepth();
        }

        if (widgetsPage != null) {
            refreshSuggestedApps(widgetsPage, forceRefreshSuggestedApps);
        }

        final Intent resumeIntent = new Intent(ACTION_LAUNCHER_RESUME);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resumeIntent);

        if (widgetContainer != null) {
            WidgetManager widgetManager = WidgetManager.getInstance();
            Integer id = widgetManager.dequeRemoveId();
            while (id != null) {
                for (int i = 0; i < widgetContainer.getChildCount(); i++) {
                    if (widgetContainer.getChildAt(i) instanceof RoundedWidgetView) {
                        RoundedWidgetView appWidgetHostView = (RoundedWidgetView) widgetContainer.getChildAt(i);
                        if (appWidgetHostView.getAppWidgetId() == id) {
                            widgetContainer.removeViewAt(i);
                            DatabaseManager.getManager(this).removeWidget(id);
                            break;
                        }
                    }
                }
                id = widgetManager.dequeRemoveId();
            }

            RoundedWidgetView widgetView = widgetManager.dequeAddWidgetView();
            while (widgetView != null) {
                widgetView = WidgetViewBuilder.create(this, widgetView);
                addWidgetToContainer(widgetView);
                DatabaseManager.getManager(this).insertWidget(new WidgetItem(widgetView.getAppWidgetId()));
                widgetView = widgetManager.dequeAddWidgetView();
            }
        }
    }

    private void addWidgetToContainer(RoundedWidgetView widgetView) {
        widgetContainer.addView(widgetView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (widgetsPage != null) {
            hideWidgetResizeContainer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Unregister active receivers
        TimeChangeBroadcastReceiver.unregister(this, timeChangedReceiver);
        ManagedProfileBroadcastReceiver.unregister(this, managedProfileReceiver);

        // Dispose CompositeDisposable
        getCompositeDisposable().dispose();

        // Unsubscribe to events
        if (events != null) {
            events.unsubscribe();
        }

        // Dismiss Dialog if currently visible to prevent view leakage.
        if (enableLocationDialog != null && enableLocationDialog.isShowing()) {
            enableLocationDialog.dismiss();
        }

        // Clear AppProvider
        BlissLauncher.getApplication(this).getAppProvider().clear();

        WallpaperManagerCompat.Companion.getInstance(this).removeOnChangeListener(this);

        // Handover to android system.
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int diff = newConfig.diff(oldConfig);

        // Don't consider any other config changes as of now.
        if ((diff & (CONFIG_ORIENTATION | CONFIG_SCREEN_SIZE)) != 0) {
            recreate();
        }
        oldConfig.setTo(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    public void onAppAddEvent(AppAddEvent appAddEvent) {
        updateOrAddApp(appAddEvent.getPackageName(), appAddEvent.getUserHandle());
        // DatabaseManager.getManager(this).saveLayouts(pages, mDock);
        if (moveTo != -1) {
            mHorizontalPager.setCurrentPage(moveTo);
            moveTo = -1;
        }
    }

    public void onAppRemoveEvent(AppRemoveEvent appRemoveEvent) {
        forceRefreshSuggestedApps = true;
        removePackageFromLauncher(appRemoveEvent.getPackageName(), appRemoveEvent.getUserHandle());
        DatabaseManager.getManager(this).saveLayouts(pages, mDock);
        rebindAllWidgets();
    }

    public void onAppChangeEvent(AppChangeEvent appChangeEvent) {
        updateOrAddApp(appChangeEvent.getPackageName(), appChangeEvent.getUserHandle());
        DatabaseManager.getManager(this).saveLayouts(pages, mDock);
    }

    public void onShortcutAddEvent(ShortcutAddEvent shortcutAddEvent) {
        moveTo = -1;
        updateOrAddShortcut(shortcutAddEvent.getShortcutItem());
        DatabaseManager.getManager(this).saveLayouts(pages, mDock);
        Toast.makeText(this, getString(R.string.toast_shortcut_added), Toast.LENGTH_SHORT).show();
        if (moveTo != -1) {
            mHorizontalPager.setCurrentPage(moveTo);
            moveTo = -1;
        }
    }

    private void addLauncherItem(LauncherItem launcherItem) {
        if (pages == null || pages.size() == 0) {
            return;
        }
        if (launcherItem != null) {

            BlissFrameLayout view = prepareLauncherItem(launcherItem);

            int current = 0;
            while ((current < pages.size() && pages.get(current).getChildCount() == mDeviceProfile.maxAppsPerPage)) {
                current++;
            }

            if (current == pages.size()) {
                pages.add(preparePage());
                ImageView dot = new ImageView(LauncherActivity.this);
                dot.setImageDrawable(getDrawable(R.drawable.dot_off));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.dotSize),
                        getResources().getDimensionPixelSize(R.dimen.dotSize));
                dot.setLayoutParams(params);
                mIndicator.addView(dot);
                mHorizontalPager.addView(pages.get(current));
            }
            launcherItem.screenId = current;
            launcherItem.cell = pages.get(current).getChildCount() - 1;
            launcherItem.container = Constants.CONTAINER_DESKTOP;
            addAppToGrid(pages.get(current), view);
            moveTo = current + 1;
        }
    }

    private void updateOrAddShortcut(ShortcutItem shortcutItem) {
        if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < mFolderAppsViewPager.getChildCount(); i++) {
                GridLayout gridLayout = (GridLayout) mFolderAppsViewPager.getChildAt(i);
                for (int j = 0; j < gridLayout.getChildCount(); j++) {
                    BlissFrameLayout viewGroup = (BlissFrameLayout) gridLayout.getChildAt(j);
                    final LauncherItem existingItem = getAppDetails(viewGroup);
                    if (existingItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                        ShortcutItem existingShortcutItem = (ShortcutItem) existingItem;
                        if (existingShortcutItem.id.equalsIgnoreCase(shortcutItem.id)) {
                            BlissFrameLayout blissFrameLayout = prepareLauncherItem(shortcutItem);
                            GridLayout.LayoutParams iconLayoutParams = new GridLayout.LayoutParams();
                            iconLayoutParams.height = mDeviceProfile.cellHeightPx;
                            iconLayoutParams.width = mDeviceProfile.cellWidthPx;
                            gridLayout.removeViewAt(j);
                            gridLayout.addView(blissFrameLayout, j, iconLayoutParams);
                            return;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < mDock.getChildCount(); i++) {
            BlissFrameLayout viewGroup = (BlissFrameLayout) mDock.getChildAt(i);
            LauncherItem launcherItem = getAppDetails(viewGroup);
            if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                FolderItem folderItem = (FolderItem) launcherItem;
                for (int k = 0; k < folderItem.items.size(); k++) {
                    if (folderItem.items.get(k).itemType == Constants.ITEM_TYPE_SHORTCUT) {
                        ShortcutItem existingShortcutItem = (ShortcutItem) folderItem.items.get(k);
                        if (existingShortcutItem.id.equalsIgnoreCase(shortcutItem.id)) {
                            folderItem.items.set(k, shortcutItem);
                            folderItem.icon = new GraphicsUtil(this).generateFolderIcon(this, folderItem);
                            BlissFrameLayout blissFrameLayout = prepareLauncherItem(launcherItem);
                            mDock.removeViewAt(i);
                            addAppToDock(blissFrameLayout, i);
                            return;
                        }
                    }

                }
            } else {
                if (launcherItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                    ShortcutItem existingShortcutItem = (ShortcutItem) launcherItem;
                    if (existingShortcutItem.id.equalsIgnoreCase(shortcutItem.id)) {
                        BlissFrameLayout blissFrameLayout = prepareLauncherItem(shortcutItem);
                        mDock.removeViewAt(i);
                        addAppToDock(blissFrameLayout, i);
                        return;
                    }
                }
            }
        }

        for (int i = 0; i < pages.size(); i++) {
            GridLayout gridLayout = pages.get(i);
            for (int j = 0; j < gridLayout.getChildCount(); j++) {
                BlissFrameLayout viewGroup = (BlissFrameLayout) gridLayout.getChildAt(j);
                LauncherItem launcherItem = getAppDetails(viewGroup);
                if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                    FolderItem folderItem = (FolderItem) launcherItem;
                    for (int k = 0; k < folderItem.items.size(); k++) {
                        if (folderItem.items.get(k).itemType == Constants.ITEM_TYPE_SHORTCUT) {
                            ShortcutItem existingShortcutItem = (ShortcutItem) folderItem.items.get(k);
                            if (existingShortcutItem.id.equalsIgnoreCase(shortcutItem.id)) {
                                folderItem.items.set(k, shortcutItem);
                                folderItem.icon = new GraphicsUtil(this).generateFolderIcon(this, folderItem);
                                BlissFrameLayout blissFrameLayout = prepareLauncherItem(launcherItem);
                                gridLayout.removeViewAt(j);
                                addAppToGrid(gridLayout, blissFrameLayout, j);
                                moveTo = i + 1;
                                return;
                            }
                        }
                    }
                } else {
                    if (launcherItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                        ShortcutItem existingShortcutItem = (ShortcutItem) launcherItem;
                        if (existingShortcutItem.id.equalsIgnoreCase(shortcutItem.id)) {
                            BlissFrameLayout blissFrameLayout = prepareLauncherItem(shortcutItem);
                            gridLayout.removeViewAt(j);
                            addAppToGrid(gridLayout, blissFrameLayout, j);
                            moveTo = i + 1;
                            return;
                        }
                    }
                }
            }
        }

        addLauncherItem(shortcutItem);
    }

    private void removePackageFromLauncher(String packageName, UserHandle userHandle) {
        handleWobbling(false);
        if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < mFolderAppsViewPager.getChildCount(); i++) {
                GridLayout grid = (GridLayout) mFolderAppsViewPager.getChildAt(i);
                for (int j = 0; j < grid.getChildCount(); j++) {
                    LauncherItem launcherItem = getAppDetails(grid.getChildAt(j));
                    if (launcherItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
                        ApplicationItem app = (ApplicationItem) launcherItem;
                        if (app.packageName.equals(packageName) && app.user.isSameUser(userHandle)) {
                            activeFolder.items.remove(app);
                        }
                    } else if (launcherItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                        ShortcutItem shortcutItem = (ShortcutItem) launcherItem;
                        if (shortcutItem.packageName.equals(packageName)) {
                            activeFolder.items.remove(shortcutItem);
                        }
                    }
                }
            }
            updateFolder();
        }

        for (int j = 0; j < mDock.getChildCount(); j++) {
            LauncherItem appItem = getAppDetails(mDock.getChildAt(j));
            if (appItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                FolderItem folderItem = (FolderItem) appItem;
                for (LauncherItem item : folderItem.items) {
                    if (item.itemType == Constants.ITEM_TYPE_APPLICATION) {
                        ApplicationItem applicationItem = (ApplicationItem) item;
                        if (applicationItem.packageName.equalsIgnoreCase(packageName)
                                && applicationItem.user.isSameUser(userHandle)) {
                            folderItem.items.remove(applicationItem);
                        }
                    } else if (item.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                        ShortcutItem shortcutItem = (ShortcutItem) item;
                        if (shortcutItem.packageName.equalsIgnoreCase(packageName)) {
                            folderItem.items.remove(shortcutItem);
                        }
                    }
                }
                updateFolderInGrid(mDock, folderItem, j);
            } else if (appItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
                ApplicationItem applicationItem = (ApplicationItem) appItem;
                if (applicationItem.packageName.equalsIgnoreCase(packageName)
                        && applicationItem.user.isSameUser(userHandle)) {
                    mDock.removeViewAt(j);
                }
            } else if (appItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                ShortcutItem shortcutItem = (ShortcutItem) appItem;
                if (shortcutItem.packageName.equalsIgnoreCase(packageName)) {
                    mDock.removeViewAt(j);
                }
            }
        }

        for (int i = 0; i < pages.size(); i++) {
            GridLayout grid = getGridFromPage(pages.get(i));
            for (int j = 0; j < grid.getChildCount(); j++) {
                LauncherItem launcherItem = getAppDetails(grid.getChildAt(j));
                if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                    FolderItem folderItem = (FolderItem) launcherItem;
                    for (LauncherItem item : folderItem.items) {
                        if (item.itemType == Constants.ITEM_TYPE_APPLICATION) {
                            ApplicationItem applicationItem = (ApplicationItem) item;
                            if (applicationItem.packageName.equalsIgnoreCase(packageName)
                                    && applicationItem.user.isSameUser(userHandle)) {
                                folderItem.items.remove(applicationItem);
                            }
                        } else if (item.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                            ShortcutItem shortcutItem = (ShortcutItem) item;
                            if (shortcutItem.packageName.equalsIgnoreCase(packageName)) {
                                folderItem.items.remove(shortcutItem);
                            }
                        }
                    }
                    updateFolderInGrid(grid, folderItem, j);

                    if (grid.getChildCount() == 0) {
                        pages.remove(i);
                        mHorizontalPager.removeViewAt(i + 1);
                        if (i == pages.size()) {
                            mHorizontalPager.scrollLeft(100);
                        }
                        mIndicator.removeViewAt(i);
                        updateIndicator();
                    }
                } else if (launcherItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
                    ApplicationItem applicationItem = (ApplicationItem) launcherItem;
                    if (applicationItem.packageName.equalsIgnoreCase(packageName)
                            && applicationItem.user.isSameUser(userHandle)) {
                        grid.removeViewAt(j);
                        if (grid.getChildCount() == 0) {
                            pages.remove(i);
                            mHorizontalPager.removeViewAt(i + 1);
                            if (i == pages.size()) {
                                mHorizontalPager.scrollLeft(100);
                            }
                            mIndicator.removeViewAt(i);
                            updateIndicator();
                        }
                    }
                } else if (launcherItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                    ShortcutItem shortcutItem = (ShortcutItem) launcherItem;
                    if (shortcutItem.packageName.equalsIgnoreCase(packageName)) {
                        grid.removeViewAt(j);
                        if (grid.getChildCount() == 0) {
                            pages.remove(i);
                            mHorizontalPager.removeViewAt(i + 1);
                            if (i == pages.size()) {
                                mHorizontalPager.scrollLeft(100);
                            }
                            mIndicator.removeViewAt(i);
                            updateIndicator();
                        }
                    }
                }
            }
        }
    }

    private void updateFolder() {
        mFolderAppsViewPager.getAdapter().notifyDataSetChanged();
        if (activeFolder.items.size() == 0) {
            ((ViewGroup) activeFolderView.getParent()).removeView(activeFolderView);
            hideFolderWindowContainer();
        } else {
            if (activeFolder.items.size() == 1) {
                LauncherItem item = activeFolder.items.get(0);
                activeFolder.items.remove(item);
                mFolderAppsViewPager.getAdapter().notifyDataSetChanged();
                BlissFrameLayout view = prepareLauncherItem(item);

                if (folderFromDock) {
                    addAppToDock(view, mDock.indexOfChild(activeFolderView));
                } else {
                    GridLayout gridLayout = pages.get(getCurrentAppsPageNumber());
                    addAppToGrid(gridLayout, view, gridLayout.indexOfChild(activeFolderView));
                }

                ((ViewGroup) activeFolderView.getParent()).removeView(activeFolderView);
                hideFolderWindowContainer();
            } else {
                updateIcon(activeFolderView, activeFolder,
                        new GraphicsUtil(this).generateFolderIcon(this, activeFolder), folderFromDock);
                hideFolderWindowContainer();
            }
        }
    }

    private void updateFolderInGrid(GridLayout grid, FolderItem folderItem, int folderIndex) {
        if (folderItem.items.size() == 0) {
            grid.removeViewAt(folderIndex);
        } else {
            folderItem.icon = new GraphicsUtil(this).generateFolderIcon(this, folderItem);
            BlissFrameLayout blissFrameLayout = prepareLauncherItem(folderItem);
            grid.removeViewAt(folderIndex);
            if (grid instanceof DockGridLayout) {
                addAppToDock(blissFrameLayout, folderIndex);
            } else {
                addAppToGrid(grid, blissFrameLayout, folderIndex);
            }
        }
    }

    private void updateOrAddApp(String packageName, UserHandle userHandle) {
        handleWobbling(false);
        ApplicationItem updatedAppItem = AppUtils.createAppItem(this, packageName, userHandle);
        if (updatedAppItem == null) {
            removePackageFromLauncher(packageName, userHandle);
            return;
        }

        if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < mFolderAppsViewPager.getChildCount(); i++) {
                GridLayout gridLayout = (GridLayout) mFolderAppsViewPager.getChildAt(i);
                for (int j = 0; j < gridLayout.getChildCount(); j++) {
                    BlissFrameLayout viewGroup = (BlissFrameLayout) gridLayout.getChildAt(j);
                    final LauncherItem existingItem = getAppDetails(viewGroup);
                    if (existingItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
                        ApplicationItem existingAppItem = (ApplicationItem) existingItem;
                        if (existingAppItem.packageName.equalsIgnoreCase(packageName)
                                && existingAppItem.user.isSameUser(userHandle)) {
                            BlissFrameLayout blissFrameLayout = prepareLauncherItem(updatedAppItem);
                            GridLayout.LayoutParams iconLayoutParams = new GridLayout.LayoutParams();
                            iconLayoutParams.height = mDeviceProfile.cellHeightPx;
                            iconLayoutParams.width = mDeviceProfile.cellWidthPx;
                            gridLayout.removeViewAt(j);
                            gridLayout.addView(blissFrameLayout, j, iconLayoutParams);
                            return;
                        }
                    }

                }
            }
        }

        for (int i = 0; i < mDock.getChildCount(); i++) {
            BlissFrameLayout viewGroup = (BlissFrameLayout) mDock.getChildAt(i);
            LauncherItem existingAppItem = getAppDetails(viewGroup);
            if (existingAppItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                FolderItem folderItem = (FolderItem) existingAppItem;
                for (int k = 0; k < folderItem.items.size(); k++) {
                    if (folderItem.items.get(k).itemType == Constants.ITEM_TYPE_APPLICATION) {
                        ApplicationItem applicationItem = (ApplicationItem) folderItem.items.get(k);
                        if (applicationItem.packageName.equalsIgnoreCase(packageName)
                                && applicationItem.user.isSameUser(userHandle)) {
                            folderItem.items.set(k, updatedAppItem);
                            folderItem.icon = new GraphicsUtil(this).generateFolderIcon(this, folderItem);
                            BlissFrameLayout blissFrameLayout = prepareLauncherItem(existingAppItem);
                            mDock.removeViewAt(i);
                            addAppToDock(blissFrameLayout, i);
                            return;
                        }
                    }

                }
            } else {
                if (existingAppItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
                    ApplicationItem applicationItem = (ApplicationItem) existingAppItem;
                    if (applicationItem.packageName.equalsIgnoreCase(packageName)
                            && applicationItem.user.isSameUser(userHandle)) {
                        BlissFrameLayout blissFrameLayout = prepareLauncherItem(updatedAppItem);
                        mDock.removeViewAt(i);
                        addAppToDock(blissFrameLayout, i);
                        return;
                    }
                }
            }
        }

        for (int i = 0; i < pages.size(); i++) {
            GridLayout gridLayout = pages.get(i);
            for (int j = 0; j < gridLayout.getChildCount(); j++) {
                BlissFrameLayout viewGroup = (BlissFrameLayout) gridLayout.getChildAt(j);
                LauncherItem existingAppItem = getAppDetails(viewGroup);
                if (existingAppItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                    FolderItem folderItem = (FolderItem) existingAppItem;
                    for (int k = 0; k < folderItem.items.size(); k++) {
                        if (folderItem.items.get(k).itemType == Constants.ITEM_TYPE_APPLICATION) {
                            ApplicationItem applicationItem = (ApplicationItem) folderItem.items.get(k);
                            if (applicationItem.packageName.equalsIgnoreCase(packageName)
                                    && applicationItem.user.isSameUser(userHandle)) {
                                folderItem.items.set(k, updatedAppItem);
                                folderItem.icon = new GraphicsUtil(this).generateFolderIcon(this, folderItem);
                                BlissFrameLayout blissFrameLayout = prepareLauncherItem(existingAppItem);
                                gridLayout.removeViewAt(j);
                                addAppToGrid(gridLayout, blissFrameLayout, j);
                                moveTo = i + 1;
                                return;
                            }
                        }
                    }
                } else {
                    if (existingAppItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
                        ApplicationItem applicationItem = (ApplicationItem) existingAppItem;
                        if (applicationItem.packageName.equalsIgnoreCase(packageName)
                                && applicationItem.user.isSameUser(userHandle)) {
                            BlissFrameLayout blissFrameLayout = prepareLauncherItem(updatedAppItem);
                            gridLayout.removeViewAt(j);
                            addAppToGrid(gridLayout, blissFrameLayout, j);
                            moveTo = i + 1;
                            return;
                        }
                    }
                }
            }
        }

        addLauncherItem(updatedAppItem);
    }

    public void showApps(List<LauncherItem> launcherItems) {
        mProgressBar.setVisibility(GONE);
        if (isWobbling)
            handleWobbling(false);
        createUI(launcherItems);
        isUiDone = true;
        createPageChangeListener();
        createFolderTitleListener();
        createDragListener();
        createWidgetsPage();
        createIndicator();
        createOrUpdateBadgeCount();
        allAppsDisplayed = true;
        subscribeToEvents();
    }

    private void subscribeToEvents() {
        events = EventRelay.getInstance();
        events.subscribe(new EventsObserverImpl(this));
    }

    private void createOrUpdateBadgeCount() {
        getCompositeDisposable().add(NotificationRepository.getNotificationRepository().getNotifications()
                .subscribeWith(new DisposableObserver<Set<String>>() {
                    @Override
                    public void onNext(Set<String> packages) {
                        mAppsWithNotifications = packages;
                        updateBadges(mAppsWithNotifications);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(LauncherActivity.this, getString(R.string.toast_recreating_launcher),
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        recreate();
                    }

                    @Override
                    public void onComplete() {
                    }
                }));
    }

    private void updateBadges(Set<String> appsWithNotifications) {
        if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < mFolderAppsViewPager.getChildCount(); i++) {
                GridLayout gridLayout = (GridLayout) mFolderAppsViewPager.getChildAt(i);
                for (int j = 0; j < gridLayout.getChildCount(); j++) {
                    BlissFrameLayout viewGroup = (BlissFrameLayout) gridLayout.getChildAt(j);
                    final LauncherItem appItem = getAppDetails(viewGroup);
                    if (appItem.itemType != Constants.ITEM_TYPE_SHORTCUT) {
                        updateBadgeToApp(viewGroup, appItem, appsWithNotifications, true);
                    }
                }
            }
        }
        for (int i = 0; i < pages.size(); i++) {
            GridLayout gridLayout = pages.get(i);
            for (int j = 0; j < gridLayout.getChildCount(); j++) {
                BlissFrameLayout viewGroup = (BlissFrameLayout) gridLayout.getChildAt(j);
                final LauncherItem appItem = getAppDetails(viewGroup);
                if (appItem.itemType != Constants.ITEM_TYPE_SHORTCUT) {
                    updateBadgeToApp(viewGroup, appItem, appsWithNotifications, true);
                }
            }
        }

        for (int i = 0; i < mDock.getChildCount(); i++) {
            BlissFrameLayout viewGroup = (BlissFrameLayout) mDock.getChildAt(i);
            final LauncherItem appItem = getAppDetails(viewGroup);
            if (appItem.itemType != Constants.ITEM_TYPE_SHORTCUT) {
                updateBadgeToApp(viewGroup, appItem, appsWithNotifications, false);
            }
        }
    }

    private void updateBadgeToApp(BlissFrameLayout viewGroup, LauncherItem appItem, Set<String> appsWithNotifications,
            boolean withText) {
        if (appItem != null) {
            if (appItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                viewGroup.applyBadge(checkHasApp((FolderItem) appItem, appsWithNotifications), withText);
            } else {
                ApplicationItem applicationItem = (ApplicationItem) appItem;
                String pkgName = applicationItem.packageName;
                viewGroup.applyBadge(appsWithNotifications.contains(pkgName), withText);
            }
        }
    }

    private boolean checkHasApp(FolderItem appItem, Set<String> packages) {
        for (LauncherItem item : appItem.items) {
            if (item.itemType == Constants.ITEM_TYPE_APPLICATION) {
                ApplicationItem applicationItem = (ApplicationItem) item;
                if (packages.contains(applicationItem.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds a listener to the folder title. When clicked, the TextView transforms
     * into an EditText. When the user changes the title and presses the DONE
     * button, the EditText becomes a TextView again. This logic was necessary
     * because on API 24, permanently having an EditText in the widgetsPage was
     * breaking the drag/drop functionality.
     */
    private void createFolderTitleListener() {
        mFolderTitleInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
            mFolderTitleInput.setCursorVisible(true);
        });
        mFolderTitleInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateFolderTitle();
            }
            return false;
        });
        mFolderTitleInput.setOnClickListener(view -> mFolderTitleInput.setCursorVisible(true));
        mFolderWindowContainer.setOnClickListener(view -> returnToHomeScreen());
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.showSoftInput(view, 0);
    }

    private void updateFolderTitle() {
        String updatedTitle = mFolderTitleInput.getText().toString();
        activeFolder.title = updatedTitle;
        List<Object> tags = (List<Object>) activeFolderView.getTag();
        ((TextView) tags.get(1)).setText(updatedTitle);
        mFolderTitleInput.setText(updatedTitle);
        mFolderTitleInput.setCursorVisible(false);
    }

    /**
     * Adds a scroll listener to the mHorizontalPager in order to keep the
     * currentPageNumber updated
     */
    private void createPageChangeListener() {
        ValueAnimator navbarAnimator = createNavbarColorAnimator();

        mHorizontalPager.addOnScrollListener(new HorizontalPager.OnScrollListener() {
            boolean isViewScrolling = true;

            @Override
            public void onScroll(int scrollX) {
                float progress = (float) scrollX / mDeviceProfile.availableWidthPx;
                if (progress >= 0.999)
                    progress = 1;
                if (progress <= 0.001)
                    progress = 0;
                int dockHeight = mDock.getHeight() + mIndicator.getHeight();
                float dockTranslationY = (1 - progress) * dockHeight;
                mDock.setTranslationY(dockTranslationY);
                mIndicator.setTranslationY(dockTranslationY);

                if (scrollX >= 0 && scrollX < mDeviceProfile.availableWidthPx) {
                    float fraction = (float) (mDeviceProfile.availableWidthPx - scrollX)
                            / mDeviceProfile.availableWidthPx;
                    int radius = (int) (fraction * 18);
                    blurLayer.setAlpha(fraction);
                }
                if (isViewScrolling) {
                    dragDropEnabled = false;
                }
            }

            @Override
            public void onViewScrollFinished(int page) {
                isViewScrolling = false;

                blurLayer.setAlpha((page == 0 || mFolderWindowContainer.getVisibility() == VISIBLE) ? 1f : 0f);

                if (currentPageNumber != page) {
                    currentPageNumber = page;
                    navbarAnimator.cancel();
                    if (currentPageNumber == WIDGET_PAGE) {
                        navbarAnimator.start();
                        refreshSuggestedApps(widgetsPage, forceRefreshSuggestedApps);
                        mInsetsController.hide(WindowInsetsCompat.Type.statusBars());
                    } else {
                        navbarAnimator.reverse();
                        mInsetsController.show(WindowInsetsCompat.Type.statusBars());
                    }

                    dragDropEnabled = true;
                    updateIndicator();
                }
            }
        });
    }

    private ValueAnimator createNavbarColorAnimator() {
        int navColor = getWindow().getNavigationBarColor();
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), navColor,
                ColorUtils.setAlphaComponent(navColor, 160));

        colorAnimation.setDuration(400);
        colorAnimation.setInterpolator(new LinearInterpolator());
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                getWindow().setNavigationBarColor((int) animator.getAnimatedValue());
            }
        });

        return colorAnimation;
    }

    public void refreshSuggestedApps(ViewGroup viewGroup, boolean forceRefresh) {
        TextView openUsageAccessSettingsTv = viewGroup.findViewById(R.id.openUsageAccessSettings);
        GridLayout suggestedAppsGridLayout = viewGroup.findViewById(R.id.suggestedAppGrid);
        AppUsageStats appUsageStats = new AppUsageStats(this);
        List<UsageStats> usageStats = appUsageStats.getUsageStats();

        if (usageStats.size() == 0) {
            // no usage stats, show setup prompt
            openUsageAccessSettingsTv.setVisibility(VISIBLE);
            suggestedAppsGridLayout.setVisibility(GONE);
            mSuggestedApps = new ArrayList<>();
            return;
        }

        openUsageAccessSettingsTv.setVisibility(GONE);
        suggestedAppsGridLayout.setVisibility(VISIBLE);

        List<ApplicationItem> suggestedApps = usageStats.stream().map(UsageStats::getPackageName)
                .map(packageName -> AppUtils.createAppItem(this, packageName, new UserHandle()))
                .filter(Objects::nonNull).limit(4).collect(Collectors.toList());

        if (!forceRefresh && suggestedApps.equals(mSuggestedApps)) {
            // no changes, skip update
            return;
        }

        suggestedAppsGridLayout.removeAllViews();
        suggestedApps.stream().map(this::prepareSuggestedApp)
                .forEach(view -> addAppToGrid(suggestedAppsGridLayout, view));
        mSuggestedApps = suggestedApps;

        // remove force flag
        forceRefreshSuggestedApps = false;
    }

    /**
     * Populates the pages and the mDock for the first time.
     */
    private void createUI(List<LauncherItem> launcherItems) {
        mHorizontalPager.setUiCreated(false);
        mDock.setEnabled(false);

        pages = new ArrayList<>();

        int hotseatCell = 0;

        // Prepare first screen of workspace here.
        GridLayout workspaceScreen = preparePage();
        pages.add(workspaceScreen);

        mHorizontalPager.removeAllViews();
        mDock.removeAllViews();

        for (int i = 0; i < launcherItems.size(); i++) {
            LauncherItem launcherItem = launcherItems.get(i);
            BlissFrameLayout appView = prepareLauncherItem(launcherItem);
            if (launcherItem.container == Constants.CONTAINER_HOTSEAT) {
                addAppToDock(appView, launcherItem.cell);
                if (launcherItem.cell == -1) {
                    launcherItem.cell = hotseatCell;
                    hotseatCell++;
                } else {
                    hotseatCell = launcherItem.cell;
                }
            } else if (launcherItem.container == Constants.CONTAINER_DESKTOP) {
                if (workspaceScreen.getChildCount() >= mDeviceProfile.maxAppsPerPage
                        || launcherItem.screenId > pages.size() - 1) {
                    workspaceScreen = preparePage();
                    pages.add(workspaceScreen);
                }
                launcherItem.screenId = pages.size() - 1;
                launcherItem.cell = workspaceScreen.getChildCount();
                addAppToGrid(workspaceScreen, appView);
            }
        }

        for (int i = 0; i < pages.size(); i++) {
            mHorizontalPager.addView(pages.get(i));
        }
        currentPageNumber = 0;

        mHorizontalPager.setUiCreated(true);
        // DatabaseManager.getManager(this).saveLayouts(pages, mDock);
        mDock.setEnabled(true);
        setUpSwipeSearchContainer();
    }

    @SuppressLint("InflateParams")
    private GridLayout preparePage() {
        GridLayout grid = (GridLayout) getLayoutInflater().inflate(R.layout.apps_page, null);
        grid.setRowCount(mDeviceProfile.numRows);
        grid.setLayoutTransition(getDefaultLayoutTransition());
        grid.setPadding(mDeviceProfile.iconDrawablePaddingPx / 2, (int) (Utilities.pxFromDp(8, this)),
                mDeviceProfile.iconDrawablePaddingPx / 2, 0);

        // If a user taps outside (background / space) stop wobbling
        grid.setOnClickListener(view -> handleWobbling(false));

        return grid;
    }

    private void createWidgetsPage() {
        widgetsPage = (InsettableFrameLayout) getLayoutInflater().inflate(R.layout.widgets_page, mHorizontalPager,
                false);
        widgetContainer = widgetsPage.findViewById(R.id.widget_container);
        /*
         * widgetsPage.setPadding(0, (int) (Utilities.pxFromDp(8, this)), 0, 0);
         */
        mHorizontalPager.addView(widgetsPage, 0);
        widgetsPage.setOnDragListener(null);
        ScrollView scrollView = widgetsPage.findViewById(R.id.widgets_scroll_container);
        scrollView.setOnTouchListener((v, event) -> {
            if (widgetsPage.findViewById(R.id.widget_resizer_container).getVisibility() == VISIBLE) {
                hideWidgetResizeContainer();
            }
            return false;
        });
        currentPageNumber = 1;
        mHorizontalPager.setCurrentPage(currentPageNumber);

        widgetsPage.findViewById(R.id.used_apps_layout).setClipToOutline(true);

        // Prepare app suggestions view
        // [[BEGIN]]
        widgetsPage.findViewById(R.id.openUsageAccessSettings)
                .setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));

        // divided by 2 because of left and right padding.
        int padding = (int) (mDeviceProfile.availableWidthPx / 2 - Utilities.pxFromDp(8, this)
                - 2 * mDeviceProfile.cellWidthPx);
        widgetsPage.findViewById(R.id.suggestedAppGrid).setPadding(padding, 0, padding, 0);
        // [[END]]

        // Prepare search suggestion view
        // [[BEGIN]]
        ImageView clearSuggestions = widgetsPage.findViewById(R.id.clearSuggestionImageView);
        clearSuggestions.setOnClickListener(v -> {
            mSearchInput.setText("");
            mSearchInput.clearFocus();
        });

        mSearchInput = widgetsPage.findViewById(R.id.search_input);
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    clearSuggestions.setVisibility(GONE);
                } else {
                    clearSuggestions.setVisibility(VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        RecyclerView suggestionRecyclerView = widgetsPage.findViewById(R.id.suggestionRecyclerView);
        AutoCompleteAdapter suggestionAdapter = new AutoCompleteAdapter(this);
        suggestionRecyclerView.setHasFixedSize(true);
        suggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        suggestionRecyclerView.setAdapter(suggestionAdapter);
        getCompositeDisposable().add(RxTextView.textChanges(mSearchInput).debounce(300, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString).distinctUntilChanged().switchMap(charSequence -> {
                    if (charSequence != null && charSequence.length() > 0) {
                        return searchForQuery(charSequence);
                    } else {
                        return Observable.just(new SuggestionsResult(charSequence));
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SearchInputDisposableObserver(this, suggestionAdapter, widgetsPage)));

        mSearchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });

        mSearchInput.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(mSearchInput);
                runSearch(mSearchInput.getText().toString());
                mSearchInput.setText("");
                mSearchInput.clearFocus();
                return true;
            }
            return false;
        });
        // [[END]]

        // Prepare edit widgets button
        findViewById(R.id.edit_widgets_button)
                .setOnClickListener(view -> startActivity(new Intent(this, WidgetsActivity.class)));

        if (WeatherUtils.isWeatherServiceAvailable(this)) {
            startService(new Intent(this, WeatherSourceListenerService.class));
            startService(new Intent(this, DeviceStatusService.class));
        }

        if (!Preferences.useCustomWeatherLocation(this)) {
            if (!WeatherPreferences.hasLocationPermission(this)) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions, WeatherPreferences.LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && Preferences.getEnableLocation(this)) {
                    showLocationEnableDialog();
                    Preferences.setEnableLocation(this);
                } else {
                    startService(new Intent(this, WeatherUpdateService.class)
                            .setAction(WeatherUpdateService.ACTION_FORCE_UPDATE));
                }
            }
        } else {
            startService(
                    new Intent(this, WeatherUpdateService.class).setAction(WeatherUpdateService.ACTION_FORCE_UPDATE));
        }
        // [[END]]

        rebindWidgetHost();
    }

    private void rebindWidgetHost() {
        int[] widgetIds = mAppWidgetHost.getAppWidgetIds();
        getCompositeDisposable().add(DatabaseManager.getManager(this).getWidgets(widgetIds)
                .subscribeOn(Schedulers.from(AppExecutors.getInstance().diskIO()))
                .observeOn(AndroidSchedulers.mainThread()).subscribe(this::bindWidgets));
    }

    public void rebindAllWidgets() {
        if (widgetsPage != null) {
            widgetContainer.removeAllViewsInLayout();
            rebindWidgetHost();
        }
    }

    private void bindWidgets(List<WidgetItem> widgets) {
        for (WidgetItem widget : widgets) {
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widget.id);
            if (appWidgetInfo == null)
                continue;

            RoundedWidgetView hostView = (RoundedWidgetView) mAppWidgetHost.createView(getApplicationContext(),
                    widget.id, appWidgetInfo);
            hostView.setAppWidget(widget.id, appWidgetInfo);

            RoundedWidgetView widgetView = WidgetViewBuilder.create(this, hostView);
            if (widgetView == null)
                continue;
            if (widget.height != 0) {
                int minHeight = hostView.getAppWidgetInfo().minResizeHeight;
                int maxHeight = mDeviceProfile.availableHeightPx * 3 / 4;
                int normalisedDifference = (maxHeight - minHeight) / 100;
                widgetView.getLayoutParams().height = minHeight + (normalisedDifference * widget.height);
            }
            addWidgetToContainer(widgetView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == WeatherPreferences.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We only get here if user tried to enable the preference,
                // hence safe to turn it on after permission is granted
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showLocationEnableDialog();
                    Preferences.setEnableLocation(this);
                } else {
                    startService(new Intent(this, WeatherUpdateService.class)
                            .setAction(WeatherUpdateService.ACTION_FORCE_UPDATE));
                }
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BlurWallpaperProvider.Companion.getInstance(getApplicationContext()).updateAsync();
            }
        }
    }

    private void showNotifPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.perm_required);
        builder.setMessage(R.string.notification_badge_warning);
        builder.setPositiveButton(R.string.grant, (dialog, button) -> {
            Bundle showFragmentArgs = new Bundle();
            showFragmentArgs.putString(EXTRA_FRAGMENT_ARG_KEY, notificationComponentName.flattenToString());
            if (permissionString == null || !permissionString.contains(getPackageName())) {
                dialog.dismiss();
                // Show notification access setting and highlight launcher
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        .putExtra(EXTRA_FRAGMENT_ARG_KEY, notificationComponentName.flattenToString())
                        .putExtra(EXTRA_SHOW_FRAGMENT_ARGS, showFragmentArgs));
                // Prevent the activity from being shown again
                Preferences.setNotToAskForNotificationAccess(this);
                Preferences.setNotToShowNotificationDialog(this);
            }
        });
        builder.setNegativeButton(R.string.ignore, (dialog, button) -> {
            Preferences.setNotToShowNotificationDialog(this);
            dialog.dismiss();
        });
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLocationEnableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Build and show the dialog
        builder.setTitle(R.string.weather_retrieve_location_dialog_title);
        builder.setMessage(R.string.weather_retrieve_location_dialog_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.weather_retrieve_location_dialog_enable_button, (dialog1, whichButton) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_LOCATION_SOURCE_SETTING);
        });
        builder.setNegativeButton(R.string.cancel, null);
        enableLocationDialog = builder.create();
        enableLocationDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_SOURCE_SETTING) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Toast.makeText(this, getString(R.string.toast_custom_location), Toast.LENGTH_SHORT).show();
            } else {
                startService(new Intent(this, WeatherUpdateService.class)
                        .setAction(WeatherUpdateService.ACTION_FORCE_UPDATE));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private ObservableSource<SuggestionsResult> searchForQuery(CharSequence charSequence) {
        Observable<SuggestionsResult> launcherItems = searchForLauncherItems(charSequence.toString())
                .subscribeOn(Schedulers.io());
        Observable<SuggestionsResult> networkItems = searchForNetworkItems(charSequence).subscribeOn(Schedulers.io());
        return launcherItems.mergeWith(networkItems);
    }

    private Observable<SuggestionsResult> searchForLauncherItems(CharSequence charSequence) {
        String query = Utilities.stripCaseAndAccents(charSequence.toString());
        SuggestionsResult suggestionsResult = new SuggestionsResult(query);
        List<LauncherItem> launcherItems = new ArrayList<>();
        pages.parallelStream().forEach(gridLayout -> {
            for (int i = 0; i < gridLayout.getChildCount(); i++) {
                BlissFrameLayout blissFrameLayout = (BlissFrameLayout) gridLayout.getChildAt(i);
                LauncherItem launcherItem = blissFrameLayout.getLauncherItem();
                if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                    FolderItem folderItem = (FolderItem) launcherItem;
                    for (LauncherItem item : folderItem.items) {
                        if (Utilities.stripCaseAndAccents(item.title.toString()).contains(query)) {
                            launcherItems.add(item);
                        }
                    }
                } else if (Utilities.stripCaseAndAccents(launcherItem.title.toString()).contains(query)) {
                    launcherItems.add(launcherItem);
                }
            }
        });

        for (int i = 0; i < mDock.getChildCount(); i++) {
            BlissFrameLayout blissFrameLayout = (BlissFrameLayout) mDock.getChildAt(i);
            LauncherItem launcherItem = blissFrameLayout.getLauncherItem();
            if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                FolderItem folderItem = (FolderItem) launcherItem;
                for (LauncherItem item : folderItem.items) {
                    if (Utilities.stripCaseAndAccents(item.title.toString()).contains(query)) {
                        launcherItems.add(item);
                    }
                }
            } else if (Utilities.stripCaseAndAccents(launcherItem.title.toString()).contains(query)) {
                launcherItems.add(launcherItem);
            }
        }

        launcherItems
                .sort(Comparator.comparing(launcherItem -> launcherItem.title.toString().toLowerCase().indexOf(query)));

        if (launcherItems.size() > 4) {
            suggestionsResult.setLauncherItems(launcherItems.subList(0, 4));
        } else {
            suggestionsResult.setLauncherItems(launcherItems);
        }
        return Observable.just(suggestionsResult).onErrorReturn(throwable -> {
            suggestionsResult.setLauncherItems(new ArrayList<>());
            return suggestionsResult;
        });
    }

    private Observable<SuggestionsResult> searchForNetworkItems(CharSequence charSequence) {
        String query = charSequence.toString().toLowerCase(Locale.getDefault()).trim();
        SuggestionProvider suggestionProvider = new SearchSuggestionUtil().getSuggestionProvider(this);
        return suggestionProvider.query(query).toObservable();
    }

    @Override
    public void onClick(String suggestion) {
        mSearchInput.setText(suggestion);
        runSearch(suggestion);
        mSearchInput.clearFocus();
        mSearchInput.setText("");
    }

    private void runSearch(String query) {
        Intent intent = new Intent(Intent.ACTION_VIEW, new SearchSuggestionUtil().getUriForQuery(this, query));
        startActivity(intent);
    }

    private int getCurrentAppsPageNumber() {
        return currentPageNumber - 1;
    }

    public void addAppToGrid(GridLayout page, BlissFrameLayout view) {
        addAppToGrid(page, view, EMPTY_LOCATION_DRAG);
    }

    private void addAppToGrid(GridLayout page, BlissFrameLayout view, int index) {
        GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        GridLayout.Spec colSpec = GridLayout.spec(GridLayout.UNDEFINED);
        GridLayout.LayoutParams iconLayoutParams = new GridLayout.LayoutParams(rowSpec, colSpec);
        iconLayoutParams.height = mDeviceProfile.cellHeightPx;
        iconLayoutParams.width = mDeviceProfile.cellWidthPx;
        view.findViewById(R.id.app_label).setVisibility(View.VISIBLE);
        view.setLayoutParams(iconLayoutParams);
        view.setWithText(true);
        if (index == EMPTY_LOCATION_DRAG || index == LauncherItem.INVALID_CELL || index > page.getChildCount()) {
            page.addView(view);
        } else {
            page.addView(view, index);
        }
        // DatabaseManager.getManager(this).saveLayouts(pages, mDock);
    }

    /**
     * Adds networkItems to the mDock making sure that the GridLayout's parameters
     * are not violated.
     */
    private void addAppToDock(BlissFrameLayout view, int index) {
        view.findViewById(R.id.app_label).setVisibility(GONE);
        GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        GridLayout.Spec colSpec = GridLayout.spec(GridLayout.UNDEFINED);
        GridLayout.LayoutParams iconLayoutParams = new GridLayout.LayoutParams(rowSpec, colSpec);
        iconLayoutParams.height = mDeviceProfile.hotseatCellHeightPx;
        iconLayoutParams.width = mDeviceProfile.cellWidthPx;
        iconLayoutParams.setGravity(Gravity.CENTER);
        view.setLayoutParams(iconLayoutParams);
        view.setWithText(false);
        if (index == LauncherItem.INVALID_CELL || index == EMPTY_LOCATION_DRAG || index > mDock.getChildCount()) {
            mDock.addView(view);
        } else {
            mDock.addView(view, index);
        }
    }

    /**
     * Converts an AppItem into a View object that can be rendered inside the pages
     * and the mDock.
     * <p>
     * The View object also has all the required listeners attached to it.
     */
    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    private BlissFrameLayout prepareLauncherItem(final LauncherItem launcherItem) {
        final BlissFrameLayout iconView = (BlissFrameLayout) getLayoutInflater().inflate(R.layout.app_view, null);
        iconView.setLauncherItem(launcherItem);
        final SquareFrameLayout icon = iconView.findViewById(R.id.app_icon);
        if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
            iconView.applyBadge(checkHasApp((FolderItem) launcherItem, mAppsWithNotifications),
                    launcherItem.container != Constants.CONTAINER_HOTSEAT);
        } else if (launcherItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
            ApplicationItem applicationItem = (ApplicationItem) launcherItem;
            if (applicationItem.appType == ApplicationItem.TYPE_CALENDAR) {
                mCalendarIcons.add(iconView);
            }
            iconView.applyBadge(mAppsWithNotifications.contains(applicationItem.packageName),
                    launcherItem.container != Constants.CONTAINER_HOTSEAT);
        }

        icon.setOnLongClickListener(view -> {
            view.setHapticFeedbackEnabled(true);
            handleWobbling(true);
            longPressed = true;
            return true;
        });

        icon.setOnTouchListener(new View.OnTouchListener() {
            long iconPressedAt = 0;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (!mLongClickStartsDrag) {
                        iconPressedAt = System.currentTimeMillis();
                    }
                } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    if (longPressed || (!mLongClickStartsDrag && (System.currentTimeMillis() - iconPressedAt) > 150)) {
                        longPressed = false;
                        movingApp = iconView;
                        dragShadowBuilder = new BlissDragShadowBuilder(icon, (event.getX() < 0 ? 0 : event.getX()),
                                (event.getY() < 0 ? 0 : event.getY()));
                        icon.startDragAndDrop(null, dragShadowBuilder, iconView, 0);
                        if (iconView.getParent().getParent() instanceof HorizontalPager) {
                            parentPage = getCurrentAppsPageNumber();
                        } else {
                            parentPage = -99;
                        }
                        iconView.clearAnimation();
                        movingApp.setVisibility(View.INVISIBLE);
                        dragDropEnabled = true;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (movingApp != null && movingApp.getVisibility() != VISIBLE) {
                            movingApp.setVisibility(VISIBLE);
                            movingApp.invalidate();
                        }
                    });
                    return false;
                }
                return false;
            }
        });

        icon.setOnClickListener(view -> {
            if (isWobbling) {
                handleWobbling(false);
                return;
            }

            if (launcherItem.itemType != Constants.ITEM_TYPE_FOLDER) {
                startActivitySafely(getApplicationContext(), launcherItem, view);
            } else {
                folderFromDock = !(iconView.getParent().getParent() instanceof HorizontalPager);
                displayFolder((FolderItem) launcherItem, iconView);
            }
        });

        return iconView;
    }

    public BlissFrameLayout prepareSuggestedApp(final LauncherItem launcherItem) {
        final BlissFrameLayout v = (BlissFrameLayout) getLayoutInflater().inflate(R.layout.app_view, null);
        v.setLauncherItem(launcherItem);
        final SquareFrameLayout icon = v.findViewById(R.id.app_icon);

        if (launcherItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
            v.applyBadge(mAppsWithNotifications.contains(((ApplicationItem) launcherItem).packageName), true);
        }
        icon.setOnClickListener(view -> startActivitySafely(this, launcherItem, view));
        return v;
    }

    private void startActivitySafely(Context context, LauncherItem launcherItem, View v) {
        Intent intent = launcherItem.getIntent();

        android.os.UserHandle user = launcherItem.user.getRealHandle();

        if (v != null) {
            intent.setSourceBounds(getViewBounds(v));
        }

        if (launcherItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
            startShortcutIntentSafely(context, intent, launcherItem);
        } else {
            ApplicationItem applicationItem = (ApplicationItem) launcherItem;
            if (applicationItem.isDisabled) {
                Toast.makeText(this, getString(R.string.toast_package_unavailable), Toast.LENGTH_SHORT).show();
            } else {
                if (user == null || user.equals(Process.myUserHandle())) {
                    context.startActivity(intent);
                } else {
                    ((LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE)).startMainActivity(intent.getComponent(),
                            user, intent.getSourceBounds(), null);
                }
            }
        }

    }

    private void startShortcutIntentSafely(Context context, Intent intent, LauncherItem appItem) {
        try {
            StrictMode.VmPolicy oldPolicy = StrictMode.getVmPolicy();
            try {
                // Temporarily disable deathPenalty on all default checks. For eg, shortcuts
                // containing file Uri's would cause a crash as penaltyDeathOnFileUriExposure
                // is enabled by default on NYC.
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());

                if (appItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                    if (Utilities.ATLEAST_OREO) {
                        String id = appItem.id;
                        String packageName = intent.getPackage();
                        DeepShortcutManager.getInstance(context).startShortcut(packageName, id,
                                intent.getSourceBounds(), null, Process.myUserHandle());
                    } else {
                        context.startActivity(intent);
                    }

                } else {
                    // Could be launching some bookkeeping activity
                    context.startActivity(intent);
                }
            } finally {
                StrictMode.setVmPolicy(oldPolicy);
            }
        } catch (SecurityException e) {
            // Due to legacy reasons, direct call shortcuts require Launchers to have the
            // corresponding permission. Show the appropriate permission prompt if that
            // is the case.
            if (intent.getComponent() == null && Intent.ACTION_CALL.equals(intent.getAction()) && context
                    .checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PERMISSION_CALL_PHONE);
            } else {
                // No idea why this was thrown.
                throw e;
            }
        }
    }

    private Rect getViewBounds(View v) {
        int[] pos = new int[2];
        v.getLocationOnScreen(pos);
        return new Rect(pos[0], pos[1], pos[0] + v.getWidth(), pos[1] + v.getHeight());
    }

    /**
     * Handle the wobbling animation.
     */
    private void handleWobbling(boolean shouldPlay) {
        if (mWobblingCountDownTimer != null && !shouldPlay) {
            mWobblingCountDownTimer.cancel();
        }
        isWobbling = shouldPlay;
        mLongClickStartsDrag = !shouldPlay;
        longPressed = false;

        if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < mFolderAppsViewPager.getChildCount(); i++) {
                toggleWobbleAnimation((GridLayout) mFolderAppsViewPager.getChildAt(i), shouldPlay);
            }
        }

        for (int i = 0; i < pages.size(); i++) {
            toggleWobbleAnimation(pages.get(i), shouldPlay);
        }
        toggleWobbleAnimation(mDock, shouldPlay);
    }

    /**
     * Toggle the wobbling animation.
     */
    private void toggleWobbleAnimation(GridLayout gridLayout, boolean shouldPlayAnimation) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            BlissFrameLayout blissFrameLayout = (BlissFrameLayout) gridLayout.getChildAt(i);
            makeAppWobble(blissFrameLayout, shouldPlayAnimation, i);
        }
    }

    private void makeAppWobble(BlissFrameLayout blissFrameLayout, boolean shouldPlayAnimation, int i) {
        UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
        Bundle restrictions = userManager.getUserRestrictions();
        boolean uninstallDisabled = restrictions.getBoolean(UserManager.DISALLOW_APPS_CONTROL, false)
                || restrictions.getBoolean(UserManager.DISALLOW_UNINSTALL_APPS, false);
        if (shouldPlayAnimation) {
            if (blissFrameLayout.getAnimation() == null) {
                ImageView imageView = blissFrameLayout.findViewById(R.id.uninstall_app);
                if (imageView == null) {
                    if (!uninstallDisabled) {
                        new Handler(Looper.getMainLooper()).post(() -> addUninstallIcon(blissFrameLayout));
                    }
                }

                if (i % 2 == 0) {
                    blissFrameLayout.startAnimation(wobbleAnimation);
                } else {
                    blissFrameLayout.startAnimation(wobbleReverseAnimation);
                }
            }
        } else {
            blissFrameLayout.clearAnimation();
            new Handler(Looper.getMainLooper()).post(() -> removeUninstallIcon(blissFrameLayout));
        }
    }

    private void removeUninstallIcon(BlissFrameLayout blissFrameLayout) {
        ImageView imageView = blissFrameLayout.findViewById(R.id.uninstall_app);
        if (imageView != null) {
            ((ViewGroup) imageView.getParent()).removeView(imageView);
        }
    }

    /**
     * Display uninstall icon while animating the view.
     */
    private void addUninstallIcon(BlissFrameLayout blissFrameLayout) {
        final LauncherItem launcherItem = getAppDetails(blissFrameLayout);
        if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
            return;
        }

        if (launcherItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
            ApplicationItem applicationItem = (ApplicationItem) launcherItem;
            if (applicationItem.isSystemApp != ApplicationItem.FLAG_SYSTEM_UNKNOWN) {
                if ((applicationItem.isSystemApp & ApplicationItem.FLAG_SYSTEM_NO) == 0) {
                    return;
                }
            }
        }

        SquareFrameLayout appIcon = blissFrameLayout.findViewById(R.id.app_icon);
        int size = mDeviceProfile.uninstallIconSizePx;
        int topPadding = (appIcon.getTop() - mDeviceProfile.uninstallIconSizePx / 2
                + mDeviceProfile.uninstallIconPadding > 0)
                        ? appIcon.getTop() - mDeviceProfile.uninstallIconSizePx / 2
                                + mDeviceProfile.uninstallIconPadding
                        : 0;
        int bottomPadding = topPadding;
        int rightPadding = (appIcon.getLeft() - mDeviceProfile.uninstallIconSizePx / 2
                + mDeviceProfile.uninstallIconPadding > 0)
                        ? appIcon.getLeft() - mDeviceProfile.uninstallIconSizePx / 2
                                + mDeviceProfile.uninstallIconPadding
                        : 0;
        int leftPadding = rightPadding;

        ImageView imageView = new ImageView(this);
        imageView.setId(R.id.uninstall_app);
        imageView.setImageResource(R.drawable.remove_icon_72);
        imageView.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);

        imageView.setOnClickListener(v -> {
            if (launcherItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
                ComponentName componentName = launcherItem.getTargetComponent();
                if (componentName == null) {
                    // System applications cannot be installed. For now, show a toast explaining
                    // that.
                    // We may give them the option of disabling apps this way.
                    Toast.makeText(this, getString(R.string.toast_cannot_uninstall), Toast.LENGTH_SHORT).show();
                } else {
                    Uri packageUri = Uri.fromParts("package", componentName.getPackageName(),
                            componentName.getClassName());
                    Intent i = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri).putExtra(Intent.EXTRA_USER,
                            launcherItem.user.getRealHandle());
                    startActivity(i);
                }
            } else if (launcherItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom))
                        .setTitle(launcherItem.title).setMessage(R.string.uninstall_shortcut_dialog)
                        .setPositiveButton(R.string.ok, (dialog1, which) -> {
                            ShortcutItem shortcut = (ShortcutItem) launcherItem;
                            if (shortcut.packageName != null) {
                                DeepShortcutManager.getInstance(this).unpinShortcut(ShortcutKey.fromItem(shortcut));
                                if (DeepShortcutManager.getInstance(this).wasLastCallSuccess()) {
                                    deleteShortcutFromProvider(shortcut.id);
                                    removeShortcutView(shortcut, blissFrameLayout);
                                }
                            } else {
                                // Null package name generally comes for nougat shortcuts so don't unpin here,
                                // just directly delete it.
                                deleteShortcutFromProvider(shortcut.id);
                                removeShortcutView(shortcut, blissFrameLayout);
                            }

                        }).setNegativeButton(R.string.cancel, null).setIcon(launcherItem.icon).create();
                dialog.setOnShowListener(arg0 -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(R.color.color_blue));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getResources().getColor(R.color.color_blue));
                });
                dialog.show();
            }
        });
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size + 2 * rightPadding,
                size + 2 * topPadding);
        layoutParams.gravity = Gravity.END | Gravity.TOP;
        blissFrameLayout.addView(imageView, layoutParams);
    }

    private void deleteShortcutFromProvider(String id) {
        ContentResolver resolver = getContentResolver();
        int count = resolver.delete(Uri.parse("content://foundation.e.pwaplayer.provider/pwa"), null, new String[]{id});
        Log.d("LauncherActivity", "Items deleted from pwa provider: " + count);
    }

    private void removeShortcutView(ShortcutItem shortcutItem, BlissFrameLayout blissFrameLayout) {
        DatabaseManager.getManager(this).removeLauncherItem(shortcutItem.id);
        if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
            activeFolder.items.remove(shortcutItem);
            mFolderAppsViewPager.getAdapter().notifyDataSetChanged();
            blissFrameLayout.clearAnimation();
            ((ViewGroup) blissFrameLayout.getParent()).removeView(blissFrameLayout);
            if (activeFolder.items.size() == 0) {
                ((ViewGroup) activeFolderView.getParent()).removeView(activeFolderView);
                hideFolderWindowContainer();
            } else if (activeFolder.items.size() == 1) {
                LauncherItem item = activeFolder.items.get(0);
                activeFolder.items.remove(item);
                mFolderAppsViewPager.getAdapter().notifyDataSetChanged();
                BlissFrameLayout view = prepareLauncherItem(item);

                if (folderFromDock) {
                    addAppToDock(view, mDock.indexOfChild(activeFolderView));
                } else {
                    GridLayout gridLayout = pages.get(getCurrentAppsPageNumber());
                    addAppToGrid(gridLayout, view, gridLayout.indexOfChild(activeFolderView));
                }
                activeFolderView.clearAnimation();
                ((ViewGroup) activeFolderView.getParent()).removeView(activeFolderView);
                hideFolderWindowContainer();
            } else {
                updateIcon(activeFolderView, activeFolder,
                        new GraphicsUtil(this).generateFolderIcon(this, activeFolder), folderFromDock);
                hideFolderWindowContainer();
            }
        } else {
            blissFrameLayout.clearAnimation();
            ((ViewGroup) blissFrameLayout.getParent()).removeView(blissFrameLayout);
        }
    }

    /**
     * Creates drag listeners for the mDock and pages, which are responsible for
     * almost all the drag and drop functionality present in this app.
     */
    private void createDragListener() {
        /*
         * mHorizontalPager.setOnDragListener(new
         * SystemDragDriver(workspaceEventListener)); mDock.setOnDragListener(new
         * SystemDragDriver(dockEventListener));
         */
        mDock.setOnDragListener(new View.OnDragListener() {
            public float cX;
            public float cY;
            private boolean latestFolderInterest;

            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                if (dragEvent.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                    isDragging = true;
                    if (mWobblingCountDownTimer != null) {
                        mWobblingCountDownTimer.cancel();
                    }
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
                    // Don't offer rearrange functionality when app is being dragged
                    // out of folder window
                    if (getAppDetails(movingApp).container != Constants.CONTAINER_DESKTOP
                            && getAppDetails(movingApp).container != Constants.CONTAINER_HOTSEAT) {
                        return true;
                    }

                    // Do nothing during scroll operations
                    if (!dragDropEnabled) {
                        return true;
                    }

                    cX = dragEvent.getX() - dragShadowBuilder.xOffset;
                    cY = mDock.getY() + dragEvent.getY() - dragShadowBuilder.yOffset;

                    int index = getIndex(mDock, cX, cY);

                    // If hovering over self, ignore drag/drop
                    if (index == mDock.indexOfChild(movingApp)) {
                        discardCollidingApp();
                        return true;
                    }

                    // If hovering over an empty location, ignore drag/drop
                    // If hovering over another app icon either move it
                    // Or create a folder depending on time and distance
                    if (index == EMPTY_LOCATION_DRAG) {
                        discardCollidingApp();
                    } else {
                        BlissFrameLayout latestCollidingApp = (BlissFrameLayout) mDock.getChildAt(index);
                        if (collidingApp != latestCollidingApp) {
                            if (collidingApp != null) {
                                makeAppCold(collidingApp,
                                        !(collidingApp.getParent().getParent() instanceof HorizontalPager));
                            }
                            collidingApp = latestCollidingApp;
                            folderInterest = false;
                        }

                        LauncherItem movingLauncherItem = movingApp.getLauncherItem();
                        if (movingLauncherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
                            folderInterest = false;
                        } else {
                            latestFolderInterest = checkIfFolderInterest(mDock, index, cX, cY);
                            if (latestFolderInterest != folderInterest) {
                                folderInterest = latestFolderInterest;
                            }
                            if (folderInterest) {
                                cleanupDockReorder(true);
                                cleanupReorder(true);
                                makeAppHot(collidingApp);
                            } else {
                                View app = collidingApp;
                                makeAppCold(app, !(app.getParent().getParent() instanceof HorizontalPager));
                            }
                        }
                    }

                    if (!folderInterest && !mDockReorderAlarm.alarmPending()) {
                        DockReorderAlarmListener dockReorderAlarmListener = new DockReorderAlarmListener(index);
                        mDockReorderAlarm.setOnAlarmListener(dockReorderAlarmListener);
                        mDockReorderAlarm.setAlarm(REORDER_TIMEOUT);
                    }
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
                    cleanupDockReorder(true);
                    cleanupReorder(true);
                    if (mFolderWindowContainer.getVisibility() != View.VISIBLE) {
                        // Drop functionality when the folder window container
                        // is not being shown -- default
                        if (!folderInterest) {
                            if (movingApp.getParent() == null) {
                                if (mDock.getChildCount() >= mDeviceProfile.numColumns) {
                                    Toast.makeText(LauncherActivity.this, getString(R.string.toast_dock_full),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    addAppToDock(movingApp, EMPTY_LOCATION_DRAG);
                                }
                            }
                            movingApp.setVisibility(View.VISIBLE);
                            makeAppWobble(movingApp, true, mDock.indexOfChild(movingApp));
                        } else {
                            if (collidingApp.getParent().getParent() instanceof HorizontalPager) {
                                createOrUpdateFolder(false);
                            } else {
                                createOrUpdateFolder(true);
                            }
                            folderInterest = false;
                        }
                    } else {
                        cX = dragEvent.getX() - dragShadowBuilder.xOffset;
                        cY = mDock.getY() + dragEvent.getY() - dragShadowBuilder.yOffset;
                        // Drop functionality when the folder window is visible
                        int[] topLeftCorner = new int[2];
                        mFolderAppsViewPager.getLocationOnScreen(topLeftCorner);
                        int left = topLeftCorner[0];
                        int top = topLeftCorner[1];
                        int right = left + mFolderAppsViewPager.getWidth();
                        int bottom = top + mFolderAppsViewPager.getHeight();

                        if (!(left < right && top < bottom && cX >= left && cX < right && cY >= top && cY < bottom)) {
                            removeAppFromFolder();
                        } else {
                            movingApp.setVisibility(View.VISIBLE);
                            int currentItem = mFolderAppsViewPager.getCurrentItem();
                            makeAppWobble(movingApp, true, ((GridLayout) mFolderAppsViewPager.getChildAt(currentItem))
                                    .indexOfChild(movingApp));
                        }
                    }
                    return true;
                }

                return true;
            }
        });

        mHorizontalPager.setOnDragListener(new View.OnDragListener() {
            public float cY;
            public float cX;
            private boolean latestFolderInterest;

            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                if (dragEvent.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                    isDragging = true;
                    if (mWobblingCountDownTimer != null) {
                        mWobblingCountDownTimer.cancel();
                    }
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
                    cX = dragEvent.getX() - dragShadowBuilder.xOffset;
                    cY = mHorizontalPager.getY() + dragEvent.getY() - dragShadowBuilder.yOffset;

                    // Don't offer rearrange functionality when app is being dragged
                    // out of folder window
                    if (getAppDetails(movingApp).container != Constants.CONTAINER_DESKTOP
                            && getAppDetails(movingApp).container != Constants.CONTAINER_HOTSEAT) {
                        return true;
                    }

                    // Do nothing during scroll operations
                    if (!dragDropEnabled) {
                        return true;
                    }

                    GridLayout page = pages.get(getCurrentAppsPageNumber());

                    if ((cX - mDeviceProfile.iconSizePx / 10) > mDeviceProfile.availableWidthPx - 2 * scrollCorner) {
                        if (getCurrentAppsPageNumber() + 1 < pages.size()) {
                            mHorizontalPager.scrollRight(300);
                        } else if (getCurrentAppsPageNumber() + 1 == pages.size()
                                && getGridFromPage(page).getChildCount() > 1) {
                            GridLayout layout = preparePage();
                            pages.add(layout);
                            ImageView dot = new ImageView(LauncherActivity.this);
                            dot.setImageDrawable(getDrawable(R.drawable.dot_off));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    getResources().getDimensionPixelSize(R.dimen.dotSize),
                                    getResources().getDimensionPixelSize(R.dimen.dotSize));
                            dot.setLayoutParams(params);
                            mIndicator.addView(dot);
                            mHorizontalPager.addView(layout);
                        }
                    } else if ((cX + mDeviceProfile.iconSizePx / 10) < 2 * scrollCorner) {
                        if (getCurrentAppsPageNumber() == 0) {
                            return true;
                        }
                        if (getCurrentAppsPageNumber() - 1 >= 0) {
                            mHorizontalPager.scrollLeft(300);
                        } else if (getCurrentAppsPageNumber() + 1 == pages.size() - 2
                                && getGridFromPage(pages.get(pages.size() - 1)).getChildCount() <= 0) {
                            mIndicator.removeViewAt(pages.size());
                            mHorizontalPager.removeViewAt(pages.size());
                            pages.remove(pages.size() - 1);
                        }
                    } else {

                        int index = getIndex(page, cX, cY);
                        // If hovering over self, ignore drag/drop
                        if (index == getGridFromPage(page).indexOfChild(movingApp)) {
                            discardCollidingApp();
                            return true;
                        }

                        // If hovering over an empty location, ignore drag/drop
                        if (index == EMPTY_LOCATION_DRAG) {
                            discardCollidingApp();
                        } else {
                            View latestCollidingApp = getGridFromPage(page).getChildAt(index);
                            if (collidingApp != latestCollidingApp) {
                                if (collidingApp != null) {
                                    makeAppCold(collidingApp,
                                            !(collidingApp.getParent().getParent() instanceof HorizontalPager));
                                }
                                collidingApp = (BlissFrameLayout) latestCollidingApp;
                                folderInterest = false;
                            }

                            if (getAppDetails(movingApp).itemType == Constants.ITEM_TYPE_FOLDER) {
                                folderInterest = false;
                            } else {
                                latestFolderInterest = checkIfFolderInterest(
                                        getGridFromPage(pages.get(getCurrentAppsPageNumber())), index, cX, cY);

                                if (latestFolderInterest != folderInterest) {
                                    folderInterest = latestFolderInterest;
                                }
                                if (folderInterest) {
                                    cleanupReorder(true);
                                    cleanupDockReorder(true);
                                    makeAppHot(collidingApp);
                                } else {
                                    makeAppCold(collidingApp,
                                            !(collidingApp.getParent().getParent() instanceof HorizontalPager));
                                }
                            }
                        }

                        if (!folderInterest && !mReorderAlarm.alarmPending()) {
                            ReorderAlarmListener reorderAlarmListener = new ReorderAlarmListener(page,
                                    (ViewGroup) movingApp.getParent(), index);
                            mReorderAlarm.setOnAlarmListener(reorderAlarmListener);
                            mReorderAlarm.setAlarm(REORDER_TIMEOUT);
                        }
                    }
                } else if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
                    cleanupReorder(true);
                    cleanupDockReorder(true);
                    if (mFolderWindowContainer.getVisibility() != View.VISIBLE) {
                        // Drop functionality when the folder window container
                        // is not being shown -- default
                        GridLayout gridLayout = pages.get(getCurrentAppsPageNumber());
                        if (!folderInterest) {
                            if (movingApp.getParent() == null) {
                                if (gridLayout.getChildCount() < mDeviceProfile.maxAppsPerPage) {
                                    addAppToGrid(gridLayout, movingApp);
                                }
                            }

                            movingApp.setVisibility(View.VISIBLE);
                            makeAppWobble(movingApp, true, gridLayout.indexOfChild(movingApp));
                        } else {
                            if (collidingApp.getParent().getParent() instanceof HorizontalPager) {
                                createOrUpdateFolder(false);
                            } else {
                                createOrUpdateFolder(true);
                            }
                            folderInterest = false;
                        }
                    } else {
                        cX = dragEvent.getX() - dragShadowBuilder.xOffset;
                        cY = mHorizontalPager.getY() + dragEvent.getY() - dragShadowBuilder.yOffset;

                        // Drop functionality when the folder window is visible
                        int[] topLeftCorner = new int[2];
                        mFolderAppsViewPager.getLocationOnScreen(topLeftCorner);
                        int left = topLeftCorner[0];
                        int top = topLeftCorner[1];
                        int right = left + mFolderAppsViewPager.getWidth();
                        int bottom = top + mFolderAppsViewPager.getHeight();

                        if (!(left < right && top < bottom && cX >= left && cX < right && cY >= top && cY < bottom)) {
                            removeAppFromFolder();
                        } else {
                            movingApp.setVisibility(View.VISIBLE);
                            int currentItem = mFolderAppsViewPager.getCurrentItem();
                            GridLayout gridLayout = (GridLayout) mFolderAppsViewPager.getChildAt(currentItem);
                            if (gridLayout != null) {
                                makeAppWobble(movingApp, true, gridLayout.indexOfChild(movingApp));
                            }
                        }
                    }
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                    if (isDragging) {
                        isDragging = false;
                    }

                    cleanupReorder(true);
                    cleanupDockReorder(true);

                    if (!dragEvent.getResult()) {
                        if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
                            int currentItem = mFolderAppsViewPager.getCurrentItem();
                            makeAppWobble(movingApp, true, ((GridLayout) mFolderAppsViewPager.getChildAt(currentItem))
                                    .indexOfChild(movingApp));
                        } else if (movingApp.getParent().getParent() instanceof HorizontalPager) {
                            GridLayout gridLayout = pages.get(getCurrentAppsPageNumber());
                            makeAppWobble(movingApp, true, gridLayout.indexOfChild(movingApp));
                        } else {
                            makeAppWobble(movingApp, true, mDock.indexOfChild(movingApp));
                        }
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        movingApp.setVisibility(VISIBLE);
                        movingApp = null;
                    });

                    if (mWobblingCountDownTimer != null) {
                        mWobblingCountDownTimer.cancel();
                    }
                    mLongClickStartsDrag = false;
                    mWobblingCountDownTimer = new CountDownTimer(25000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            if (isWobbling) {
                                handleWobbling(false);
                            }
                        }
                    }.start();

                    for (int i = 0; i < pages.size(); i++) {
                        if (pages.get(i).getChildCount() <= 0) {
                            pages.remove(i);
                            mHorizontalPager.removeViewAt(i + 1);
                            if (i == pages.size()) {
                                mHorizontalPager.scrollLeft(100);
                            }
                            mIndicator.removeViewAt(i);
                            updateIndicator();
                            i--;
                        }
                    }
                    DatabaseManager.getManager(LauncherActivity.this).saveLayouts(pages, mDock);
                }

                return true;
            }
        });
    }

    private void cleanupDockReorder(boolean cancelAlarm) {
        if (cancelAlarm) {
            mDockReorderAlarm.cancelAlarm();
        }
    }

    private void cleanupReorder(boolean cancelAlarm) {
        if (cancelAlarm) {
            mReorderAlarm.cancelAlarm();
        }
    }

    /**
     * Remove app from the folder by dragging out of the folder view.
     */
    private void removeAppFromFolder() {
        if (pages.get(getCurrentAppsPageNumber()).getChildCount() >= mDeviceProfile.maxAppsPerPage) {
            Toast.makeText(this, getString(R.string.toast_no_room), Toast.LENGTH_SHORT).show();
            movingApp.setVisibility(View.VISIBLE);
            int currentItem = mFolderAppsViewPager.getCurrentItem();
            makeAppWobble(movingApp, true,
                    ((GridLayout) mFolderAppsViewPager.getChildAt(currentItem)).indexOfChild(movingApp));
        } else {
            LauncherItem app = getAppDetails(movingApp);
            activeFolder.items.remove(app);
            mFolderAppsViewPager.getAdapter().notifyDataSetChanged();
            assert app != null;
            app.container = folderFromDock ? Constants.CONTAINER_HOTSEAT : Constants.CONTAINER_DESKTOP;
            app.screenId = folderFromDock ? -1 : currentPageNumber;

            if (activeFolder.items.size() == 0) {
                BlissFrameLayout view = prepareLauncherItem(app);
                if (folderFromDock) {
                    int index = mDock.indexOfChild(activeFolderView);
                    removeUninstallIcon(activeFolderView);
                    mDock.removeView(activeFolderView);
                    addAppToDock(view, index);
                    makeAppWobble(view, true, index);
                } else {
                    GridLayout gridLayout = pages.get(getCurrentAppsPageNumber());
                    int index = gridLayout.indexOfChild(activeFolderView);
                    activeFolderView.clearAnimation();
                    removeUninstallIcon(activeFolderView);
                    gridLayout.removeView(activeFolderView);
                    addAppToGrid(gridLayout, view, index);
                    makeAppWobble(view, true, index);
                }
                DatabaseManager.getManager(this).removeLauncherItem(activeFolder.id);
            } else {
                if (activeFolder.items.size() == 1) {
                    LauncherItem item = activeFolder.items.get(0);
                    activeFolder.items.remove(item);
                    mFolderAppsViewPager.getAdapter().notifyDataSetChanged();
                    item.container = folderFromDock ? Constants.CONTAINER_HOTSEAT : Constants.CONTAINER_DESKTOP;
                    item.screenId = folderFromDock ? -1 : currentPageNumber;
                    BlissFrameLayout view = prepareLauncherItem(item);
                    if (folderFromDock) {
                        int index = mDock.indexOfChild(activeFolderView);
                        activeFolderView.clearAnimation();
                        removeUninstallIcon(activeFolderView);
                        mDock.removeView(activeFolderView);
                        addAppToDock(view, index);
                        makeAppWobble(view, true, index);
                    } else {
                        GridLayout gridLayout = pages.get(getCurrentAppsPageNumber());
                        int index = gridLayout.indexOfChild(activeFolderView);
                        activeFolderView.clearAnimation();
                        removeUninstallIcon(activeFolderView);
                        gridLayout.removeView(activeFolderView);
                        addAppToGrid(gridLayout, view, index);
                        makeAppWobble(view, true, index);
                    }
                    DatabaseManager.getManager(this).removeLauncherItem(activeFolder.id);
                } else {
                    updateIcon(activeFolderView, activeFolder,
                            new GraphicsUtil(this).generateFolderIcon(this, activeFolder), folderFromDock);
                    activeFolderView.applyBadge(checkHasApp(activeFolder, mAppsWithNotifications), !folderFromDock);
                }
                if (movingApp.getParent() != null) {
                    ((ViewGroup) movingApp.getParent()).removeView(movingApp);
                }
                int current = getCurrentAppsPageNumber();
                addAppToGrid(pages.get(current), movingApp);
                makeAppWobble(movingApp, true, pages.get(current).getChildCount() - 1);
            }

            hideFolderWindowContainer();
            movingApp.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Returns an app to normal if the user doesn't express move/folder-creation
     * interests.
     */
    private void discardCollidingApp() {
        if (collidingApp != null) {
            makeAppCold(collidingApp, !(collidingApp.getParent().getParent() instanceof HorizontalPager));
            collidingApp = null;
            folderInterest = false;
        }
    }

    /**
     * Creates/updates a folder using the tags associated with the app being
     * dragged, and the target app.
     */
    private void createOrUpdateFolder(boolean fromDock) {
        int index;

        collidingApp.clearAnimation();

        if (fromDock) {
            index = mDock.indexOfChild(collidingApp);
        } else {
            index = getGridFromPage(pages.get(getCurrentAppsPageNumber())).indexOfChild(collidingApp);
        }

        LauncherItem app1 = getAppDetails(collidingApp);
        LauncherItem app2 = getAppDetails(movingApp);

        if (app1.itemType == Constants.ITEM_TYPE_FOLDER) {
            FolderItem folderItem = (FolderItem) app1;
            app2.container = Long.parseLong(folderItem.id);
            app2.screenId = -1;
            app2.cell = folderItem.items.size();
            folderItem.items.add(app2);
            updateIcon(collidingApp, app1, new GraphicsUtil(this).generateFolderIcon(this, folderItem), folderFromDock);
            collidingApp.applyBadge(checkHasApp(folderItem, mAppsWithNotifications), !fromDock);
            makeAppWobble(collidingApp, true, index);
        } else {
            FolderItem folder = new FolderItem();
            folder.title = getString(R.string.untitled);
            folder.id = String.valueOf(System.currentTimeMillis());
            folder.items = new ArrayList<>();
            app1.container = Long.parseLong(folder.id);
            app2.container = Long.parseLong(folder.id);
            app1.screenId = -1;
            app2.screenId = -1;
            app1.cell = folder.items.size();
            folder.items.add(app1);
            app2.cell = folder.items.size();
            folder.items.add(app2);
            Drawable folderIcon = new GraphicsUtil(this).generateFolderIcon(this, app1.icon, app2.icon);
            folder.icon = folderIcon;
            BlissFrameLayout folderView = prepareLauncherItem(folder);
            makeAppWobble(collidingApp, false, index);
            ((ViewGroup) collidingApp.getParent()).removeView(collidingApp);
            if (fromDock) {
                addAppToDock(folderView, index);
            } else {
                addAppToGrid(pages.get(getCurrentAppsPageNumber()), folderView, index);
            }
            makeAppWobble(folderView, true, index);
        }

        if (movingApp.getParent() != null) {
            ((ViewGroup) movingApp.getParent()).removeView(movingApp);
        }

        makeAppCold(collidingApp, fromDock);
        makeAppCold(movingApp, fromDock);

        // DatabaseManager.getManager(LauncherActivity.this).saveLayouts(pages, mDock);
    }

    private void updateIcon(BlissFrameLayout appView, LauncherItem app, Drawable drawable, boolean folderFromDock) {
        app.icon = drawable;
        List<Object> tags = (List<Object>) appView.getTag();
        SquareImageView iv = (SquareImageView) tags.get(0);
        iv.setImageDrawable(drawable);
        // appView.applyBadge(checkHasApp(app, mAppsWithNotifications),
        // !folderFromDock);
    }

    /**
     * Highlights an app
     */
    private void makeAppHot(View app) {
        if (app == null) {
            return;
        }
        app.setScaleX(1.2f);
        app.setScaleY(1.2f);
    }

    /**
     * Makes an app look normal
     */
    private synchronized void makeAppCold(View app, boolean fromDock) {
        if (app == null) {
            return;
        }

        List<Object> views = (List<Object>) app.getTag();
        if (!fromDock) {
            ((View) views.get(1)).setVisibility(View.VISIBLE);
        } else {
            ((View) views.get(1)).setVisibility(GONE);
        }
        app.setScaleX(1.0f);
        app.setScaleY(1.0f);
        collidingApp = null;
    }

    /**
     * Reads the tags of a View
     */
    private LauncherItem getAppDetails(View app) {
        if (app instanceof BlissFrameLayout) {
            return ((BlissFrameLayout) app).getLauncherItem();
        }
        return null;
    }

    /**
     * Checks if the user wants to create a folder based on the distance between the
     * dragged app and the dragged-over app
     */
    private boolean checkIfFolderInterest(ViewGroup view, int index, float x, float y) {
        View v = view.getChildAt(index).findViewById(R.id.app_icon);
        Rect r = new Rect();
        v.getGlobalVisibleRect(r);
        float vx = r.left + (r.right - r.left) / 2;
        float vy = r.top + (r.bottom - r.top) / 2;
        double distance = getDistance(x, y, vx, vy);
        return distance < maxDistanceForFolderCreation;
    }

    private GridLayout getGridFromPage(ViewGroup page) {
        return (GridLayout) page;
    }

    /**
     * Calculates the euclidean distance between two points
     */
    private double getDistance(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Identifies the app that surrounds a given point
     */
    private int getIndex(ViewGroup page, float x, float y) {
        float minDistance = Float.MAX_VALUE;
        int index = EMPTY_LOCATION_DRAG;

        for (int i = 0; i < page.getChildCount(); i++) {
            View v = page.getChildAt(i).findViewById(R.id.app_icon);
            Rect r = new Rect();
            v.getGlobalVisibleRect(r);
            Rect r2 = new Rect((int) (x - mDeviceProfile.iconSizePx / 2), (int) (y - mDeviceProfile.iconSizePx / 2),
                    (int) (x + mDeviceProfile.iconSizePx / 2), (int) (y + mDeviceProfile.iconSizePx / 2));
            if (Rect.intersects(r, r2)) {
                float vx = r.left + (float) (r.right - r.left) / 2;
                float vy = r.top + (float) (r.bottom - r.top) / 2;
                float distance = (float) Math.hypot(vx - x, vy - y);
                if (minDistance > distance) {
                    minDistance = distance;
                    index = i;
                }
            }
        }
        return index;
    }

    /**
     * Creates the animation effect that runs when apps are moved around
     */
    private LayoutTransition getDefaultLayoutTransition() {
        LayoutTransition transition = new LayoutTransition();
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        transition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        transition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
                dragDropEnabled = false;
            }

            @Override
            public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
                dragDropEnabled = true;
            }
        });
        return transition;
    }

    /**
     * Creates a dots-based mIndicator using two simple drawables.
     */
    private void createIndicator() {
        if (mIndicator.getChildCount() != 0) {
            mIndicator.removeAllViews();
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mDeviceProfile.pageIndicatorSizePx,
                mDeviceProfile.pageIndicatorSizePx);

        for (int i = 0; i < pages.size() + 1; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageDrawable(getDrawable(R.drawable.dot_off));
            dot.setLayoutParams(params);
            mIndicator.addView(dot);
        }
        updateIndicator();
    }

    private void updateIndicator() {
        if (mIndicator.getChildAt(activeDot) != null) {
            ((ImageView) mIndicator.getChildAt(activeDot)).setImageResource(R.drawable.dot_off);
        }
        if (mIndicator.getChildAt(currentPageNumber) != null) {
            ((ImageView) mIndicator.getChildAt(currentPageNumber)).setImageResource(R.drawable.dot_on);
            activeDot = currentPageNumber;
        }
    }

    /**
     * Ensures that backpress is not allowed, except when the folder window is open.
     */
    @Override
    public void onBackPressed() {
        returnToHomeScreen();
    }

    private void displayFolder(FolderItem app, BlissFrameLayout v) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        activeFolder = app;
        activeFolderView = v;

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        startBounds = new Rect();
        finalBounds = new Rect();
        Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        v.getGlobalVisibleRect(startBounds);
        findViewById(R.id.workspace).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        /*
         * ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 18);
         * valueAnimator.addUpdateListener(animation ->
         * BlurWallpaperProvider.getInstance(this).blur((Integer)
         * animation.getAnimatedValue()));
         */
        set.play(ObjectAnimator.ofFloat(mFolderWindowContainer, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mFolderWindowContainer, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mFolderWindowContainer, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(mFolderWindowContainer, View.SCALE_Y, startScale, 1f))
                .with(ObjectAnimator.ofFloat(blurLayer, View.ALPHA, 1f))
                .with(ObjectAnimator.ofFloat(mHorizontalPager, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mIndicator, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mDock, View.ALPHA, 0f));
        set.setDuration(300);
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFolderWindowContainer.setVisibility(View.VISIBLE);

                // Set the pivot point for SCALE_X and SCALE_Y transformations
                // to the top-left corner of the zoomed-in view (the default
                // is the center of the view).
                mFolderWindowContainer.setPivotX(0f);
                mFolderWindowContainer.setPivotY(0f);
                // BlurWallpaperProvider.getInstance(LauncherActivity.this).clear();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
                blurLayer.setAlpha(1f);
                mHorizontalPager.setAlpha(0f);
                mIndicator.setAlpha(0f);
                mDock.setAlpha(0f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
                mFolderWindowContainer.setVisibility(GONE);
                blurLayer.setAlpha(0f);
                mHorizontalPager.setAlpha(1f);
                mIndicator.setAlpha(1f);
                mDock.setAlpha(1f);
            }
        });
        set.start();
        currentAnimator = set;
        startScaleFinal = startScale;

        mFolderTitleInput.setText(app.title);
        mFolderTitleInput.setCursorVisible(false);

        mFolderAppsViewPager.setAdapter(new FolderAppsPagerAdapter(this, app.items));
        mFolderAppsViewPager.getLayoutParams().width = mDeviceProfile.cellWidthPx * 3
                + mDeviceProfile.iconDrawablePaddingPx;
        mFolderAppsViewPager.getLayoutParams().height = mDeviceProfile.cellHeightPx * 3
                + mDeviceProfile.iconDrawablePaddingPx;
        ((CircleIndicator) mLauncherView.findViewById(R.id.indicator)).setViewPager(mFolderAppsViewPager);

    }

    private Bitmap getLauncherView() {
        View view = getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * Hides folder window with an animation
     */
    private void hideFolderWindowContainer() {
        DatabaseManager.getManager(LauncherActivity.this).saveLayouts(pages, mDock);
        mFolderTitleInput.clearFocus();
        folderFromDock = false;
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        /*
         * ValueAnimator valueAnimator = ValueAnimator.ofInt(18, 0);
         * valueAnimator.addUpdateListener(animation ->
         * BlurWallpaperProvider.getInstance(this).blurWithLauncherView(mergedView,
         * (Integer) animation.getAnimatedValue()));
         */
        set.play(ObjectAnimator.ofFloat(mFolderWindowContainer, View.X, startBounds.left))
                .with(ObjectAnimator.ofFloat(mFolderWindowContainer, View.Y, startBounds.top))
                .with(ObjectAnimator.ofFloat(mFolderWindowContainer, View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator.ofFloat(mFolderWindowContainer, View.SCALE_Y, startScaleFinal))
                .with(ObjectAnimator.ofFloat(blurLayer, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mHorizontalPager, View.ALPHA, 1f))
                .with(ObjectAnimator.ofFloat(mIndicator, View.ALPHA, 1f))
                .with(ObjectAnimator.ofFloat(mDock, View.ALPHA, 1f));
        // .with(valueAnimator);
        set.setDuration(300);
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mHorizontalPager.setVisibility(VISIBLE);
                mDock.setVisibility(VISIBLE);
                mIndicator.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFolderWindowContainer.setVisibility(View.GONE);
                currentAnimator = null;
                blurLayer.setAlpha(0f);
                mHorizontalPager.setAlpha(1f);
                mIndicator.setAlpha(1f);
                mDock.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mFolderWindowContainer.setVisibility(View.GONE);
                currentAnimator = null;
                blurLayer.setAlpha(0f);
                mHorizontalPager.setAlpha(1f);
                mIndicator.setAlpha(1f);
                mDock.setAlpha(1f);
            }
        });
        set.start();
        currentAnimator = set;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final boolean alreadyOnHome = hasWindowFocus() && ((intent.getFlags()
                & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        boolean shouldMoveToDefaultScreen = alreadyOnHome && swipeSearchContainer.getVisibility() != VISIBLE
                && !isWobbling && mFolderWindowContainer.getVisibility() != View.VISIBLE
                && (activeRoundedWidgetView == null || !activeRoundedWidgetView.isWidgetActivated());

        if (alreadyOnHome) {
            returnToHomeScreen();
        }

        if (shouldMoveToDefaultScreen) {
            mHorizontalPager.setVisibility(VISIBLE);
            mHorizontalPager.setAlpha(1f);
            mDock.setVisibility(VISIBLE);
            mDock.setAlpha(1f);
            mIndicator.setVisibility(VISIBLE);
            mIndicator.setAlpha(1f);
            mHorizontalPager.snapToPage(1);
        }
    }

    private void returnToHomeScreen() {
        if (activeRoundedWidgetView != null && activeRoundedWidgetView.isWidgetActivated()) {
            hideWidgetResizeContainer();
        }

        if (mSearchInput != null) {
            mSearchInput.setText("");
        }

        if (swipeSearchContainer.getVisibility() == VISIBLE) {
            hideSwipeSearchContainer();
        }

        if (isWobbling) {
            handleWobbling(false);
        } else if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
            hideFolderWindowContainer();
        }
    }

    private void showSwipeSearchContainer() {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        int animationDuration = (int) (blurLayer.getAlpha() * 300);
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(swipeSearchContainer, View.TRANSLATION_Y, 0))
                .with(ObjectAnimator.ofFloat(blurLayer, View.ALPHA, 1f))
                .with(ObjectAnimator.ofFloat(mHorizontalPager, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mIndicator, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mDock, View.ALPHA, 0f));
        set.setDuration(animationDuration);
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                currentAnimator = null;
                swipeSearchContainer.setVisibility(GONE);
                blurLayer.setAlpha(0f);
                mHorizontalPager.setVisibility(VISIBLE);
                mDock.setVisibility(VISIBLE);
                mIndicator.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                currentAnimator = null;

                blurLayer.setAlpha(1f);
                mHorizontalPager.setVisibility(GONE);
                mDock.setVisibility(GONE);
                mIndicator.setVisibility(GONE);

                BlissInput searchEditText = swipeSearchContainer.findViewById(R.id.search_input);
                ImageView clearSuggestions = swipeSearchContainer.findViewById(R.id.clearSuggestionImageView);
                searchDisposableObserver = RxTextView.textChanges(searchEditText).debounce(300, TimeUnit.MILLISECONDS)
                        .map(CharSequence::toString).distinctUntilChanged().switchMap(charSequence -> {
                            if (charSequence != null && charSequence.length() > 0) {
                                LauncherActivity.this.runOnUiThread(() -> clearSuggestions.setVisibility(VISIBLE));
                                return LauncherActivity.this.searchForQuery(charSequence);
                            } else {
                                LauncherActivity.this.runOnUiThread(() -> clearSuggestions.setVisibility(GONE));
                                return Observable.just(new SuggestionsResult(charSequence));
                            }
                        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new SearchInputDisposableObserver(LauncherActivity.this,
                                ((RecyclerView) swipeSearchContainer.findViewById(R.id.suggestionRecyclerView))
                                        .getAdapter(),
                                swipeSearchContainer));
                searchEditText.requestFocus();
                refreshSuggestedApps(swipeSearchContainer, true);
            }
        });
        set.start();
        currentAnimator = set;
    }

    private void setUpSwipeSearchContainer() {
        BlissInput searchEditText = swipeSearchContainer.findViewById(R.id.search_input);
        ImageView clearSuggestions = swipeSearchContainer.findViewById(R.id.clearSuggestionImageView);
        clearSuggestions.setOnClickListener(v -> {
            searchEditText.setText("");
            searchEditText.clearFocus();
        });

        RecyclerView suggestionRecyclerView = swipeSearchContainer.findViewById(R.id.suggestionRecyclerView);
        AutoCompleteAdapter networkSuggestionAdapter = new AutoCompleteAdapter(this);
        suggestionRecyclerView.setAdapter(networkSuggestionAdapter);
        suggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            } else {
                showKeyboard(v);
            }
        });
        searchEditText.clearFocus();

        searchEditText.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(searchEditText);
                runSearch(searchEditText.getText().toString());
                searchEditText.setText("");
                searchEditText.clearFocus();
                return true;
            }
            return false;
        });
    }

    private void hideSwipeSearchContainer() {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(swipeSearchContainer, View.TRANSLATION_Y, -swipeSearchContainer.getHeight()))
                .with(ObjectAnimator.ofFloat(mHorizontalPager, View.ALPHA, 1f))
                .with(ObjectAnimator.ofFloat(mIndicator, View.ALPHA, 1f))
                .with(ObjectAnimator.ofFloat(mDock, View.ALPHA, 1f))
                .with(ObjectAnimator.ofFloat(blurLayer, View.ALPHA, 0f));
        set.setDuration(300);
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mHorizontalPager.setVisibility(VISIBLE);
                mDock.setVisibility(VISIBLE);
                mIndicator.setVisibility(VISIBLE);
                // BlurWallpaperProvider.getInstance(LauncherActivity.this).clear();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                currentAnimator = null;
                swipeSearchContainer.setVisibility(VISIBLE);
                blurLayer.setAlpha(1f);
                mHorizontalPager.setVisibility(GONE);
                mDock.setVisibility(GONE);
                mIndicator.setVisibility(GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                currentAnimator = null;
                swipeSearchContainer.setVisibility(GONE);
                blurLayer.setAlpha(0f);
                if (searchDisposableObserver != null && !searchDisposableObserver.isDisposed()) {
                    searchDisposableObserver.dispose();
                }
                ((BlissInput) swipeSearchContainer.findViewById(R.id.search_input)).setText("");
                swipeSearchContainer.findViewById(R.id.search_input).clearFocus();
            }
        });
        set.start();
        currentAnimator = set;
    }

    @Override
    public void onSwipeStart() {
        swipeSearchContainer.setTranslationY(BlissLauncher.getApplication(this).getDeviceProfile().availableHeightPx);
        swipeSearchContainer.setVisibility(GONE);
        showSwipeSearch = false;
    }

    @Override
    public void onSwipe(int position) {
        if ((longPressed && !mLongClickStartsDrag) || (!longPressed && isWobbling)) {
            return;
        }
        swipeSearchContainer.setVisibility(VISIBLE);
        float translateBy = position * 1.25f;
        if (translateBy <= swipeSearchContainer.getHeight()) {
            swipeSearchContainer.setTranslationY(-swipeSearchContainer.getHeight() + translateBy);
            float deltaAlpha = 1f - (translateBy / swipeSearchContainer.getHeight());
            mHorizontalPager.setAlpha(deltaAlpha);
            mIndicator.setAlpha(deltaAlpha);
            mDock.setAlpha(deltaAlpha);
            blurLayer.setAlpha(1 - deltaAlpha);
        }

        if (translateBy >= swipeSearchContainer.getHeight() / 2) {
            showSwipeSearch = true;
        } else {
            showSwipeSearch = false;
        }
    }

    @Override
    public void onSwipeFinish() {
        if (showSwipeSearch) {
            showSwipeSearchContainer();
        } else {
            hideSwipeSearchContainer();
        }
    }

    public void showWidgetResizeContainer(RoundedWidgetView roundedWidgetView) {
        RelativeLayout widgetResizeContainer = widgetsPage.findViewById(R.id.widget_resizer_container);
        if (widgetResizeContainer.getVisibility() != VISIBLE) {
            activeRoundedWidgetView = roundedWidgetView;

            SeekBar seekBar = widgetResizeContainer.findViewById(R.id.widget_resizer_seekbar);
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }

            seekBar.setOnTouchListener((v, event) -> {
                seekBar.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });

            AnimatorSet set = new AnimatorSet();
            set.play(
                    ObjectAnimator.ofFloat(widgetResizeContainer, View.TRANSLATION_Y, Utilities.pxFromDp(48, this), 0));
            set.setDuration(200);
            set.setInterpolator(new LinearInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    widgetResizeContainer.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    currentAnimator = null;
                    widgetResizeContainer.setVisibility(GONE);
                    roundedWidgetView.removeBorder();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    currentAnimator = null;
                    prepareWidgetResizeSeekBar(seekBar);
                    roundedWidgetView.addBorder();
                }
            });
            set.start();
            currentAnimator = set;
        }

    }

    private void prepareWidgetResizeSeekBar(SeekBar seekBar) {
        int minHeight = activeRoundedWidgetView.getAppWidgetInfo().minResizeHeight;
        int maxHeight = mDeviceProfile.availableHeightPx * 3 / 4;
        int normalisedDifference = (maxHeight - minHeight) / 100;
        int defaultHeight = activeRoundedWidgetView.getHeight();
        int currentProgress = (defaultHeight - minHeight) * 100 / (maxHeight - minHeight);

        seekBar.setMax(100);
        seekBar.setProgress(currentProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newHeight = minHeight + (normalisedDifference * progress);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) activeRoundedWidgetView
                        .getLayoutParams();
                layoutParams.height = newHeight;
                activeRoundedWidgetView.setLayoutParams(layoutParams);

                Bundle newOps = new Bundle();
                newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, mDeviceProfile.getMaxWidgetWidth());
                newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, mDeviceProfile.getMaxWidgetWidth());
                newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, newHeight);
                newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, newHeight);
                activeRoundedWidgetView.updateAppWidgetOptions(newOps);
                activeRoundedWidgetView.requestLayout();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DatabaseManager.getManager(LauncherActivity.this)
                        .saveWidgetHeight(activeRoundedWidgetView.getAppWidgetId(), seekBar.getProgress());
            }
        });

    }

    public void hideWidgetResizeContainer() {
        RelativeLayout widgetResizeContainer = widgetsPage.findViewById(R.id.widget_resizer_container);
        if (widgetResizeContainer.getVisibility() == VISIBLE) {
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }
            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(widgetResizeContainer, View.TRANSLATION_Y, Utilities.pxFromDp(48, this)));
            set.setDuration(200);
            set.setInterpolator(new LinearInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    ((SeekBar) widgetsPage.findViewById(R.id.widget_resizer_seekbar)).setOnSeekBarChangeListener(null);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    currentAnimator = null;
                    widgetResizeContainer.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    currentAnimator = null;
                    widgetResizeContainer.setVisibility(GONE);
                    activeRoundedWidgetView.removeBorder();
                }
            });
            set.start();
            currentAnimator = set;
        }
    }

    public void forceReload() {
        /**
         * Return to homescreen if [forceReload] is called to prevent the icons from
         * applying wrong dimensions
         */
        if (mFolderWindowContainer.getVisibility() == View.VISIBLE) {
            returnToHomeScreen();
        }

        allAppsDisplayed = false;
        BlissLauncher.getApplication(this).getAppProvider().getAppsRepository()
                .updateAppsRelay(Collections.emptyList());
    }

    @Override
    public void onColorsChanged() {
        updateTheme();
    }

    private void updateTheme() {
        if (mThemeRes != ThemesKt.getActivityThemeRes(this)) {
            recreate();
        }
    }

    /**
     * Adapter for folder apps.
     */
    public class FolderAppsPagerAdapter extends PagerAdapter {

        private Context mContext;
        private List<LauncherItem> mFolderAppItems;

        public FolderAppsPagerAdapter(Context context, List<LauncherItem> items) {
            this.mContext = context;
            this.mFolderAppItems = items;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            GridLayout viewGroup = (GridLayout) LayoutInflater.from(mContext).inflate(R.layout.apps_page, container,
                    false);
            viewGroup.setRowCount(3);
            viewGroup.setColumnCount(3);
            viewGroup.setPadding(mDeviceProfile.iconDrawablePaddingPx / 2, mDeviceProfile.iconDrawablePaddingPx / 2,
                    mDeviceProfile.iconDrawablePaddingPx / 2, mDeviceProfile.iconDrawablePaddingPx / 2);
            int i = 0;
            while (9 * position + i < mFolderAppItems.size() && i < 9) {
                LauncherItem appItem = mFolderAppItems.get(9 * position + i);
                BlissFrameLayout appView = prepareLauncherItem(appItem);
                GridLayout.LayoutParams iconLayoutParams = new GridLayout.LayoutParams();
                iconLayoutParams.height = mDeviceProfile.cellHeightPx;
                iconLayoutParams.width = mDeviceProfile.cellWidthPx;
                appView.findViewById(R.id.app_label).setVisibility(View.VISIBLE);
                appView.setLayoutParams(iconLayoutParams);
                viewGroup.addView(appView);
                i++;
            }
            container.addView(viewGroup);
            return viewGroup;
        }

        @Override
        public int getCount() {
            return (int) Math.ceil((float) mFolderAppItems.size() / 9);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    class ReorderAlarmListener implements Alarm.OnAlarmListener {

        private final ViewGroup mParent;
        private final int mIndex;
        private GridLayout mPage;

        public ReorderAlarmListener(GridLayout page, ViewGroup parent, int index) {
            mPage = page;
            mParent = parent;
            mIndex = index;
        }

        public void onAlarm(Alarm alarm) {
            GridLayout gridLayout = pages.get(getCurrentAppsPageNumber());
            if (movingApp.getParent() != null && (parentPage == getCurrentAppsPageNumber()
                    || gridLayout.getChildCount() < mDeviceProfile.maxAppsPerPage)) {
                ((ViewGroup) movingApp.getParent()).removeView(movingApp);
                if (gridLayout.getChildCount() < mDeviceProfile.maxAppsPerPage) {
                    addAppToGrid(gridLayout, movingApp, mIndex);
                    parentPage = getCurrentAppsPageNumber();
                }
            }
        }
    }

    class DockReorderAlarmListener implements Alarm.OnAlarmListener {
        private final int mIndex;

        DockReorderAlarmListener(int index) {
            mIndex = index;
        }

        public void onAlarm(Alarm alarm) {
            if (mDock.getChildCount() < mDeviceProfile.numColumns || parentPage == -99) {
                if (movingApp.getParent() != null) {
                    ((ViewGroup) movingApp.getParent()).removeView(movingApp);
                }
                parentPage = -99;
                addAppToDock(movingApp, mIndex);
            }

        }
    }
}
