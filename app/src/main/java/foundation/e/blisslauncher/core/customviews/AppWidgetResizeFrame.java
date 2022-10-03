package foundation.e.blisslauncher.core.customviews;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Rect;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import foundation.e.blisslauncher.R;

public class AppWidgetResizeFrame extends FrameLayout {
    private RoundedWidgetView mRoundedWidgetView;

    private ImageView mTopHandle;
    private ImageView mBottomHandle;

    private boolean mTopBorderActive;
    private boolean mBottomBorderActive;
    private int mWidgetPaddingTop;
    private int mWidgetPaddingBottom;

    private int mBaselineWidth;
    private int mBaselineHeight;
    private int mBaselineX;
    private int mBaselineY;
    private int mResizeMode;

    private int mRunningHInc;
    private int mRunningVInc;
    private int mMinHeight;
    private int mDeltaX;
    private int mDeltaY;
    private int mDeltaXAddOn;
    private int mDeltaYAddOn;

    private int mBackgroundPadding;
    private int mTouchTargetWidth;

    private int mTopTouchRegionAdjustment = 0;
    private int mBottomTouchRegionAdjustment = 0;

    int[] mDirectionVector = new int[2];
    int[] mLastDirectionVector = new int[2];

    final int SNAP_DURATION = 150;
    final int BACKGROUND_PADDING = 24;
    final float DIMMED_HANDLE_ALPHA = 0f;
    final float RESIZE_THRESHOLD = 0.66f;

    private static Rect mTmpRect = new Rect();

    public static final int TOP = 1;
    public static final int BOTTOM = 3;

    private Context mContext;

    private static final String TAG = "AppWidgetResizeFrame";

    public AppWidgetResizeFrame(@NonNull Context context, RoundedWidgetView widgetView) {
        super(context);
        mRoundedWidgetView = widgetView;
        mContext = context;

        final AppWidgetProviderInfo info = widgetView.getAppWidgetInfo();
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, info.provider, null);
        // We want to account for the extra amount of padding that we are adding to the
        // widget
        // to ensure that it gets the full amount of space that it has requested
        mMinHeight = info.minHeight + padding.top + padding.bottom;

        setBackgroundResource(R.drawable.widget_resize_frame);
        setPadding(0, 0, 0, 0);
        // setLayoutParams(mRoundedWidgetView.getLayoutParams());
    }
}
