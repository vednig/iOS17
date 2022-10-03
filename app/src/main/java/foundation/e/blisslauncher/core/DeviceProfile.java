package foundation.e.blisslauncher.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;

import foundation.e.blisslauncher.core.customviews.PathParser;
import foundation.e.blisslauncher.core.utils.AdaptiveIconUtils;

public class DeviceProfile {

    private static final int TYPE_WORKSPACE = 0;
    private static final int TYPE_FOLDER = 1;
    private static final int TYPE_HOTSEAT = 2;
    private static final float ICON_SIZE_DEFINED_IN_APP_DP = 48;

    public static Path path;
    private final float widthCm;
    private final float ratio;
    private int statusBarHeight;
    public int cellHeightWithoutPaddingPx;
    public int hotseatCellHeightWithoutPaddingPx;
    public int fillResIconDpi;

    public interface LauncherLayoutChangeListener {
        void onLauncherLayoutChanged();
    }

    /**
     * Number of icons per row and column in the workspace.
     */
    public int numRows;
    public int numColumns;
    public int maxAppsPerPage;

    /**
     * Number of icons per row and column in the folder.
     */
    public int numFolderRows;
    public int numFolderColumns;

    /**
     * Number of icons inside the hotseat area.
     */
    public int numHotseatIcons;

    // Device properties in current orientation
    public final int widthPx;
    public final int heightPx;
    public final int availableWidthPx;
    public final int availableHeightPx;

    // Page indicator
    public int pageIndicatorSizePx;
    public int pageIndicatorTopPaddingPx;
    public int pageIndicatorBottomPaddingPx;

    // Workspace icons
    public int iconSizePx;
    public int iconTextSizePx;
    public int iconDrawablePaddingPx;

    // Calendar icon
    public int dateTextSize;
    public int monthTextSize;
    public int dateTextviewHeight;
    public int monthTextviewHeight;
    public int calendarIconWidth;
    public int dateTextBottomPadding;
    public int dateTextTopPadding;

    // Uninstall icon
    public int uninstallIconSizePx;
    public int uninstallIconPadding;

    public int cellWidthPx;
    public int cellHeightPx;
    public int workspaceCellPaddingXPx;

    // Widget
    public int maxWidgetWidth;
    public int maxWidgetHeight;

    // Folder
    public int folderBackgroundOffset;
    public int folderIconSizePx;
    public int folderIconPreviewPadding;

    // Folder cell
    public int folderCellWidthPx;
    public int folderCellHeightPx;

    // Folder child
    public int folderChildIconSizePx;
    public int folderChildTextSizePx;
    public int folderChildDrawablePaddingPx;

    // Hotseat
    public int hotseatCellHeightPx;
    // In portrait: size = height, in landscape: size = width
    public int hotseatBarSizePx;
    public int hotseatBarTopPaddingPx;
    public int hotseatBarBottomPaddingPx;

    public int hotseatBarLeftNavBarLeftPaddingPx;
    public int hotseatBarLeftNavBarRightPaddingPx;

    public int hotseatBarRightNavBarLeftPaddingPx;
    public int hotseatBarRightNavBarRightPaddingPx;

    // All apps
    public int allAppsCellHeightPx;
    public int allAppsNumCols;
    public int allAppsNumPredictiveCols;
    public int allAppsButtonVisualSize;
    public int allAppsIconSizePx;
    public int allAppsIconDrawablePaddingPx;
    public float allAppsIconTextSizePx;

    // Drop Target
    public int dropTargetBarSizePx;

    // Insets
    private Rect mInsets = new Rect();

    // Listeners
    private ArrayList<LauncherLayoutChangeListener> mListeners = new ArrayList<>();

    private static final String TAG = "DeviceProfile";

