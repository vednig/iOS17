package foundation.e.blisslauncher.core.customviews;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Property;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import foundation.e.blisslauncher.BlissLauncher;
import foundation.e.blisslauncher.R;
import foundation.e.blisslauncher.core.DeviceProfile;
import foundation.e.blisslauncher.core.Utilities;
import foundation.e.blisslauncher.core.database.model.ApplicationItem;
import foundation.e.blisslauncher.core.database.model.CalendarIcon;
import foundation.e.blisslauncher.core.database.model.FolderItem;
import foundation.e.blisslauncher.core.database.model.LauncherItem;
import foundation.e.blisslauncher.core.database.model.ShortcutItem;
import foundation.e.blisslauncher.core.utils.Constants;
import foundation.e.blisslauncher.features.notification.DotRenderer;

/**
 * Created by falcon on 20/3/18.
 */

public class BlissFrameLayout extends FrameLayout {

    private final Context mContext;

    private boolean hasBadge = false;
    private Rect mTempIconBounds = new Rect();
    private DeviceProfile mDeviceProfile;

    private float mBadgeScale;

    private static final Property<BlissFrameLayout, Float> BADGE_SCALE_PROPERTY
            = new Property<BlissFrameLayout, Float>(Float.TYPE, "badgeScale") {
        @Override
        public Float get(BlissFrameLayout bubbleTextView) {
            return bubbleTextView.mBadgeScale;
        }

        @Override
        public void set(BlissFrameLayout bubbleTextView, Float value) {
            bubbleTextView.mBadgeScale = value;
            bubbleTextView.invalidate();
        }
    };
    private int mIconSize;
    private boolean mWithText;
    private DotRenderer mDotRenderer;
    private LauncherItem mLauncherItem;

    public BlissFrameLayout(Context context) {
        this(context, null, 0);
    }