    public DeviceProfile(Context context) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);

        Point smallestSize = new Point();
        Point largestSize = new Point();
        display.getCurrentSizeRange(smallestSize, largestSize);

        availableWidthPx = smallestSize.x;
        availableHeightPx = largestSize.y;

        Point realSize = new Point();
        display.getRealSize(realSize);

        widthPx = realSize.x;
        double x = widthPx / dm.densityDpi;
        ratio = (float) dm.densityDpi / dm.xdpi;
        widthCm = (float) (x * 2.540001f);
        heightPx = realSize.y;

        context = getContext(context, Configuration.ORIENTATION_PORTRAIT);
        Resources res = context.getResources();

        // status bar height
        statusBarHeight = 0;
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }

        ComponentName cn = new ComponentName(context.getPackageName(), this.getClass().getName());

        pageIndicatorSizePx = Utilities.pxFromDp(8, dm);
        pageIndicatorTopPaddingPx = Utilities.pxFromDp(8, dm);
        pageIndicatorBottomPaddingPx = Utilities.pxFromDp(8, dm);

        numColumns = 4;
        numFolderColumns = 3;
        numHotseatIcons = numColumns;
        numFolderRows = numFolderColumns;

        // Calculate all of the remaining variables.
        updateAvailableDimensions(dm, res);

        /*
         * // Now that we have all of the variables calculated, we can tune certain
         * sizes. float aspectRatio = ((float) Math.max(widthPx, heightPx)) /
         * Math.min(widthPx, heightPx); boolean isTallDevice =
         * Float.compare(aspectRatio, TALL_DEVICE_ASPECT_RATIO_THRESHOLD) >= 0; if
         * (isTallDevice) { // We increase the hotseat size when there is extra space.
         * // ie. For a display with a large aspect ratio, we can keep the icons on the
         * workspace // in portrait mode closer together by adding more height to the
         * hotseat. // Note: This calculation was created after noticing a pattern in
         * the design spec. int extraSpace = getCellSize().y - iconSizePx -
         * iconDrawablePaddingPx; hotseatBarSizePx += extraSpace - pageIndicatorSizePx;
         * 
         * // Recalculate the available dimensions using the new hotseat size.
         * updateAvailableDimensions(dm, res); }
         */
    }

    private void updateAvailableDimensions(DisplayMetrics dm, Resources res) {
        updateIconSize(1f, res, dm);

        // Check to see if the icons fit within the available height. If not, then scale
        // down.
        int usedHeight = (cellHeightPx * numRows);

        int remainHeight = (availableHeightPx - usedHeight - hotseatCellHeightPx - pageIndicatorSizePx
                - pageIndicatorTopPaddingPx - pageIndicatorBottomPaddingPx);
        int incrementHeight = remainHeight / (numRows + 1);
        cellHeightPx = cellHeightPx + incrementHeight;
        hotseatCellHeightPx = hotseatCellHeightPx + incrementHeight;
        path = getRoundedCornerPath(iconSizePx);
    }

    private void updateIconSize(float scale, Resources res, DisplayMetrics dm) {
        // Workspace
        /*
         * if (availableWidthPx < 640) { iconSizePx = 95; } else if (availableWidthPx <
         * 960) { iconSizePx = 126; } else if (availableWidthPx < 1100) { iconSizePx =
         * 160; } else if (availableWidthPx < 1200) { iconSizePx = 190; } else {
         * iconSizePx = 213; }
         */

        float a = 1.578f;
        float b = 1.23f;

        iconSizePx = (int) (0.75 * widthPx / widthCm);
        iconTextSizePx = (int) (Utilities.pxFromSp(12, dm) * scale);
        iconDrawablePaddingPx = (availableWidthPx - iconSizePx * 4) / 5;

        int tempUninstallIconSize = iconSizePx * 72 / 192;
        uninstallIconSizePx = (tempUninstallIconSize > iconDrawablePaddingPx)
                ? iconDrawablePaddingPx
                : tempUninstallIconSize;
        uninstallIconPadding = iconSizePx * 10 / 192;

        calendarIconWidth = iconSizePx;
        monthTextviewHeight = iconSizePx * 40 / 192;
        monthTextSize = iconSizePx * 48 / 192;
        dateTextviewHeight = iconSizePx * 152 / 192;
        dateTextSize = iconSizePx * 154 / 192;

        dateTextTopPadding = (dateTextviewHeight
                - (int) (1.14 * Utilities.calculateTextHeight((float) dateTextSize / 2))) / 2;
        dateTextBottomPadding = (dateTextviewHeight
                - (int) (0.86 * Utilities.calculateTextHeight((float) dateTextSize / 2))) / 2;

        cellHeightWithoutPaddingPx = iconSizePx + Utilities.pxFromDp(4, dm)
                + Utilities.calculateTextHeight(iconTextSizePx);

        cellHeightPx = cellHeightWithoutPaddingPx + iconDrawablePaddingPx;
        cellWidthPx = iconSizePx + iconDrawablePaddingPx;

        // Hotseat
        hotseatCellHeightWithoutPaddingPx = iconSizePx;
        hotseatCellHeightPx = hotseatCellHeightWithoutPaddingPx + iconDrawablePaddingPx;

        numRows = (availableHeightPx - Utilities.pxFromDp(8, dm) - pageIndicatorTopPaddingPx
                - pageIndicatorBottomPaddingPx - pageIndicatorSizePx - hotseatCellHeightPx) / cellHeightPx;

        maxAppsPerPage = numColumns * numRows;

        // Folder icon
        folderIconSizePx = iconSizePx;

        fillResIconDpi = getLauncherIconDensity(iconSizePx);

        maxWidgetWidth = availableWidthPx - (2 * Utilities.pxFromDp(8, dm));
        maxWidgetHeight = getWorkspaceHeight();
    }

    public static int calculateCellWidth(int width, int countX) {
        return width / countX;
    }

    public static int calculateCellHeight(int height, int countY) {
        return height / countY;
    }

    private int getCurrentWidth() {
        return Math.min(widthPx, heightPx);
    }

    private int getCurrentHeight() {
        return Math.max(widthPx, heightPx);
    }

    public int getWorkspaceHeight() {
        return cellHeightPx * numRows;
    }

    public int getAvailableWidthPx() {
        return availableWidthPx;
    }

    public int getPageIndicatorHeight() {
        return pageIndicatorSizePx + pageIndicatorBottomPaddingPx + pageIndicatorTopPaddingPx;
    }

    public int getMaxWidgetWidth() {
        return maxWidgetWidth;
    }

    public int getMaxWidgetHeight() {
        return maxWidgetHeight;
    }

    public int getCellHeight(int containerType) {
        switch (containerType) {
            case TYPE_WORKSPACE :
                return cellHeightPx;
            case TYPE_FOLDER :
                return folderCellHeightPx;
            case TYPE_HOTSEAT :
                return hotseatCellHeightPx;
            default :
                // ??
                return 0;
        }
    }

    private static Context getContext(Context c, int orientation) {
        Configuration context = new Configuration(c.getResources().getConfiguration());
        context.orientation = orientation;
        return c.createConfigurationContext(context);

    }

    public Path getRoundedCornerPath(int iconSize) {
        return resizePath(PathParser.createPathFromPathData(AdaptiveIconUtils.getMaskPath()), iconSize, iconSize);
    }

    private Path resizePath(Path path, int width, int height) {
        RectF bounds = new RectF(0, 0, width, height);
        Path resizedPath = new Path(path);
        RectF src = new RectF();
        resizedPath.computeBounds(src, true);

        Matrix resizeMatrix = new Matrix();
        resizeMatrix.setRectToRect(src, bounds, Matrix.ScaleToFit.CENTER);
        resizedPath.transform(resizeMatrix);

        return resizedPath;
    }

    public boolean hasSoftNavigationBar(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);

        Point smallestSize = new Point();
        Point largestSize = new Point();
        display.getCurrentSizeRange(smallestSize, largestSize);

        int availableHeight = largestSize.y;

        Point realSize = new Point();
        display.getRealSize(realSize);
        int realHeight = realSize.y;
        context = getContext(context, Configuration.ORIENTATION_PORTRAIT);
        Resources res = context.getResources();

        // status bar height
        statusBarHeight = 0;
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }

        return (realHeight - availableHeight - statusBarHeight) > 0;
    }

    private int getLauncherIconDensity(int requiredSize) {
        // Densities typically defined by an app.
        int[] densityBuckets = new int[]{DisplayMetrics.DENSITY_LOW, DisplayMetrics.DENSITY_MEDIUM,
                DisplayMetrics.DENSITY_TV, DisplayMetrics.DENSITY_HIGH, DisplayMetrics.DENSITY_XHIGH,
                DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_XXXHIGH};

        int density = DisplayMetrics.DENSITY_XXXHIGH;
        for (int i = densityBuckets.length - 1; i >= 0; i--) {
            float expectedSize = ICON_SIZE_DEFINED_IN_APP_DP * densityBuckets[i] / DisplayMetrics.DENSITY_DEFAULT;
            if (expectedSize >= requiredSize) {
                density = densityBuckets[i];
            }
        }

        return density;
    }
}