    public BlissFrameLayout(Context context,
                            AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlissFrameLayout(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mDeviceProfile = BlissLauncher.getApplication(mContext).getDeviceProfile();
        mDotRenderer = new DotRenderer(mContext, mDeviceProfile.iconSizePx);
        mIconSize = mDeviceProfile.iconSizePx;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawBadgeIfNecessary(canvas);
    }

    private void drawBadgeIfNecessary(Canvas canvas) {
        if (hasBadge) {
            getIconBounds(mTempIconBounds);
            mDotRenderer.drawDot(canvas, mTempIconBounds);
        }
    }

    public void setWithText(boolean withText) {
        this.mWithText = withText;
    }

    private void getIconBounds(Rect outBounds) {
        int cellHeightPx;
        if (mWithText) {
            cellHeightPx = mDeviceProfile.cellHeightWithoutPaddingPx;
        } else {
            cellHeightPx = mDeviceProfile.hotseatCellHeightWithoutPaddingPx;
        }
        int top = (getHeight() - cellHeightPx) / 2;
        int left = (getWidth() - mIconSize) / 2;
        int right = left + mIconSize;
        int bottom = top + mIconSize;
        outBounds.set(left, top, right, bottom);
    }

    public void applyBadge(boolean isBadge, boolean withText) {
        mWithText = withText;
        boolean wasBadged = hasBadge;
        hasBadge = isBadge;
        boolean isBadged = hasBadge;
        float newBadgeScale = isBadge ? 1f : 0;

        if ((wasBadged ^ isBadged)) {
            if (isShown()) {
                ObjectAnimator.ofFloat(this, BADGE_SCALE_PROPERTY, newBadgeScale).start();
            } else {
                mBadgeScale = newBadgeScale;
                invalidate();
            }
        }
    }

    public LauncherItem getLauncherItem() {
        return mLauncherItem;
    }

    public void setLauncherItem(LauncherItem launcherItem) {
        this.mLauncherItem = launcherItem;
        if (launcherItem.itemType == Constants.ITEM_TYPE_APPLICATION) {
            bindApplicationItem((ApplicationItem) launcherItem);
        } else if (launcherItem.itemType == Constants.ITEM_TYPE_SHORTCUT) {
            bindShortcutItem((ShortcutItem) launcherItem);
        } else if (launcherItem.itemType == Constants.ITEM_TYPE_FOLDER) {
            bindFolderItem((FolderItem) launcherItem);
        }
    }

    private void bindFolderItem(FolderItem folderItem) {
        final TextView label = findViewById(R.id.app_label);
        final SquareFrameLayout icon = findViewById(R.id.app_icon);
        final SquareImageView squareImageView = findViewById(
                R.id.icon_image_view);
        icon.enableBlur();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
        layoutParams.leftMargin = mDeviceProfile.iconDrawablePaddingPx / 2;
        layoutParams.rightMargin = mDeviceProfile.iconDrawablePaddingPx / 2;

        label.setPadding((int) Utilities.pxFromDp(4, mContext),
                (int) Utilities.pxFromDp(0, mContext),
                (int) Utilities.pxFromDp(4, mContext),
                (int) Utilities.pxFromDp(0, mContext));
        squareImageView.setImageDrawable(folderItem.icon);
        label.setText(folderItem.title.toString());
        label.setTextSize(12);
        List<Object> tags = new ArrayList<>();
        tags.add(squareImageView);
        tags.add(label);
        tags.add(folderItem);
        setTag(tags);
    }

    private void bindShortcutItem(ShortcutItem shortcutItem) {
        final TextView label = findViewById(R.id.app_label);
        final SquareFrameLayout icon = findViewById(R.id.app_icon);
        final SquareImageView squareImageView = findViewById(
                R.id.icon_image_view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
        layoutParams.leftMargin = mDeviceProfile.iconDrawablePaddingPx / 2;
        layoutParams.rightMargin = mDeviceProfile.iconDrawablePaddingPx / 2;

        label.setPadding((int) Utilities.pxFromDp(4, mContext),
                (int) Utilities.pxFromDp(0, mContext),
                (int) Utilities.pxFromDp(4, mContext),
                (int) Utilities.pxFromDp(0, mContext));
        squareImageView.setImageDrawable(shortcutItem.icon);
        label.setText(shortcutItem.title.toString());
        label.setTextSize(12);
        List<Object> tags = new ArrayList<>();
        tags.add(squareImageView);
        tags.add(label);
        tags.add(shortcutItem);
        setTag(tags);
    }

    private void bindApplicationItem(ApplicationItem applicationItem) {
        final TextView label = findViewById(R.id.app_label);
        final SquareFrameLayout icon = findViewById(R.id.app_icon);
        final SquareImageView squareImageView = findViewById(
                R.id.icon_image_view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
        layoutParams.leftMargin = mDeviceProfile.iconDrawablePaddingPx / 2;
        layoutParams.rightMargin = mDeviceProfile.iconDrawablePaddingPx / 2;
        label.setPadding((int) Utilities.pxFromDp(4, mContext),
                (int) Utilities.pxFromDp(0, mContext),
                (int) Utilities.pxFromDp(4, mContext),
                (int) Utilities.pxFromDp(0, mContext));
        if (applicationItem.appType == ApplicationItem.TYPE_CLOCK) {
            final CustomAnalogClock analogClock = findViewById(
                    R.id.icon_clock);
            analogClock.setAutoUpdate(true);
            analogClock.setVisibility(View.VISIBLE);
            squareImageView.setVisibility(GONE);
        } else if (applicationItem.appType == ApplicationItem.TYPE_CALENDAR) {

            TextView monthTextView = findViewById(R.id.calendar_month_textview);
            monthTextView.getLayoutParams().height = mDeviceProfile.monthTextviewHeight;
            monthTextView.getLayoutParams().width = mDeviceProfile.calendarIconWidth;
            int monthPx = mDeviceProfile.monthTextSize;
            monthTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, monthPx / 2);

            TextView dateTextView = findViewById(R.id.calendar_date_textview);
            dateTextView.getLayoutParams().height = mDeviceProfile.dateTextviewHeight;
            dateTextView.getLayoutParams().width = mDeviceProfile.calendarIconWidth;
            int datePx = mDeviceProfile.dateTextSize;
            dateTextView.setPadding(0, mDeviceProfile.dateTextTopPadding, 0,
                    mDeviceProfile.dateTextBottomPadding);

            dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, datePx / 2);

            findViewById(R.id.icon_calendar).setVisibility(View.VISIBLE);
            squareImageView.setVisibility(GONE);

            CalendarIcon calendarIcon = new CalendarIcon(monthTextView, dateTextView);
            calendarIcon.monthTextView.setText(
                    Utilities.convertMonthToString());
            calendarIcon.dayTextView.setText(
                    String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
        } else {
            squareImageView.setImageDrawable(applicationItem.icon);
        }
        label.setText(applicationItem.title.toString());
        label.setTextSize(12);
        List<Object> tags = new ArrayList<>();
        tags.add(squareImageView);
        tags.add(label);
        tags.add(applicationItem);
        setTag(tags);
    }
}
